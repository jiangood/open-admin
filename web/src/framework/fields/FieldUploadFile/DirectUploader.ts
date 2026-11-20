import {HttpClient} from "../../utils/HttpClient";
import {UrlUtils} from "../../utils/UrlUtils";

export interface DirectUploadOptions {
    file: File;
    isPublic?: boolean;
    onProgress?: (percent: number) => void;
    /** 单次直传 PUT 或代理上传的 XHR/流程可选取消支持预留 */
    signal?: AbortSignal;
}

/**
 * 前端直传上传器（类组件/无 hooks 场景可用的普通工具类）。
 * 流程：prepare -> (PUT 或 分片上传) -> complete；不支持直传或直传失败时回退服务端代理上传。
 * 仅负责字节上传与完成确认，返回与 /admin/sys-file/upload 兼容的 AjaxResult 结构。
 */
export class DirectUploader {

    static async upload(opts: DirectUploadOptions): Promise<any> {
        const {file, isPublic = true, onProgress} = opts;
        try {
            const prepareRs = await HttpClient.post<any>('admin/sys-file/direct-upload/prepare', {
                originName: file.name,
                size: file.size,
                isPublic,
                contentType: file.type || undefined,
            });
            const info = prepareRs?.data || {};
            if (!info.supported) {
                return await DirectUploader.uploadByProxy(file, isPublic, onProgress);
            }
            if (info.mode === 'multipart') {
                return await DirectUploader.uploadMultipart(file, info, onProgress);
            }
            return await DirectUploader.uploadPut(file, info, onProgress);
        } catch (e) {
            // prepare/complete 等框架请求失败：若已是 AjaxError 直接抛出；直传链路异常可尝试代理回退
            // 这里保守处理：若是 S3 PUT 阶段抛出的普通 Error，尝试回退代理上传
            if (e && typeof e === 'object' && (e as any).__directPutFailed) {
                return await DirectUploader.uploadByProxy(file, isPublic, onProgress);
            }
            throw e;
        }
    }

    private static async uploadPut(file: File, info: any, onProgress?: (p: number) => void): Promise<any> {
        const headers = DirectUploader.filterHeaders(info.headers);
        // 若签名未限定 Content-Type，按文件类型补充（无害）
        if (file.type && !DirectUploader.hasHeader(headers, 'content-type')) {
            headers['Content-Type'] = file.type;
        }
        try {
            await DirectUploader.xhrPut(info.uploadUrl, file, headers, onProgress);
        } catch (e) {
            const err: any = e instanceof Error ? e : new Error('直传失败');
            err.__directPutFailed = true;
            throw err;
        }
        const completeRs = await HttpClient.post<any>('admin/sys-file/direct-upload/complete', {
            objectName: info.objectName,
        });
        return DirectUploader.toUploadResult(completeRs, info);
    }

    private static async uploadMultipart(file: File, info: any, onProgress?: (p: number) => void): Promise<any> {
        const partSize: number = Number(info.partSize) || 5 * 1024 * 1024;
        const total = file.size;
        const totalParts = Math.max(1, Math.ceil(total / partSize));
        const parts: Array<{partNumber: number; etag: string}> = [];
        let uploadedBytes = 0;

        try {
            for (let partNumber = 1; partNumber <= totalParts; partNumber++) {
                const start = (partNumber - 1) * partSize;
                const end = Math.min(start + partSize, total);
                const blob = file.slice(start, end);

                const partRs = await HttpClient.post<any>('admin/sys-file/direct-upload/multipart/part-url', {
                    objectName: info.objectName,
                    uploadId: info.uploadId,
                    partNumber,
                });
                const pinfo = partRs?.data || {};
                const headers = DirectUploader.filterHeaders(pinfo.headers);

                const etag = await DirectUploader.xhrPut(pinfo.uploadUrl, blob, headers, (partPercent) => {
                    if (!onProgress) return;
                    const current = uploadedBytes + (blob.size * partPercent) / 100;
                    onProgress(Math.min(99, Math.floor((current / total) * 100)));
                });
                if (!etag) {
                    throw new Error('分片上传未返回 ETag');
                }
                parts.push({partNumber, etag: etag.replace(/"/g, '')});
                uploadedBytes += blob.size;
                if (onProgress) {
                    onProgress(Math.min(99, Math.floor((uploadedBytes / total) * 100)));
                }
            }

            const completeRs = await HttpClient.post<any>('admin/sys-file/direct-upload/multipart/complete', {
                objectName: info.objectName,
                uploadId: info.uploadId,
                parts,
            });
            if (onProgress) onProgress(100);
            return DirectUploader.toUploadResult(completeRs, info);
        } catch (e) {
            // 尽力终止未完成的分片上传
            try {
                await HttpClient.post('admin/sys-file/direct-upload/multipart/abort', {
                    objectName: info.objectName,
                    uploadId: info.uploadId,
                }, null, {autoShowErrorMessage: false});
            } catch (_) {
                // ignore
            }
            // 若是 S3 侧 PUT 失败，尝试回退代理上传（记录已清理/标记为待删，新代理上传会生成新文件）
            if (e && typeof e === 'object' && (e as any).__directPutFailed) {
                return await DirectUploader.uploadByProxy(file, info.isPublic ?? true, onProgress);
            }
            throw e;
        }
    }

    /**
     * 服务端代理上传（兼容 LOCAL 或直传不可用/失败场景）。
     */
    static async uploadByProxy(file: File, isPublic: boolean = true, onProgress?: (p: number) => void): Promise<any> {
        const url = UrlUtils.contextPath('/admin/sys-file/upload');
        const fd = new FormData();
        fd.append('file', file);
        fd.append('isPublic', String(isPublic));

        return await new Promise<any>((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            xhr.open('POST', url, true);
            xhr.withCredentials = true;
            xhr.upload.onprogress = (evt) => {
                if (!onProgress || !evt.lengthComputable) return;
                onProgress(Math.floor((evt.loaded / evt.total) * 100));
            };
            xhr.onerror = () => reject(new Error('上传失败：网络错误'));
            xhr.onload = () => {
                let body: any = null;
                try {
                    body = JSON.parse(xhr.responseText || '{}');
                } catch {
                    reject(new Error('上传失败：响应解析错误'));
                    return;
                }
                if (xhr.status >= 200 && xhr.status < 300 && body && body.success) {
                    resolve(body);
                } else if (xhr.status === 401) {
                    // 交给全局登录失效处理约定：模拟 AjaxResult 失败结构由上层处理较复杂，
                    // 这里直接 reject，调用方可按需提示；多数场景会话仍有效
                    reject(body || new Error(body?.message || '登录已过期'));
                } else {
                    reject(body || new Error(body?.message || '上传失败'));
                }
            };
            xhr.send(fd);
        });
    }

    private static xhrPut(url: string, data: Blob, headers: Record<string, string>, onProgress?: (p: number) => void): Promise<string | null> {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            xhr.open('PUT', url, true);
            // 预签名 URL 自带鉴权，不应携带站点 cookie
            xhr.withCredentials = false;
            for (const [k, v] of Object.entries(headers || {})) {
                try {
                    xhr.setRequestHeader(k, v);
                } catch {
                    // 忽略浏览器禁止设置的头
                }
            }
            xhr.upload.onprogress = (evt) => {
                if (!onProgress || !evt.lengthComputable) return;
                onProgress(Math.floor((evt.loaded / evt.total) * 100));
            };
            xhr.onerror = () => {
                const err: any = new Error('直传失败：网络错误或跨域配置异常');
                err.__directPutFailed = true;
                reject(err);
            };
            xhr.onload = () => {
                if (xhr.status >= 200 && xhr.status < 300) {
                    resolve(xhr.getResponseHeader('ETag'));
                } else {
                    const err: any = new Error(`直传失败：HTTP ${xhr.status}`);
                    err.__directPutFailed = true;
                    reject(err);
                }
            };
            xhr.send(data);
        });
    }

    private static toUploadResult(completeRs: any, info: any): any {
        // 统一为 antd Upload 期望的 AjaxResult 结构
        const data = (completeRs && completeRs.data) || {};
        return {
            success: true,
            code: 200,
            message: completeRs?.message,
            data: {
                objectName: data.objectName || info.objectName,
                name: data.name || info.name || info.originName,
            },
            location: data.location || info.location || completeRs?.location,
        };
    }

    private static filterHeaders(headers: any): Record<string, string> {
        const out: Record<string, string> = {};
        if (!headers) return out;
        const forbidden = new Set(['host', 'content-length']);
        for (const [k, v] of Object.entries(headers)) {
            if (v == null) continue;
            if (forbidden.has(String(k).toLowerCase())) continue;
            out[String(k)] = String(v);
        }
        return out;
    }

    private static hasHeader(headers: Record<string, string>, name: string): boolean {
        const n = name.toLowerCase();
        return Object.keys(headers).some(k => k.toLowerCase() === n);
    }
}
