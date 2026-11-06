package io.github.jiangood.openadmin.modules.job.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class JobImportReq {

    /**
     * 要导入的任务 key；为空时导入全部未注册任务
     */
    List<String> keys;

}
