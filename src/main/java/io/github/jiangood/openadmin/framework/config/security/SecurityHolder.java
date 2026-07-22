package io.github.jiangood.openadmin.framework.config.security;

import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Setter
@Component
public class SecurityHolder {

    private   Map<Class<?>, Object> sharedObjects = new HashMap<>();


    @SuppressWarnings("unchecked")
    public <C> C getSharedObject(Class<C> sharedType) {
        return (C) this.sharedObjects.get(sharedType);
    }

}
