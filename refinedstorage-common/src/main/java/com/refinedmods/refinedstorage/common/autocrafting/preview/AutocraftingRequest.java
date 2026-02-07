package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

public class AutocraftingRequest {
    private final UUID id;
    private final ResourceKey resource;
    private final double amount;
    @Nullable
    private Preview preview;
    @Nullable
    private TreePreview treePreview;
    private long pendingPreviewAmount;

    private AutocraftingRequest(final UUID id, final ResourceKey resource, final double amount) {
        this.id = id;
        this.resource = resource;
        this.amount = amount;
    }

    public static AutocraftingRequest of(final ResourceAmount resourceAmount) {
        final double displayAmount = resourceAmount.resource() instanceof PlatformResourceKey platformResourceKey
            ? platformResourceKey.getResourceType().getDisplayAmount(resourceAmount.amount())
            : resourceAmount.amount();
        return new AutocraftingRequest(UUID.randomUUID(), resourceAmount.resource(), displayAmount);
    }

    boolean sendPreviewRequest(final double previewAmount, final AutocraftingPreviewStyle style) {
        if (!(resource instanceof PlatformResourceKey resourceKey)) {
            return false;
        }
        final long normalizedAmount = resourceKey.getResourceType().normalizeAmount(previewAmount);
        if (normalizedAmount == pendingPreviewAmount) {
            return false;
        }
        this.preview = null;
        this.pendingPreviewAmount = normalizedAmount;
        C2SPackets.sendAutocraftingPreviewRequest(id, resourceKey, normalizedAmount, style);
        return true;
    }

    void previewResponseReceived(final Preview previewReceived) {
        this.pendingPreviewAmount = 0;
        this.preview = previewReceived;
        this.treePreview = null;
    }

    void previewResponseReceived(final TreePreview previewReceived) {
        this.pendingPreviewAmount = 0;
        this.preview = null;
        this.treePreview = previewReceived;
    }

    void sendRequest(final double amountRequested, final boolean notify) {
        if (!(resource instanceof PlatformResourceKey resourceKey)) {
            return;
        }
        final long normalizedAmount = resourceKey.getResourceType().normalizeAmount(amountRequested);
        C2SPackets.sendAutocraftingRequest(id, resourceKey, normalizedAmount, notify);
    }

    UUID getId() {
        return id;
    }

    ResourceKey getResource() {
        return resource;
    }

    double getAmount() {
        return amount;
    }

    @Nullable
    Preview getPreview() {
        return preview;
    }

    @Nullable
    TreePreview getTreePreview() {
        return treePreview;
    }

    void clearPreview() {
        this.pendingPreviewAmount = 0;
        this.preview = null;
    }
}
