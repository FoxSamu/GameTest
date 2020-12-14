package net.shadew.gametest.testmc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameTest {
    long predelay() default 0;
    int timeout() default 100;
    boolean required() default true;
    int rotation() default 0;
    String batch() default "";
    String structure() default "";
}
