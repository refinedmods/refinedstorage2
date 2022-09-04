package com.refinedmods.refinedstorage2.network.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(SetupNetworks.class)
public @interface SetupNetwork {
    long energyStored() default Long.MAX_VALUE;

    long energyCapacity() default Long.MAX_VALUE;

    String id() default "default";
}
