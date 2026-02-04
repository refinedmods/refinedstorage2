package com.refinedmods.refinedstorage.neoforge.cape;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.neoforged.neoforge.attachment.AttachmentType;

import static java.util.Objects.requireNonNull;

public final class TenthAnniversaryCape {
    @Nullable
    private static Supplier<AttachmentType<Boolean>> attachment;

    private TenthAnniversaryCape() {
    }

    public static void setAttachment(final Supplier<AttachmentType<Boolean>> att) {
        attachment = att;
    }

    public static AttachmentType<Boolean> getAttachment() {
        return requireNonNull(attachment).get();
    }
}
