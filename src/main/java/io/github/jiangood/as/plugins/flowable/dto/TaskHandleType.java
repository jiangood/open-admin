package io.github.jiangood.as.plugins.flowable.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskHandleType {

    APPROVE("同意"), REJECT("不同意"), BACK("退回");

    final String message;

}
