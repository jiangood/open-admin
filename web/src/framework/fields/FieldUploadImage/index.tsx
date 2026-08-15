import React from "react";
import {Button, Divider, Modal, Radio, Select, Space, Upload, message} from "antd";
import {DeleteOutlined, EyeOutlined, PlusOutlined} from "@ant-design/icons";
import Compressor from "compressorjs";
import Cropper from "cropperjs";
import "cropperjs/dist/cropper.css";
import {HttpClient, ObjectUtils, UrlUtils} from "../../utils";
import type {FieldProps} from '../types';

export interface FieldUploadImageProps extends FieldProps<string> {
    /** 最大上传数量，默认 1 */
    maxCount?: number;
    /** 缩略图最长边，默认 300 */
    thumbWidth?: number;
    /** 是否公开免登录访问，默认 true（private 需登录） */
    isPublic?: boolean;
    /** 接受的文件类型 */
    accept?: string;
}

interface Dims {
    width: number;
    height: number;
}

/** 弹窗内当前主图（原图或处理结果），缩略图在确定上传时生成 */
interface PreviewResult {
    cUrl: string;
    cFile: File;
    cSize: number;
    cdims: Dims;
}

type Tool = 'crop' | 'auto';

interface FieldUploadImageState {
    maxCount: number;
    thumbWidth: number;
    isPublic: boolean;
    accept: string;

    objectNames: string[];
    modalOpen: boolean;
    originalUrl?: string;
    originalDims?: Dims;
    originalSize: number;
    preview?: PreviewResult;
    tool?: Tool;
    cropperReady: boolean;
    cropRatio: Dims | null;
    uploading: boolean;
    fullPreviewUrl?: string;
    compressWidth: number;
    compressSize: number;
}

function formatSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1024 / 1024).toFixed(2) + ' MB';
}

function gcd(a: number, b: number): number {
    return b === 0 ? a : gcd(b, a % b);
}

/** 比例转最简整数比字符串，如 800:600 → "4:3" */
function formatRatio(w: number, h: number): string {
    const g = gcd(w, h) || 1;
    return `${w / g}:${h / g}`;
}

/** 裁切比例选项：null 表示自由裁切；desc 标注常见场景用途 */
const CROP_RATIOS: Array<{label: string; ratio: Dims | null; desc?: string}> = [
    {label: '自由', ratio: null},
    {label: '1:1', ratio: {width: 1, height: 1}, desc: '方形'},
    {label: '4:3', ratio: {width: 4, height: 3}, desc: '商品图'},
    {label: '3:4', ratio: {width: 3, height: 4}, desc: '竖图'},
    {label: '3:2', ratio: {width: 3, height: 2}, desc: 'Banner'},
    {label: '16:9', ratio: {width: 16, height: 9}, desc: '横幅'},
];

function readDims(url: string): Promise<Dims> {
    return new Promise((resolve) => {
        const img = new Image();
        img.onload = () => resolve({width: img.naturalWidth, height: img.naturalHeight});
        img.onerror = () => resolve({width: 0, height: 0});
        img.src = url;
    });
}

function compressToFile(source: Blob, options: {maxWidth?: number; maxHeight?: number; width?: number; height?: number; quality?: number}): Promise<File> {
    return new Promise((resolve, reject) => {
        new Compressor(source, { // NOSONAR (typescript:S1848) — compressorjs 通过构造函数副作用异步压缩，回调内 resolve/reject，实例无需保留
            maxWidth: options.maxWidth,
            maxHeight: options.maxHeight,
            width: options.width,
            height: options.height,
            quality: options.quality,
            success(result) {
                const file = result instanceof File ? result : new File([result], 'image.jpg', {type: result.type || 'image/jpeg'});
                resolve(file);
            },
            error: reject,
        });
    });
}

/**
 * 压缩到指定体积内：先按 maxWidth 等比缩放，再二分质量逼近目标体积；
 * 若最低质量仍超目标，则进一步缩小宽度。
 */
async function compressToTarget(source: Blob, maxWidth: number | undefined, targetBytes: number): Promise<File> {
    const maxW = maxWidth || undefined;
    if (!targetBytes || targetBytes <= 0) {
        return compressToFile(source, {maxWidth: maxW, maxHeight: maxW});
    }
    // 二分查找满足目标体积的最大质量（JPEG 质量范围 0.1 ~ 1）
    let best = await compressToFile(source, {maxWidth: maxW, maxHeight: maxW, quality: 1});
    if (best.size <= targetBytes) return best;
    let lo = 0.1;
    let hi = 1;
    for (let i = 0; i < 5; i++) {
        const mid = Number(((lo + hi) / 2).toFixed(2));
        const f = await compressToFile(source, {maxWidth: maxW, maxHeight: maxW, quality: mid});
        if (f.size <= targetBytes) {
            best = f;
            lo = mid;
        } else {
            hi = mid;
        }
    }
    // 最低质量仍超目标：缩小宽度重试（最多缩小到原宽 50%）
    let w = maxW;
    while (best.size > targetBytes && w && w > 320) {
        w = Math.round(w * 0.8);
        best = await compressToFile(source, {maxWidth: w, maxHeight: w, quality: 0.1});
    }
    return best;
}

export class FieldUploadImage extends React.Component<FieldUploadImageProps, FieldUploadImageState> {

    state: FieldUploadImageState = {
        // 传入的参数
        maxCount: 1,
        thumbWidth: 300,
        isPublic: true,
        accept: 'image/*',

        // 内部参数
        objectNames: [],
        modalOpen: false,
        originalSize: 0,
        cropperReady: false,
        cropRatio: {width: 4, height: 3},
        uploading: false,
        compressWidth: 1920,
        compressSize: 500 * 1024,
    };

    private imgRef = React.createRef<HTMLImageElement>();
    private cropperRef?: Cropper;
    private selectedFileRef?: File;
    private previewUrlsRef: string[] = [];

    constructor(props: FieldUploadImageProps) {
        super(props);
        ObjectUtils.copyPropertyIfPresent(props, this.state);
        if (props.value) this.state.objectNames = props.value.split(',');
    }

    componentDidUpdate(prevProps: FieldUploadImageProps, prevState: FieldUploadImageState) { // NOSONAR: 顺序同步多个 props 到 state，拆分反而更难读
        const next: Partial<FieldUploadImageState> = {};
        if (this.props.maxCount !== prevProps.maxCount) next.maxCount = this.props.maxCount;
        if (this.props.thumbWidth !== prevProps.thumbWidth) next.thumbWidth = this.props.thumbWidth;
        if (this.props.isPublic !== prevProps.isPublic) next.isPublic = this.props.isPublic;
        if (this.props.accept !== prevProps.accept) next.accept = this.props.accept;

        const prevValue = prevProps.value ?? null;
        const curValue = this.props.value ?? null;
        if (curValue !== prevValue) {
            next.objectNames = curValue ? curValue.split(',') : [];
        }

        if (Object.keys(next).length > 0) this.setState(next as FieldUploadImageState);

        // 进入/退出裁切工具时初始化或销毁 Cropper
        if (prevState.modalOpen !== this.state.modalOpen || prevState.tool !== this.state.tool) {
            this.destroyCropper();
            if (this.state.modalOpen && this.state.tool === 'crop') {
                this.initCropper();
            }
        }

        // 切换裁切比例时仅更新约束，保留当前裁切框位置（避免重建后贴边）
        if (prevState.cropRatio !== this.state.cropRatio && this.cropperRef) {
            if (!this.state.cropRatio) {
                const box = this.cropperRef.getCropBoxData();
                this.cropperRef.setAspectRatio(Number.NaN);
                this.cropperRef.setCropBoxData(box);
            } else {
                this.cropperRef.setAspectRatio(this.state.cropRatio.width / this.state.cropRatio.height);
            }
        }
    }

    componentWillUnmount() {
        if (this.cropperRef) {
            this.cropperRef.destroy();
            this.cropperRef = undefined;
        }
        this.revokePreviewUrls();
    }

    private destroyCropper() {
        if (this.cropperRef) {
            this.cropperRef.destroy();
            this.cropperRef = undefined;
        }
        this.setState({cropperReady: false});
    }

    private initCropper() {
        const el = this.imgRef.current;
        if (!el) return;
        const init = () => {
            if (this.cropperRef) this.cropperRef.destroy();
            this.cropperRef = new Cropper(el, {
                aspectRatio: this.state.cropRatio ? this.state.cropRatio.width / this.state.cropRatio.height : Number.NaN,
                viewMode: 1,
                autoCropArea: 0.85,
                dragMode: 'move',
            });
            this.setState({cropperReady: true});
        };
        if (el.complete) init();
        else el.addEventListener('load', init, {once: true});
    }

    private revokePreviewUrls() {
        this.previewUrlsRef.forEach((u) => URL.revokeObjectURL(u));
        this.previewUrlsRef = [];
    }

    /**
     * 处理源文件为当前主图（不压缩，仅加载画布预览），用于裁切/手动处理后更新画布
     */
    private regenerate = async (file: File) => {
        const cUrl = URL.createObjectURL(file);
        const cdims = await readDims(cUrl);
        this.revokePreviewUrls();
        this.previewUrlsRef = [cUrl];
        this.setState({preview: {cUrl, cFile: file, cSize: file.size, cdims}});
    };

    /**
     * 默认展示原图：不做任何压缩处理
     */
    private loadOriginalPreview = async (file: File, url: string, dims: Dims) => {
        this.revokePreviewUrls();
        this.previewUrlsRef = [];
        this.setState({preview: {cUrl: url, cFile: file, cSize: file.size, cdims: dims}});
    };

    private closeModal = () => {
        this.destroyCropper();
        this.setState({
            modalOpen: false,
            tool: undefined,
            uploading: false,
            originalUrl: undefined,
            originalDims: undefined,
            originalSize: 0,
            preview: undefined,
        });
        if (this.state.originalUrl) URL.revokeObjectURL(this.state.originalUrl);
        this.revokePreviewUrls();
    };

    /** 手动压缩：按用户设定的最大宽度与目标体积压缩当前主图 */
    private applyCompress = async (width?: number, size?: number) => {
        // 优先压缩当前主图（裁切结果），否则用原始文件
        const file = this.state.preview?.cFile || this.selectedFileRef;
        if (!file) return;
        try {
            const cFile = await compressToTarget(file, width, size ?? 0);
            const cUrl = URL.createObjectURL(cFile);
            const cdims = await readDims(cUrl);
            this.revokePreviewUrls();
            this.previewUrlsRef = [cUrl];
            this.setState({preview: {cUrl, cFile, cSize: cFile.size, cdims}, tool: undefined});
            message.success(`已压缩：${cdims.width} x ${cdims.height} / ${formatSize(cFile.size)}`);
        } catch {
            message.error('压缩失败');
        }
    };

    private handleBeforeUpload = async (file: File) => {
        if (this.state.objectNames.length >= this.state.maxCount) {
            message.warning('已达到最大上传数量');
            return Upload.LIST_IGNORE;
        }
        if (!file.type.startsWith('image/')) {
            message.error('请选择图片文件');
            return Upload.LIST_IGNORE;
        }

        this.selectedFileRef = file;
        const url = URL.createObjectURL(file);
        this.setState({originalUrl: url, originalSize: file.size, tool: undefined, modalOpen: true});

        try {
            // EXIF 方向感知地读取原始尺寸
            const bitmap = await createImageBitmap(file, {imageOrientation: 'from-image'});
            const dims = {width: bitmap.width, height: bitmap.height};
            bitmap.close();
            this.setState({originalDims: dims});
            // 默认展示原图，不做压缩处理，用户可点击「压缩」按钮手动压缩
            await this.loadOriginalPreview(file, url, dims);
        } catch {
            message.error('读取图片失败');
            this.closeModal();
            return;
        }

        return Upload.LIST_IGNORE;
    };

    private confirmCrop = () => {
        const cropper = this.cropperRef;
        const file = this.state.preview?.cFile || this.selectedFileRef;
        if (!cropper || !file) return;
        const canvas = cropper.getCroppedCanvas();
        const mime = file.type || 'image/jpeg';
        canvas.toBlob(async (blob) => {
            this.destroyCropper();
            this.setState({tool: undefined});
            if (blob) {
                const croppedFile = new File([blob], 'cropped.jpg', {type: mime});
                await this.regenerate(croppedFile);
            }
        }, mime);
    };

    /** 重置：丢弃所有处理，回到原始图片 */
    private resetImage = () => {
        const file = this.selectedFileRef;
        if (!file) return;
        if (this.state.originalUrl && this.state.originalDims) {
            this.setState({tool: undefined, cropperReady: false});
            this.loadOriginalPreview(file, this.state.originalUrl, this.state.originalDims);
        }
    };

    private handleConfirm = async () => {
        if (!this.state.preview) return;
        this.setState({uploading: true});
        try {
            // 确定时才生成缩略图
            const tFile = await compressToFile(this.state.preview.cFile, {maxWidth: this.state.thumbWidth, maxHeight: this.state.thumbWidth});
            const fd = new FormData();
            fd.append('file', this.state.preview.cFile);
            fd.append('thumb', tFile);
            fd.append('isPublic', String(this.state.isPublic));
            HttpClient.post('admin/sysFile/uploadImage', fd, null, (rs) => {
                this.setState(
                    (prevState) => ({objectNames: [...prevState.objectNames, rs.objectName]}),
                    () => this.props.onChange?.(this.state.objectNames.join(','))
                );
                this.closeModal();
                this.setState({uploading: false});
            }, (e) => {
                message.error(HttpClient.errToMsg(e));
                this.setState({uploading: false});
            });
        } catch (e) {
            message.error(HttpClient.errToMsg(e));
            this.setState({uploading: false});
        }
    };

    private removeImage = (name: string) => {
        this.setState(
            (prevState) => ({objectNames: prevState.objectNames.filter((n) => n !== name)}),
            () => this.props.onChange?.(this.state.objectNames.join(','))
        );
    };

    private renderCanvas = (preview, canvasImg, originalUrl) => {
        if (this.state.tool === 'crop') {
            return <img key="crop-canvas" ref={this.imgRef} src={preview?.cUrl || originalUrl} style={{maxWidth: '100%', maxHeight: '100%'}} alt="待裁切"/>;
        }
        if (canvasImg?.url) {
            return <img src={canvasImg.url} style={{maxWidth: '100%', maxHeight: '100%'}} alt="预览"/>;
        }
        return <div style={{color: '#999'}}>生成中...</div>;
    };

    render() {
        const {objectNames, accept, compressWidth, compressSize, cropRatio, cropperReady, fullPreviewUrl, maxCount, modalOpen, originalUrl, preview, tool, uploading} = this.state;

        // 画布当前显示的图片
        const canvasImg = preview
            ? {url: preview.cUrl, dims: preview.cdims, size: preview.cSize}
            : undefined;

        // 是否需要压缩：当前图片宽度/体积任一超过设定目标
        const needCompress = !!canvasImg?.dims && (
            (compressWidth > 0 && canvasImg.dims.width > compressWidth) ||
            (compressSize > 0 && canvasImg.size > compressSize)
        );

        return (
            <>
                <div>
                    {objectNames.map((name) => (
                        <div key={name} style={{
                            position: 'relative', display: 'inline-block', marginRight: 8, verticalAlign: 'top',
                            width: 80, height: 80, borderRadius: 4, overflow: 'hidden', cursor: 'pointer',
                        }}
                            onMouseEnter={(e) => {
                                const mask = e.currentTarget.querySelector('.oa-field-upload-img-mask') as HTMLElement;
                                if (mask) {
                                    mask.style.opacity = '1';
                                    mask.style.pointerEvents = 'auto';
                                }
                            }}
                            onMouseLeave={(e) => {
                                const mask = e.currentTarget.querySelector('.oa-field-upload-img-mask') as HTMLElement;
                                if (mask) {
                                    mask.style.opacity = '0';
                                    mask.style.pointerEvents = 'none';
                                }
                            }}
                        >
                            <img
                                src={UrlUtils.contextPath(`/file/${name}?thumb=1`)}
                                width={80}
                                height={80}
                                style={{objectFit: 'cover', borderRadius: 4, display: 'block'}}
                                alt={name}
                            />
                            <div style={{
                                position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center',
                                background: 'rgba(0,0,0,0.45)', opacity: 0, transition: 'opacity .3s', borderRadius: 4,
                                pointerEvents: 'none',
                            }} className="oa-field-upload-img-mask">
                                <Space size={12}>
                                    <EyeOutlined
                                        style={{color: '#fff', fontSize: 18, cursor: 'pointer'}}
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            this.setState({fullPreviewUrl: UrlUtils.contextPath(`/file/${name}`)});
                                        }}
                                    />
                                    <DeleteOutlined
                                        style={{color: '#fff', fontSize: 18, cursor: 'pointer'}}
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            this.removeImage(name);
                                        }}
                                    />
                                </Space>
                            </div>
                        </div>
                    ))}
                    {objectNames.length < maxCount && (
                        <Upload accept={accept} showUploadList={false} beforeUpload={this.handleBeforeUpload} multiple={false}>
                            <Button icon={<PlusOutlined/>}>选择图片</Button>
                        </Upload>
                    )}
                </div>

                <Modal
                    open={modalOpen}
                    title="图片处理"
                    width={900}
                    centered
                    okText="确定"
                    cancelText="取消"
                    onOk={this.handleConfirm}
                    onCancel={this.closeModal}
                    confirmLoading={uploading}
                    okButtonProps={{disabled: tool === 'crop' || !preview}}
                    cancelButtonProps={{disabled: tool === 'crop'}}
                    styles={{body: {height: 'calc(100vh - 300px)', minHeight: 400, overflow: 'hidden'}}}
                >
                    <div style={{display: 'flex', gap: 16, height: '100%'}}>
                        {/* 中央画布 + 底部操作按钮 */}
                        <div style={{flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column', gap: 12}}>
                            <div style={{
                                flex: 1, minHeight: 0, display: 'flex', alignItems: 'center', justifyContent: 'center',
                                background: '#f5f5f5', borderRadius: 8, border: '1px dashed #d9d9d9', position: 'relative', overflow: 'hidden',
                            }}>
                                {this.renderCanvas(preview, canvasImg, originalUrl)}
                            </div>
                        </div>

                        {/* 右侧属性栏 */}
                        <div style={{width: 240, flexShrink: 0, borderLeft: '1px solid #f0f0f0', paddingLeft: 16, display: 'flex', flexDirection: 'column', overflow: 'hidden'}}>
                            <div style={{flex: 1, overflowY: 'auto'}}>
                                {/* 工具栏（横排） */}
                                {tool !== 'crop' && (
                                    <Space size={8} style={{marginBottom: 12, display: 'flex'}}>
                                        <Button
                                            onClick={() => {
                                                this.setState({tool: 'crop'});
                                            }}
                                        >裁切</Button>
                                        <Button onClick={this.resetImage}>重置</Button>
                                    </Space>
                                )}
                                {tool !== 'crop' && <Divider style={{margin: '0 0 12px'}}/>}
                                <div style={{fontWeight: 600, marginBottom: 8}}>{tool === 'crop' ? '裁切信息' : '图片信息'}</div>
                                {tool === 'crop' ? (
                                        <Radio.Group
                                            value={cropRatio ? formatRatio(cropRatio.width, cropRatio.height) : 'free'}
                                            onChange={(e) => {
                                                const found = CROP_RATIOS.find((r) => (r.ratio ? `${r.ratio.width}:${r.ratio.height}` : 'free') === e.target.value);
                                                this.setState({cropRatio: found ? found.ratio : null});
                                            }}
                                            style={{display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 12}}
                                        >
                                            {CROP_RATIOS.map((r) => (
                                                <Radio key={r.ratio ? `${r.ratio.width}:${r.ratio.height}` : 'free'} value={r.ratio ? `${r.ratio.width}:${r.ratio.height}` : 'free'}>
                                                    {r.label}{r.desc ? <span style={{color: '#999', fontSize: 12}}>（{r.desc}）</span> : null}
                                                </Radio>
                                            ))}
                                        </Radio.Group>
                                ) : (
                                    <>
                                        {canvasImg && (
                                            <div style={{color: '#666'}}>
                                                <div>尺寸：{canvasImg.dims ? `${canvasImg.dims.width} x ${canvasImg.dims.height}` : '--'}</div>
                                                <div>体积：{formatSize(canvasImg.size)}</div>
                                            </div>
                                        )}

                                        {/* 压缩处理 */}
                                        <Divider style={{margin: '12px 0'}}/>
                                        <div style={{fontWeight: 600, marginBottom: 8}}>压缩处理</div>
                                        <div style={{marginBottom: 8, display: 'flex', alignItems: 'center', gap: 8}}>
                                            <span style={{color: '#666', flexShrink: 0}}>最大宽度</span>
                                            <Select
                                                value={compressWidth}
                                                onChange={(v) => this.setState({compressWidth: v})}
                                                style={{flex: 1}}
                                                options={[
                                                    {label: '不限', value: 0},
                                                    {label: '400 px（logo）', value: 400},
                                                    {label: '640 px（移动端小图）', value: 640},
                                                    {label: '800 px（商品图）', value: 800},
                                                    {label: '960 px（详情页）', value: 960},
                                                    {label: '1280 px（宽屏）', value: 1280},
                                                    {label: '1920 px（网页）', value: 1920},
                                                    {label: '2560 px（超清）', value: 2560},
                                                ]}
                                            />
                                        </div>
                                        <div style={{marginBottom: 12, display: 'flex', alignItems: 'center', gap: 8}}>
                                            <span style={{color: '#666', flexShrink: 0}}>最大体积</span>
                                            <Select
                                                value={compressSize}
                                                onChange={(v) => this.setState({compressSize: v})}
                                                style={{flex: 1}}
                                                options={[
                                                    {label: '不限', value: 0},
                                                    {label: '200 KB（秒开）', value: 200 * 1024},
                                                    {label: '500 KB（流畅）', value: 500 * 1024},
                                                    {label: '1 MB（较快）', value: 1024 * 1024},
                                                    {label: '2 MB（较慢）', value: 2 * 1024 * 1024},
                                                ]}
                                            />
                                        </div>
                                        {needCompress
                                            ? <Button block danger type="primary" onClick={() => this.applyCompress(compressWidth, compressSize)}>推荐压缩</Button>
                                            : <Button block disabled>无需压缩</Button>}
                                    </>
                                )}
                            </div>
                            {tool === 'crop' && (
                                <div style={{display: 'flex', gap: 12, marginTop: 'auto', padding: '12px 16px 16px 0'}}>
                                    <Button style={{flex: 1}} onClick={() => { this.setState({tool: undefined, cropperReady: false}); }}>取消</Button>
                                    <Button style={{flex: 1}} type="primary" onClick={this.confirmCrop} disabled={!cropperReady}>确认裁切</Button>
                                </div>
                            )}
                        </div>
                    </div>
                </Modal>

                <Modal open={!!fullPreviewUrl} title="图片预览" width="70vw" footer={null}
                       onCancel={() => this.setState({fullPreviewUrl: undefined})}>
                    {fullPreviewUrl && <img src={fullPreviewUrl} style={{maxWidth: '100%'}} alt="预览"/>}
                </Modal>
            </>
        );
    }
}

export default FieldUploadImage;
