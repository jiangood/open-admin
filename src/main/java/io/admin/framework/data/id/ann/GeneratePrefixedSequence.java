package io.admin.framework.data.id.ann;

import io.admin.framework.data.id.impl.PrefixedSequenceGenerator;
import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IdGeneratorType(value = PrefixedSequenceGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface GeneratePrefixedSequence {

    String prefix();

}
