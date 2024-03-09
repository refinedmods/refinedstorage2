package com.refinedmods.refinedstorage2.platform.api.support.resource;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public abstract class AbstractResourceType implements ResourceType {
    private final String name;
    private final MutableComponent title;
    private final ResourceLocation textureIdentifier;
    private final int textureX;
    private final int textureY;

    protected AbstractResourceType(final String name,
                                   final MutableComponent title,
                                   final ResourceLocation textureIdentifier,
                                   final int textureX,
                                   final int textureY) {
        this.name = name;
        this.title = title;
        this.textureIdentifier = textureIdentifier;
        this.textureX = textureX;
        this.textureY = textureY;
    }

    @Override
    public MutableComponent getTitle() {
        return title;
    }

    @Override
    public ResourceLocation getTextureIdentifier() {
        return textureIdentifier;
    }

    @Override
    public int getXTexture() {
        return textureX;
    }

    @Override
    public int getYTexture() {
        return textureY;
    }

    @Override
    public String toString() {
        return name;
    }
}
