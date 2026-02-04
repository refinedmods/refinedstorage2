package com.refinedmods.refinedstorage.fabric.cape;

import com.refinedmods.refinedstorage.common.content.ContentIds;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;

public final class TenthAnniversaryCape {
    public static final AttachmentType<Boolean> ATTACHMENT = AttachmentRegistry.create(
        ContentIds.TENTH_ANNIVERSARY_CAPE, builder -> builder
            .initializer(() -> false)
            .syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
            .persistent(Codec.BOOL)
            .copyOnDeath());

    private TenthAnniversaryCape() {
    }
}
