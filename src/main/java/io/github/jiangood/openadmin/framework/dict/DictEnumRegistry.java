package io.github.jiangood.openadmin.framework.dict;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DictEnumRegistry {
    private final List<Class<? extends Enum<?>>> enumClasses = new ArrayList<>();

    public void register(Class<? extends Enum<?>> enumClass) {
        enumClasses.add(enumClass);
    }

    public List<Class<? extends Enum<?>>> getAll() {
        return List.copyOf(enumClasses);
    }
}
