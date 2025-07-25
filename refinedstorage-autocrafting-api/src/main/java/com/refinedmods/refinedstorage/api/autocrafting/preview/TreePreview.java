package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.List;
import javax.annotation.Nullable;

public record TreePreview(PreviewType type, @Nullable TreePreviewNode rootNode,
                          List<ResourceAmount> outputsOfPatternWithCycle) {
}
