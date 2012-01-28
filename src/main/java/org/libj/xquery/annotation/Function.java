package org.libj.xquery.annotation;


@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(value={java.lang.annotation.ElementType.METHOD})
public @interface Function {
    String name() default "";
}
