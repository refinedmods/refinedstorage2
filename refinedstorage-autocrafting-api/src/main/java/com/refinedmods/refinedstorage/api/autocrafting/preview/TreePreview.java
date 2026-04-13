package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.List;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-beta.4")
public record TreePreview(PreviewType type, @Nullable TreePreviewNode rootNode,
                          List<ResourceAmount> outputsOfPatternWithCycle) {
}
