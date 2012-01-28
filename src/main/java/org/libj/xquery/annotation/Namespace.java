package org.libj.xquery.annotation;

@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE})
public @interface Namespace {
    String name() default "";
}
