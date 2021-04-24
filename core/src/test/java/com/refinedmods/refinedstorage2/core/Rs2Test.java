package com.refinedmods.refinedstorage2.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public @interface Rs2Test {
}
