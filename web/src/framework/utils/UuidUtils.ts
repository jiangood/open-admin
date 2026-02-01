/**
 * 基于 https://github.com/jchook/uuid-random 的工作进行封装
 */
export class UuidUtils {
    // 静态属性用于存储随机字节缓冲区和索引
    private static buf: Uint8Array | undefined = undefined;
    private static bufIdx: number = 0;

    // 预计算 toString(16) 以提高速度
    private static readonly hexBytes: string[] = (() => {
        const bytes = new Array<string>(256);
        for (let i = 0; i < 256; i++) {
            // (i + 0x100).toString(16).substring(1) 确保总是两位十六进制
            bytes[i] = (i + 0x100).toString(16).substring(1);
        }
        return bytes;
    })();

    // 缓冲区大小，可调整
    private static readonly BUFFER_SIZE: number = 4096;

    // 随机字节生成函数，使用最佳可用 PRNG
    private static readonly randomBytes = ((n: number): Uint8Array => {
        // Node & Browser 支持
        const lib = typeof crypto !== 'undefined'
            ? crypto
            : (
                typeof window !== 'undefined'
                    ? window.crypto || (window as any).msCrypto // window.msCrypto 在 TypeScript 中可能不存在
                    : undefined
            );

        if (lib !== undefined) {
            // Node.js 支持
            if ((lib as any).randomBytes !== undefined) {
                return (lib as any).randomBytes(n);
            }
// 浏览器支持
            if (lib.getRandomValues !== undefined) {
                return (() => {
                    const bytes = new Uint8Array(n);
                    lib.getRandomValues(bytes);
                    return bytes;
                })();
            }
        }

// 降级使用 Math.random
        const r = new Uint8Array(n);
        for (let i = 0; i < n; i++) {
            r[i] = Math.floor(Math.random() * 256);
        }
        return r;
    });

    /**
     * 生成一个符合 v4 标准的 UUID。
     * @returns 一个 UUID 字符串。
     */
    public static uuidV4(): string {
        // 为加速而缓冲一些随机字节
        if (UuidUtils.buf === undefined || (UuidUtils.bufIdx + 16 > UuidUtils.BUFFER_SIZE)) {
            UuidUtils.bufIdx = 0;
            // randomBytes 的返回类型已确定为 Uint8Array
            UuidUtils.buf = UuidUtils.randomBytes(UuidUtils.BUFFER_SIZE);
        }

        // 提取 16 个字节。使用 Array.prototype.slice.call 转化为 Array 并不能保证性能，
        // 且 TypeScript 中操作 Uint8Array 更直接。我们直接使用 subarray 提取，性能更优。
        const b = UuidUtils.buf.subarray(UuidUtils.bufIdx, (UuidUtils.bufIdx += 16));

        // 设置 UUID 版本 (Version 4: 0100)
        b[6] = (b[6] & 0x0f) | 0x40;
        // 设置时钟序列高位 (Variant 1: 10xx)
        b[8] = (b[8] & 0x3f) | 0x80;

        // 将字节转换为十六进制字符串格式
        const h = UuidUtils.hexBytes;
        return h[b[0]] + h[b[1]]
            + h[b[2]] + h[b[3]] + '-'
            + h[b[4]] + h[b[5]] + '-'
            + h[b[6]] + h[b[7]] + '-'
            + h[b[8]] + h[b[9]] + '-'
            + h[b[10]] + h[b[11]]
            + h[b[12]] + h[b[13]]
            + h[b[14]] + h[b[15]];
    }
}
