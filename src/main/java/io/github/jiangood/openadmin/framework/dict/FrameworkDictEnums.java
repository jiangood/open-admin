package io.github.jiangood.openadmin.framework.dict;

import io.github.jiangood.openadmin.framework.enums.ApproveStatus;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.framework.enums.MaterialType;
import io.github.jiangood.openadmin.framework.enums.Sex;
import io.github.jiangood.openadmin.framework.enums.StatusColor;
import io.github.jiangood.openadmin.framework.enums.YesNo;
import io.github.jiangood.openadmin.modules.system.entity.DataPermType;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FrameworkDictEnums {

    public FrameworkDictEnums(DictEnumRegistry registry) {
        registry.register(ApproveStatus.class);
        registry.register(Sex.class);
        registry.register(YesNo.class);
        registry.register(DataPermType.class);
        registry.register(StatusColor.class);
        registry.register(ArticlePosition.class);
        registry.register(MaterialType.class);
        registry.register(FileStatus.class);
    }
}
