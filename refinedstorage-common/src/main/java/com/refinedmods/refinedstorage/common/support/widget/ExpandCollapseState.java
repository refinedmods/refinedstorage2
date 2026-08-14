package com.refinedmods.refinedstorage.common.support.widget;

import org.jspecify.annotations.Nullable;

public class ExpandCollapseState {
    private static final long DELAY = 5;

    private double expandedPct;
    private int elapsed;
    @Nullable
    private ExpandCollapse animationState;

    public double getExpandedPercentage() {
        return expandedPct;
    }

    public boolean isExpanded() {
        return expandedPct > 0;
    }

    public void expand() {
        expandedPct = 1;
        elapsed = 0;
        animationState = null;
    }

    public boolean toggle() {
        if (animationState != null) {
            animationState = animationState == ExpandCollapse.EXPAND ? ExpandCollapse.COLLAPSE : ExpandCollapse.EXPAND;
            elapsed = (int) (DELAY - elapsed);
            return animationState == ExpandCollapse.EXPAND;
        }
        animationState = expandedPct > 0 ? ExpandCollapse.COLLAPSE : ExpandCollapse.EXPAND;
        return animationState == ExpandCollapse.EXPAND;
    }

    public boolean updateAnimation() {
        if (animationState == null) {
            return false;
        }
        ++elapsed;
        if (animationState == ExpandCollapse.EXPAND) {
            expandedPct = (double) elapsed / DELAY;
            if (expandedPct >= 1) {
                resetAnimation();
            }
        } else {
            expandedPct = 1 - (double) elapsed / DELAY;
            if (expandedPct <= 0) {
                resetAnimation();
            }
        }
        return true;
    }

    private void resetAnimation() {
        expandedPct = animationState == ExpandCollapse.EXPAND ? 1 : 0;
        elapsed = 0;
        animationState = null;
    }

    private enum ExpandCollapse {
        EXPAND,
        COLLAPSE
    }
}
