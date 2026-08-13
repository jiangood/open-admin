import React, {useCallback, useEffect, useRef, useState} from "react";
import {Button, Divider, Modal, Radio, Select, Space, Upload, message} from "antd";
import {DeleteOutlined, EyeOutlined, PlusOutlined} from "@ant-design/icons";
import Compressor from "compressorjs";
import Cropper from "cropperjs";
import "cropperjs/dist/cropper.css";
import {HttpUtils} from "../../utils";
import {UrlUtils} from "../../utils";
import type {FieldProps} from '../types';

interface FieldUploadImageProps extends FieldProps<string> {
    /** 最大上传数量，默认 1 */
    maxCount?: number;
    /** 目标尺寸（默认 800x600），裁切框与最终压缩均按此比例 */
    targetWidth?: number;
    /** 目标尺寸（默认 800x600） */
    targetHeight?: number;
    /** 缩略图最长边，默认 300 */
    thumbWidth?: number;
    /** 压缩质量，默认 0.8 */
    quality?: number;
    /** 文件可见性，默认 public */
    visibility?: 'public' | 'private';
    /** 接受的文件类型 */
    accept?: string;
    /** 超过该宽度/高度提示尺寸过大，默认 1920x1080 */
    maxWebWidth?: number;
    /** 超过该高度提示尺寸过大，默认 1920x1080 */
    maxWebHeight?: number;
    /** 超过该存储大小提示过大，默认 2MB */
    maxStorageSize?: number;
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
        new Compressor(source, {
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
 * 压缩到指定体积内：先按 maxWidth 等比缩放，再二分 quality 逼近目标体积；
 * 若最低质量仍超目标，则进一步缩小宽度。
 */
async function compressToTarget(source: Blob, maxWidth: number | undefined, targetBytes: number, quality: number): Promise<File> {
    const maxW = maxWidth || undefined;
    if (!targetBytes || targetBytes <= 0) {
        return compressToFile(source, {maxWidth: maxW, maxHeight: maxW, quality});
    }
    // 尝试给定质量
    let best = await compressToFile(source, {maxWidth: maxW, maxHeight: maxW, quality});
    if (best.size <= targetBytes) return best;
    // 二分查找满足目标的最大质量
    let lo = 0.1;
    let hi = quality;
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

export function FieldUploadImage(props: FieldUploadImageProps) {
    const {
        value, onChange, maxCount = 1, targetWidth = 800, targetHeight = 600,
        thumbWidth = 300, quality = 0.8, visibility = 'public', accept = 'image/*',
    } = props;

    const [objectNames, setObjectNames] = useState<string[]>(() => (value ? value.split(',') : []));
    const [modalOpen, setModalOpen] = useState(false);
    const [originalUrl, setOriginalUrl] = useState<string>();
    const [originalDims, setOriginalDims] = useState<Dims>();
    const [originalSize, setOriginalSize] = useState(0);
    const [preview, setPreview] = useState<PreviewResult>();
    const [tool, setTool] = useState<Tool>();
    const [cropperReady, setCropperReady] = useState(false);
    const [cropRatio, setCropRatio] = useState<Dims | null>({width: targetWidth, height: targetHeight});
    const [uploading, setUploading] = useState(false);
    const [fullPreviewUrl, setFullPreviewUrl] = useState<string>();
    const [compressWidth, setCompressWidth] = useState<number>(1920);
    const [compressSize, setCompressSize] = useState<number>(500 * 1024);

    const imgRef = useRef<HTMLImageElement>(null);
    const cropperRef = useRef<Cropper>();
    const selectedFileRef = useRef<File>();
    const previewUrlsRef = useRef<string[]>([]);

    // 父组件 value 变化时同步
    useEffect(() => {
        const parsed = value ? value.split(',') : [];
        setObjectNames(parsed);
    }, [value]);

    const revokePreviewUrls = useCallback(() => {
        previewUrlsRef.current.forEach((u) => URL.revokeObjectURL(u));
        previewUrlsRef.current = [];
    }, []);

    /**
     * 处理源文件为当前主图（仅压缩，不生成缩略图），用于裁切/自动处理后更新画布
     */
    const regenerate = useCallback(async (source: Blob) => {
        const cFile = await compressToFile(source, {maxWidth: targetWidth, maxHeight: targetHeight, quality});
        const cUrl = URL.createObjectURL(cFile);
        const cdims = await readDims(cUrl);
        revokePreviewUrls();
        previewUrlsRef.current = [cUrl];
        setPreview({cUrl, cFile, cSize: cFile.size, cdims});
    }, [quality, revokePreviewUrls, targetHeight, targetWidth]);

    /**
     * 默认展示原图：不做任何压缩处理
     */
    const loadOriginalPreview = useCallback(async (file: File, url: string, dims: Dims) => {
        revokePreviewUrls();
        previewUrlsRef.current = [];
        setPreview({cUrl: url, cFile: file, cSize: file.size, cdims: dims});
    }, [revokePreviewUrls]);

    const closeModal = useCallback(() => {
        if (cropperRef.current) {
            cropperRef.current.destroy();
            cropperRef.current = undefined;
        }
        setModalOpen(false);
        setTool(undefined);
        setCropperReady(false);
        setUploading(false);
        setOriginalUrl(undefined);
        setOriginalDims(undefined);
        setOriginalSize(0);
        setPreview(undefined);
        if (originalUrl) URL.revokeObjectURL(originalUrl);
        revokePreviewUrls();
    }, [originalUrl, revokePreviewUrls]);

    /** 手动压缩：按用户设定的最大宽度与目标体积压缩当前主图 */
    const applyCompress = useCallback(async (width?: number, size?: number) => {
        // 优先压缩当前主图（裁切结果），否则用原始文件
        const file = preview?.cFile || selectedFileRef.current;
        if (!file) return;
        try {
            const cFile = await compressToTarget(file, width, size, quality);
            const cUrl = URL.createObjectURL(cFile);
            const cdims = await readDims(cUrl);
            revokePreviewUrls();
            previewUrlsRef.current = [cUrl];
            setPreview({cUrl, cFile, cSize: cFile.size, cdims});
            setTool(undefined);
            message.success(`已压缩：${cdims.width} x ${cdims.height} / ${formatSize(cFile.size)}`);
        } catch (e) {
            message.error('压缩失败');
        }
    }, [preview, quality, revokePreviewUrls]);

    const handleBeforeUpload = useCallback(async (file: File) => {
        if (objectNames.length >= maxCount) {
            message.warning('已达到最大上传数量');
            return Upload.LIST_IGNORE;
        }
        if (!file.type.startsWith('image/')) {
            message.error('请选择图片文件');
            return Upload.LIST_IGNORE;
        }

        selectedFileRef.current = file;
        const url = URL.createObjectURL(file);
        setOriginalUrl(url);
        setOriginalSize(file.size);
        setTool(undefined);
        setModalOpen(true);

        try {
            // EXIF 方向感知地读取原始尺寸
            const bitmap = await createImageBitmap(file, {imageOrientation: 'from-image'});
            const dims = {width: bitmap.width, height: bitmap.height};
            bitmap.close();
            setOriginalDims(dims);
            // 默认展示原图，不做压缩处理，用户可点击「压缩」按钮手动压缩
            await loadOriginalPreview(file, url, dims);
        } catch (e) {
            message.error('读取图片失败');
            closeModal();
            return;
        }

        return Upload.LIST_IGNORE;
    }, [closeModal, loadOriginalPreview, maxCount, objectNames.length]);

    // 进入裁切工具时初始化 Cropper
    useEffect(() => {
        if (!modalOpen || tool !== 'crop') return;
        const el = imgRef.current;
        if (!el) return;
        const init = () => {
            if (cropperRef.current) cropperRef.current.destroy();
            cropperRef.current = new Cropper(el, {
                aspectRatio: cropRatio ? cropRatio.width / cropRatio.height : NaN,
                viewMode: 1,
                autoCropArea: 0.85,
                dragMode: 'move',
            });
            setCropperReady(true);
        };
        if (el.complete) init();
        else el.addEventListener('load', init, {once: true});
        return () => {
            if (cropperRef.current) {
                cropperRef.current.destroy();
                cropperRef.current = undefined;
            }
            setCropperReady(false);
        };
    }, [modalOpen, tool]);

    // 切换比例时仅更新约束，保留当前裁切框位置（避免重建后贴边）
    useEffect(() => {
        if (!cropperRef.current) return;
        if (!cropRatio) {
            // 切到自由：保持当前裁切框大小不变
            const box = cropperRef.current.getCropBoxData();
            cropperRef.current.setAspectRatio(NaN);
            cropperRef.current.setCropBoxData(box);
        } else {
            cropperRef.current.setAspectRatio(cropRatio.width / cropRatio.height);
        }
    }, [cropRatio]);

    const confirmCrop = useCallback(async () => {
        const cropper = cropperRef.current;
        const file = preview?.cFile || selectedFileRef.current;
        if (!cropper || !file) return;
        const canvas = cropper.getCroppedCanvas();
        const mime = file.type || 'image/jpeg';
        canvas.toBlob(async (blob) => {
            if (cropperRef.current) {
                cropperRef.current.destroy();
                cropperRef.current = undefined;
            }
            setTool(undefined);
            setCropperReady(false);
            if (blob) {
                const croppedFile = new File([blob], 'cropped.jpg', {type: mime});
                await regenerate(croppedFile);
            }
        }, mime, quality);
    }, [preview, quality, regenerate]);

    /** 重置：丢弃所有处理，回到原始图片 */
    const resetImage = useCallback(() => {
        const file = selectedFileRef.current;
        if (!file) return;
        if (originalUrl && originalDims) {
            setTool(undefined);
            setCropperReady(false);
            loadOriginalPreview(file, originalUrl, originalDims);
        }
    }, [loadOriginalPreview, originalDims, originalUrl]);

    const handleConfirm = useCallback(async () => {
        if (!preview) return;
        setUploading(true);
        try {
            // 确定时才生成缩略图
            const tFile = await compressToFile(preview.cFile, {maxWidth: thumbWidth, maxHeight: thumbWidth, quality});
            const fd = new FormData();
            fd.append('file', preview.cFile);
            fd.append('thumb', tFile);
            fd.append('visibility', visibility);
            const rs = await HttpUtils.post('admin/sysFile/uploadImage', fd, null, {headers: {'Content-Type': 'multipart/form-data'}});
            const newNames = [...objectNames, rs.objectName];
            setObjectNames(newNames);
            onChange?.(newNames.join(','));
            closeModal();
        } catch (e) {
            message.error(HttpUtils.extractErrorMessage(e));
        } finally {
            setUploading(false);
        }
    }, [closeModal, objectNames, onChange, preview, quality, thumbWidth, visibility]);

    const removeImage = useCallback((name: string) => {
        const newNames = objectNames.filter((n) => n !== name);
        setObjectNames(newNames);
        onChange?.(newNames.join(','));
    }, [objectNames, onChange]);

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
                                        setFullPreviewUrl(UrlUtils.contextPath(`/file/${name}`));
                                    }}
                                />
                                <DeleteOutlined
                                    style={{color: '#fff', fontSize: 18, cursor: 'pointer'}}
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        removeImage(name);
                                    }}
                                />
                            </Space>
                        </div>
                    </div>
                ))}
                {objectNames.length < maxCount && (
                    <Upload accept={accept} showUploadList={false} beforeUpload={handleBeforeUpload} multiple={false}>
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
                onOk={handleConfirm}
                onCancel={closeModal}
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
                            {tool === 'crop' ? (
                                <img key="crop-canvas" ref={imgRef} src={preview?.cUrl || originalUrl} style={{maxWidth: '100%', maxHeight: '100%'}} alt="待裁切"/>
                            ) : canvasImg?.url ? (
                                <img src={canvasImg.url} style={{maxWidth: '100%', maxHeight: '100%'}} alt="预览"/>
                            ) : (
                                <div style={{color: '#999'}}>生成中...</div>
                            )}
                        </div>
                    </div>

                    {/* 右侧属性栏 */}
                    <div style={{width: 240, flexShrink: 0, borderLeft: '1px solid #f0f0f0', paddingLeft: 16, display: 'flex', flexDirection: 'column', overflow: 'hidden'}}>
                        <div style={{flex: 1, overflowY: 'auto'}}>
                            {/* 工具栏（横排） */}
                            {tool !== 'crop' && (
                                <Space size={8} style={{marginBottom: 12, display: 'flex'}}>
                                    <Button
                                        type={tool === 'crop' ? 'primary' : 'default'}
                                        onClick={() => {
                                            setTool('crop');
                                        }}
                                    >裁切</Button>
                                    <Button onClick={resetImage}>重置</Button>
                                </Space>
                            )}
                            {tool !== 'crop' && <Divider style={{margin: '0 0 12px'}}/>}
                            <div style={{fontWeight: 600, marginBottom: 8}}>{tool === 'crop' ? '裁切信息' : '图片信息'}</div>
                            {tool === 'crop' ? (
                                <>
                                    <Radio.Group
                                        value={cropRatio ? formatRatio(cropRatio.width, cropRatio.height) : 'free'}
                                        onChange={(e) => {
                                            const found = CROP_RATIOS.find((r) => (r.ratio ? `${r.ratio.width}:${r.ratio.height}` : 'free') === e.target.value);
                                            setCropRatio(found ? found.ratio : null);
                                        }}
                                        style={{display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 12}}
                                    >
                                        {CROP_RATIOS.map((r) => (
                                            <Radio key={r.ratio ? `${r.ratio.width}:${r.ratio.height}` : 'free'} value={r.ratio ? `${r.ratio.width}:${r.ratio.height}` : 'free'}>
                                                {r.label}{r.desc ? <span style={{color: '#999', fontSize: 12}}>（{r.desc}）</span> : null}
                                            </Radio>
                                        ))}
                                    </Radio.Group>
                                </>
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
                                            onChange={setCompressWidth}
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
                                            onChange={setCompressSize}
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
                                        ? <Button block danger type="primary" onClick={() => applyCompress(compressWidth, compressSize)}>推荐压缩</Button>
                                        : <Button block disabled>无需压缩</Button>}
                                </>
                            )}
                        </div>
                        {tool === 'crop' && (
                            <div style={{display: 'flex', gap: 12, marginTop: 'auto', padding: '12px 16px 16px 0'}}>
                                <Button style={{flex: 1}} onClick={() => { setTool(undefined); setCropperReady(false); }}>取消</Button>
                                <Button style={{flex: 1}} type="primary" onClick={confirmCrop} disabled={!cropperReady}>确认裁切</Button>
                            </div>
                        )}
                    </div>
                </div>
            </Modal>

            <Modal open={!!fullPreviewUrl} title="图片预览" width="70vw" footer={null}
                   onCancel={() => setFullPreviewUrl(undefined)}>
                {fullPreviewUrl && <img src={fullPreviewUrl} style={{maxWidth: '100%'}} alt="预览"/>}
            </Modal>
        </>
    );
}

export default FieldUploadImage;