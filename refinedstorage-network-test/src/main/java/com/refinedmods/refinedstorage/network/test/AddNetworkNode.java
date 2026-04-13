package com.refinedmods.refinedstorage.network.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AddNetworkNode {
    String networkId() default "default";

    Property[] properties() default {};

    @interface Property {
        String key();

        int intValue() default -1;

        long longValue() default -1;

        boolean boolValue() default false;
    }
}
