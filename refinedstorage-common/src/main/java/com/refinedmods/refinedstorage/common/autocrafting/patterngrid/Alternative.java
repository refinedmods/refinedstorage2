package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.widget.ExpandCollapseState;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.Identifier;

class Alternative {
    private final Identifier id;
    private final String translationKey;
    private final List<PlatformResourceKey> resources;
    private final List<AlternativeSlot> mainSlots = new ArrayList<>();
    private final List<AlternativeSlot> overflowSlots = new ArrayList<>();
    private final ExpandCollapseState expandCollapseState = new ExpandCollapseState();
    private boolean visible = true;

    Alternative(final Identifier id, final String translationKey, final List<PlatformResourceKey> resources) {
        this.id = id;
        this.translationKey = translationKey;
        this.resources = resources;
    }

    List<AlternativeSlot> getMainSlots() {
        return mainSlots;
    }

    List<AlternativeSlot> getOverflowSlots() {
        return overflowSlots;
    }

    Identifier getId() {
        return id;
    }

    String getTranslationKey() {
        return translationKey;
    }

    List<PlatformResourceKey> getResources() {
        return resources;
    }

    double getExpandedPercentage() {
        return expandCollapseState.getExpandedPercentage();
    }

    boolean toggle() {
        return expandCollapseState.toggle();
    }

    void update() {
        expandCollapseState.updateAnimation();
    }

    boolean isVisible() {
        return visible;
    }

    void setVisible(final boolean visible) {
        this.visible = visible;
    }
}
