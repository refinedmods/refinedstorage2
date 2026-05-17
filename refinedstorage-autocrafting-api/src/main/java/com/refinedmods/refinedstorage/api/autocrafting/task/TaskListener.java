package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface TaskListener {
    TaskListener EMPTY = (pattern, sinkId) -> {
    };

    void receivedExternalIteration(Pattern pattern, ExternalPatternSinkId sinkId);
}
