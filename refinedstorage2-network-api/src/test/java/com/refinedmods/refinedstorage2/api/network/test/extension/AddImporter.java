package com.refinedmods.refinedstorage2.api.network.test.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AddImporter {
    long energyUsage() default 0L;

    long coolDownTime() default 0L;

    String networkId() default "default";
}
