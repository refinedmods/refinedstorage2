package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.List;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-beta.4")
public record TreePreview(PreviewType type, @Nullable TreePreviewNode rootNode,
                          List<ResourceAmount> outputsOfPatternWithCycle) {
}
