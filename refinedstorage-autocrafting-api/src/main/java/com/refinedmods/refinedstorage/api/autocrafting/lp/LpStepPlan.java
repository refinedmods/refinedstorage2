package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanStep;

import java.util.List;

public record LpStepPlan(List<LpExecutionPlanStep> steps, boolean hasRecipeCycles) {
    public LpStepPlan {
        steps = List.copyOf(steps);
    }
}