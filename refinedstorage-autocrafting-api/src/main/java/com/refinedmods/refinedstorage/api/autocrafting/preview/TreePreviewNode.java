package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-beta.4")
public record TreePreviewNode(ResourceKey resource,
                              long amount,
                              long toCraft,
                              long available,
                              long missing,
                              List<TreePreviewNode> children) {
}
