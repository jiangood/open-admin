package io.github.jiangood.openadmin.modules.system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DictItemVO {
    private String id;
    private String code;
    private String label;

    private String color;
    private  Boolean enabled ;
    private Integer seq;


    private String typeCode;

    // 前端使用的唯一id，解决非数据库数据时拼接
    private String uid;

    public DictItemVO(String code, String label, String color) {
        this.code = code;
        this.label = label;
        this.color = color;
    }
}