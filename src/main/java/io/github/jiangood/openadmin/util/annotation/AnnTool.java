package io.github.jiangood.openadmin.util.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class AnnTool {
    private AnnTool() {
    }


    public static boolean hasAnn(Field field, String annName) {
        Annotation[] anns = field.getAnnotations();
        for (Annotation ann : anns) {
            if (ann.annotationType().getSimpleName().equals(annName)) {
                return true;
            }
        }
        return false;
    }


}
