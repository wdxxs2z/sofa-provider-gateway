package com.wdxxs2z.gateway.bundle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GatewayOperationType {

    String value();

    String name() default "";

    String desc() default "";

}
