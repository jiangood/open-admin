import React, {useCallback, useEffect, useRef, useState} from "react";
import {Alert, Button, Modal, Space, Upload, message} from "antd";
import {DeleteOutlined, PlusOutlined, ScissorOutlined} from "@ant-design/icons";
import Compressor from "compressorjs";
import Cropper from "cropperjs";
import "cropperjs/dist/cropper.css";
import {HttpUtils} from "../../utils";
import {UrlUtils} from "../../utils";
import type {FieldProps} from '../types';

interface FieldUploadImageProps extends FieldProps<string> {
    /** 最大上传数量，默认 1 */
    maxCount?: number;
    /** 压缩图目标宽度（强制目标比例），默认 800 */
    targetWidth?: number;
    /** 压缩图目标高度（强制目标比例），默认 600 */
    targetHeight?: number;
    /** 缩略图最长边，默认 300 */
    thumbWidth?: number;
    /** 压缩质量，默认 0.8 */
    quality?: number;
    /** 文件可见性，默认 public */
    visibility?: 'public' | 'private';
    /** 接受的文件类型 */
    accept?: string;
}

interface Dims {
    width: number;
    height: number;
}

/** 弹窗内预览结果：压缩图 + 缩略图 */
interface PreviewResult {
    cUrl: string;
    tUrl: string;
    cFile: File;
    tFile: File;
    cSize: number;
    tSize: number;
    cdims: Dims;
    tdims: Dims;
}

function formatSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1024 / 1024).toFixed(2) + ' MB';
}

function readDims(url: string): Promise<Dims> {
    return new Promise((resolve) => {
        const img = new Image();
        img.onload = () => resolve({width: img.naturalWidth, height: img.naturalHeight});
        img.onerror = () => resolve({width: 0, height: 0});
        img.src = url;
    });
}

function compressToFile(source: Blob, maxWidth: number, maxHeight: number, quality: number): Promise<File> {
    return new Promise((resolve, reject) => {
        new Compressor(source, {
            maxWidth,
            maxHeight,
            quality,
            success(result) {
                const file = result instanceof File ? result : new File([result], 'image.jpg', {type: result.type || 'image/jpeg'});
                resolve(file);
            },
            error: reject,
        });
    });
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
    const [needsCrop, setNeedsCrop] = useState(false);
    const [cropperActive, setCropperActive] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [fullPreviewUrl, setFullPreviewUrl] = useState<string>();

    const imgRef = useRef<HTMLImageElement>(null);
    const cropperRef = useRef<Cropper>();
    const selectedFileRef = useRef<File>();
    const previewUrlsRef = useRef<string[]>([]);

    const targetRatio = targetWidth / targetHeight;

    // 父组件 value 变化时同步
    useEffect(() => {
        const parsed = value ? value.split(',') : [];
        setObjectNames(parsed);
    }, [value]);

    const revokePreviewUrls = useCallback(() => {
        previewUrlsRef.current.forEach((u) => URL.revokeObjectURL(u));
        previewUrlsRef.current = [];
    }, []);

    const closeModal = useCallback(() => {
        if (cropperRef.current) {
            cropperRef.current.destroy();
            cropperRef.current = undefined;
        }
        setModalOpen(false);
        setCropperActive(false);
        setNeedsCrop(false);
        setUploading(false);
        setOriginalUrl(undefined);
        setOriginalDims(undefined);
        setOriginalSize(0);
        setPreview(undefined);
        if (originalUrl) URL.revokeObjectURL(originalUrl);
        revokePreviewUrls();
    }, [originalUrl, revokePreviewUrls]);

    /**
     * 基于源文件生成压缩图 + 缩略图（含各自尺寸/大小），用于弹窗展示
     */
    const regenerate = useCallback(async (source: Blob) => {
        const cFile = await compressToFile(source, targetWidth, targetHeight, quality);
        const tFile = await compressToFile(cFile, thumbWidth, thumbWidth, quality);
        const cUrl = URL.createObjectURL(cFile);
        const tUrl = URL.createObjectURL(tFile);
        const cdims = await readDims(cUrl);
        const tdims = await readDims(tUrl);
        revokePreviewUrls();
        previewUrlsRef.current = [cUrl, tUrl];
        setPreview({cUrl, tUrl, cFile, tFile, cSize: cFile.size, tSize: tFile.size, cdims, tdims});
    }, [quality, revokePreviewUrls, targetHeight, targetWidth, thumbWidth]);

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
        setModalOpen(true);
        setCropperActive(false);

        try {
            // EXIF 方向感知地读取原始尺寸
            const bitmap = await createImageBitmap(file, {imageOrientation: 'from-image'});
            const dims = {width: bitmap.width, height: bitmap.height};
            bitmap.close();
            setOriginalDims(dims);
            const ratioDiff = Math.abs(dims.width / dims.height - targetRatio);
            setNeedsCrop(ratioDiff / targetRatio > 0.02);
        } catch (e) {
            message.error('读取图片失败');
            closeModal();
            return;
        }

        await regenerate(file);
        return Upload.LIST_IGNORE;
    }, [closeModal, maxCount, objectNames.length, regenerate, targetRatio]);

    const startCrop = useCallback(() => {
        setCropperActive(true);
        setTimeout(() => {
            const el = imgRef.current;
            if (!el) return;
            if (cropperRef.current) cropperRef.current.destroy();
            cropperRef.current = new Cropper(el, {
                aspectRatio: targetRatio,
                viewMode: 1,
                autoCropArea: 1,
                dragMode: 'move',
            });
        }, 0);
    }, [targetRatio]);

    const confirmCrop = useCallback(() => {
        const cropper = cropperRef.current;
        const file = selectedFileRef.current;
        if (!cropper || !file) return;
        const canvas = cropper.getCroppedCanvas();
        const mime = file.type || 'image/jpeg';
        canvas.toBlob(async (blob) => {
            if (cropperRef.current) {
                cropperRef.current.destroy();
                cropperRef.current = undefined;
            }
            setCropperActive(false);
            setNeedsCrop(false);
            if (blob) {
                const croppedFile = new File([blob], 'cropped.jpg', {type: mime});
                await regenerate(croppedFile);
            }
        }, mime, quality);
    }, [quality, regenerate]);

    const cancelCrop = useCallback(() => {
        if (cropperRef.current) {
            cropperRef.current.destroy();
            cropperRef.current = undefined;
        }
        setCropperActive(false);
    }, []);

    const handleConfirm = useCallback(async () => {
        if (needsCrop || !preview) return;
        setUploading(true);
        try {
            const fd = new FormData();
            fd.append('file', preview.cFile);
            fd.append('thumb', preview.tFile);
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
    }, [closeModal, needsCrop, objectNames, onChange, preview, visibility]);

    const removeImage = useCallback((name: string) => {
        const newNames = objectNames.filter((n) => n !== name);
        setObjectNames(newNames);
        onChange?.(newNames.join(','));
    }, [objectNames, onChange]);

    const renderPanel = (title: string, url: string | undefined, dims: Dims | undefined, size: number | undefined) => (
        <div style={{flex: 1, textAlign: 'center'}}>
            <div style={{fontWeight: 600, marginBottom: 8}}>{title}</div>
            {url ? (
                <img src={url} style={{maxWidth: '100%', maxHeight: 160, display: 'block', margin: '0 auto'}}/>
            ) : (
                <div style={{height: 120, lineHeight: '120px', color: '#999'}}>生成中...</div>
            )}
            <div style={{color: '#666', marginTop: 8}}>
                {dims ? `${dims.width} x ${dims.height}` : '--'}
                {size != null && size > 0 && <span> · {formatSize(size)}</span>}
            </div>
        </div>
    );

    return (
        <>
            <div>
                {objectNames.map((name) => (
                    <div key={name} style={{position: 'relative', display: 'inline-block', marginRight: 8, verticalAlign: 'top'}}>
                        <img
                            src={UrlUtils.contextPath(`/file/${name}?thumb=1`)}
                            width={80}
                            height={80}
                            style={{objectFit: 'cover', borderRadius: 4, cursor: 'pointer'}}
                            onClick={() => setFullPreviewUrl(UrlUtils.contextPath(`/file/${name}`))}
                            alt={name}
                        />
                        <Button
                            size="small"
                            danger
                            icon={<DeleteOutlined/>}
                            style={{position: 'absolute', top: -8, right: -8}}
                            onClick={() => removeImage(name)}
                        />
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
                title="图片预览"
                width={900}
                okText="确定"
                cancelText="取消"
                onOk={handleConfirm}
                onCancel={closeModal}
                confirmLoading={uploading}
                okButtonProps={{disabled: needsCrop}}
            >
                {cropperActive ? (
                    <div>
                        {originalUrl && <img ref={imgRef} src={originalUrl} style={{maxWidth: '100%'}} alt="待裁切"/>}
                        <Space style={{marginTop: 12}}>
                            <Button type="primary" onClick={confirmCrop}>确认裁切</Button>
                            <Button onClick={cancelCrop}>取消裁切</Button>
                        </Space>
                    </div>
                ) : (
                    <>
                        {needsCrop && (
                            <Alert type="warning" showIcon
                                   message="图片比例与目标尺寸不符，请点击下方「裁切」按钮按比例处理后再确定"
                                   style={{marginBottom: 12}}/>
                        )}
                        <div style={{display: 'flex', gap: 16}}>
                            {renderPanel('原图', originalUrl, originalDims, originalSize)}
                            {renderPanel('压缩图', preview?.cUrl, preview?.cdims, preview?.cSize)}
                            {renderPanel('缩略图', preview?.tUrl, preview?.tdims, preview?.tSize)}
                        </div>
                        <Button icon={<ScissorOutlined/>} onClick={startCrop} style={{marginTop: 12}}>裁切</Button>
                    </>
                )}
            </Modal>

            <Modal open={!!fullPreviewUrl} title="图片预览" width="70vw" footer={null}
                   onCancel={() => setFullPreviewUrl(undefined)}>
                {fullPreviewUrl && <img src={fullPreviewUrl} style={{maxWidth: '100%'}} alt="预览"/>}
            </Modal>
        </>
    );
}

export default FieldUploadImage;
