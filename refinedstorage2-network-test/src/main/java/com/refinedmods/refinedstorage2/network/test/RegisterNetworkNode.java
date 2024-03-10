package com.refinedmods.refinedstorage2.network.test;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.network.test.nodefactory.NetworkNodeFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(RegisterNetworkNodes.class)
public @interface RegisterNetworkNode {
    Class<? extends NetworkNodeFactory> value();

    Class<? extends NetworkNode> clazz();
}
