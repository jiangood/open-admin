package io.github.jiangood.sa.common.tools;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlBuilder {

    private StringBuilder sb = new StringBuilder();

    @Getter
    private List<Object> params = new ArrayList<>();

    public void append(String sql) {
        sb.append(sql);
    }
    public void append(String sql, Object... sqlParams) {
        sb.append(sql);
        Collections.addAll(params, sqlParams);
    }
    public void addParams(Object... sqlParams) {
        Collections.addAll(params, sqlParams);
    }

    public String getSql() {
        return sb.toString();
    }

    @Override
    public String toString() {
        return getSql();
    }
}
