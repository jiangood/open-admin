package io.github.jiangood.openadmin.lang.dto.antd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 选项，如下拉多选，单选等
 */
@EqualsAndHashCode(of = "value")
@Getter
@Setter
@NoArgsConstructor
public class Option {
    String label;
    Object value;

    Object data;


    public static Option of(Object value, String label) {
        Option option = new Option();
        option.setValue(value);
        option.setLabel(label);
        return option;
    }


    public static <T> List<Option> convertList(Iterable<T> list, Function<T, Object> valueFn, Function<T, String> labelFn) {
        List<Option> result = new ArrayList<>();
        for (T t : list) {
            String label = labelFn.apply(t);
            Object value = valueFn.apply(t);
            result.add(Option.of(value, label));
        }
        return result;
    }

    public static <T> List<Option> convertList(Iterable<String> list) {
        List<Option> result = new ArrayList<>();
        for (String t : list) {
            result.add(Option.of(t, t));
        }
        return result;
    }

}
