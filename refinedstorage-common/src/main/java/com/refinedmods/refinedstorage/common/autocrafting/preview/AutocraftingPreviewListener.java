package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;

import javax.annotation.Nullable;

interface AutocraftingPreviewListener {
    void requestChanged(AutocraftingRequest request);

    void previewChanged(@Nullable Preview preview, @Nullable TreePreview treePreview);

    void requestRemoved(AutocraftingRequest request, boolean last);

    void maxAmountReceived(double maxAmount);
}
