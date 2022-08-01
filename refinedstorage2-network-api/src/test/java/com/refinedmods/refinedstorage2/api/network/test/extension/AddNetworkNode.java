package com.refinedmods.refinedstorage2.api.network.test.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Nonnull
public @interface AddNetworkNode {
    long energyUsage() default 0L;

    boolean active() default true;

    String networkId() default "default";
}
