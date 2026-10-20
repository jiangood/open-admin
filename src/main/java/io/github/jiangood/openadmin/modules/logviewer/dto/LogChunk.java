package io.github.jiangood.openadmin.modules.logviewer.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 日志文件增量读取结果。
 */
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
