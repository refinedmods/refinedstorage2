package com.refinedmods.refinedstorage.network.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(SetupNetworks.class)
public @interface SetupNetwork {
    long energyStored() default Long.MAX_VALUE;

    long energyCapacity() default Long.MAX_VALUE;

    boolean setupEnergy() default true;

    String id() default "default";
}
