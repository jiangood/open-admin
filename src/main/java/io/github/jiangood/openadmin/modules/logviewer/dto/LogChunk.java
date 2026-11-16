package io.github.jiangood.openadmin.modules.logviewer.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 日志文件增量读取结果。
 *
 * @deprecated 框架前端已改为 raw 全文读取，本分块增量协议仅为兼容保留，后续版本可能移除。
 */
@Deprecated(since = "3.3.1", forRemoval = false)
@Getter
@Setter
public class LogChunk {

    /**
     * 本次读到的内容（按 UTF-8 字符边界切分，不会出现半个汉字）
     */
    private String content = "";

    /**
     * 下次读取应传入的 offset（本次读取结束的字节位置）
     */
    private long nextOffset;

    /**
     * 是否已读到当前文件末尾；文件不存在时为 true
     */
    private boolean eof;
}
