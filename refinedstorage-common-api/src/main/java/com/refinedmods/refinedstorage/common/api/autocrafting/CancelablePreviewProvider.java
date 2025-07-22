package com.refinedmods.refinedstorage.common.api.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.beta.3")
public interface CancelablePreviewProvider extends PreviewProvider {
    void cancel();
}
