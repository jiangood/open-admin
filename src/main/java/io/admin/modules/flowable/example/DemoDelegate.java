package io.admin.modules.flowable.example;

import io.admin.common.utils.annotation.Remark;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;


@Remark("demo-Java处理类")
@Component
public class DemoDelegate implements JavaDelegate {



    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("demo-Java处理类执行....");
    }
}
