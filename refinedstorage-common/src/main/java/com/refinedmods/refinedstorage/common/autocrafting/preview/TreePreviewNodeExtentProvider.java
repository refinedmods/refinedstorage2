package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreviewNode;
import com.refinedmods.refinedstorage.common.repackage.org.abego.treelayout.NodeExtentProvider;

class TreePreviewNodeExtentProvider implements NodeExtentProvider<TreePreviewNode> {
    @Override
    public double getWidth(final TreePreviewNode node) {
        return 26;
    }

    @Override
    public double getHeight(final TreePreviewNode node) {
        return 26;
    }
}
