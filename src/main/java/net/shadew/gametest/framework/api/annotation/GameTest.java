package net.shadew.gametest.framework.api.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameTest {

    @Deprecated
    int predelay() default -1;

    @Deprecated
    int timeout() default -1;

    @Deprecated
    boolean required() default true;

    @Deprecated
    int rotation() default 0;

    @Deprecated
    String batch() default "";

    String value() default "";

    @Deprecated
    String template() default "";

    @Deprecated
    String dimension() default "minecraft:overworld";

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Batch {
        String value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Class {
        String value();

        @Deprecated
        int predelay() default 0;

        @Deprecated
        int timeout() default 100;
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Dimension {
        String value();
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Optional {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Required {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Predelay {
        int value();
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Timeout {
        int value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Template {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Rotate {
        int value();
    }
}
