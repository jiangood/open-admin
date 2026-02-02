package io.github.jiangood.openadmin.lang.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObjectReference<T> {

    T current;

    public static <T> ObjectReference<T> createRef() {
        return new ObjectReference<>();
    }


}
