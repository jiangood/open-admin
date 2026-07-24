package io.github.jiangood.openadmin.framework.auth.dto;

import io.github.jiangood.openadmin.modules.system.entity.Article;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LoginDataVO {
    private boolean login;
    private boolean needUpdatePwd;
    private Object dictInfo;
    private LoginInfoVO loginInfo;
    private Map<String, List<Article>> siteArticles;
}
