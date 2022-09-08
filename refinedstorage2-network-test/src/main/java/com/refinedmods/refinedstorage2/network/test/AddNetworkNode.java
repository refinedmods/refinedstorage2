package com.refinedmods.refinedstorage2.network.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Nonnull
public @interface AddNetworkNode {
    String networkId() default "default";

    Property[] properties() default {};

    @interface Property {
        String key();

        long longValue() default -1;

        boolean boolValue() default false;
    }
}
