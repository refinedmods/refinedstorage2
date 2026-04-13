package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

class Alternative {
    private static final long EXPAND_COLLAPSE_DELAY = 10;

    private final Identifier id;
    private final String translationKey;
    private final List<PlatformResourceKey> resources;
    private final List<AlternativeSlot> mainSlots = new ArrayList<>();
    private final List<AlternativeSlot> overflowSlots = new ArrayList<>();
    private boolean visible = true;
    private double expandPct;
    private int expandCollapseElapsed;
    @Nullable
    private ExpandCollapse expandCollapse;

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

    double getExpandPct() {
        return expandPct;
    }

    boolean expandOrCollapse() {
        if (expandCollapse != null) {
            expandCollapse = expandCollapse == ExpandCollapse.EXPAND ? ExpandCollapse.COLLAPSE : ExpandCollapse.EXPAND;
            expandCollapseElapsed = (int) (EXPAND_COLLAPSE_DELAY - expandCollapseElapsed);
            return expandCollapse == ExpandCollapse.EXPAND;
        }
        expandCollapse = expandPct > 0 ? ExpandCollapse.COLLAPSE : ExpandCollapse.EXPAND;
        return expandCollapse == ExpandCollapse.EXPAND;
    }

    void update() {
        if (expandCollapse == null) {
            return;
        }
        ++expandCollapseElapsed;
        if (expandCollapse == ExpandCollapse.EXPAND) {
            expandPct = (double) expandCollapseElapsed / EXPAND_COLLAPSE_DELAY;
            if (expandPct >= 1) {
                stopExpandCollapse();
            }
        } else {
            expandPct = 1 - (double) expandCollapseElapsed / EXPAND_COLLAPSE_DELAY;
            if (expandPct <= 0) {
                stopExpandCollapse();
            }
        }
    }

    boolean isVisible() {
        return visible;
    }

    void setVisible(final boolean visible) {
        this.visible = visible;
    }

    private void stopExpandCollapse() {
        expandPct = expandCollapse == ExpandCollapse.EXPAND ? 1 : 0;
        expandCollapseElapsed = 0;
        expandCollapse = null;
    }

    private enum ExpandCollapse {
        EXPAND,
        COLLAPSE
    }
}
