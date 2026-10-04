package io.github.jiangood.openadmin.modules.system.dto.request;

import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import lombok.Data;

@Data
public class ArticleReq {
    String id;
    String code;
    String title;
    String mainImage;
    String content;
    ArticlePosition position;
    Integer seq;
    Boolean enabled;
}
