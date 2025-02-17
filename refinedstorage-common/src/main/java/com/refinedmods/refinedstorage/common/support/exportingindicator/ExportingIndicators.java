package com.refinedmods.refinedstorage.common.support.exportingindicator;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.support.packet.s2c.ExportingIndicatorUpdatePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;

public class ExportingIndicators {
    @Nullable
    private final ResourceContainer filterContainer;
    private final List<ExportingIndicator> indicators;
    private final IntFunction<ExportingIndicator> indicatorProvider;

    public ExportingIndicators(final List<ExportingIndicator> indicators) {
        this.filterContainer = null;
        this.indicators = indicators;
        this.indicatorProvider = i -> ExportingIndicator.NONE;
    }

    public ExportingIndicators(final ResourceContainer filterContainer,
                               final IntFunction<ExportingIndicator> indicatorProvider) {
        this.filterContainer = filterContainer;
        this.indicators = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < filterContainer.size(); ++i) {
            if (filterContainer.isEmpty(i)) {
                indicators.add(ExportingIndicator.NONE);
                continue;
            }
            indicators.add(indicatorProvider.apply(j));
            j++;
        }
        this.indicatorProvider = indicatorProvider;
    }

    public void detectChanges(final ServerPlayer player) {
        if (filterContainer == null) {
            return;
        }
        int j = 0;
        List<ExportingIndicatorUpdatePacket.UpdatedIndicator> updatedIndicators = null;
        for (int i = 0; i < filterContainer.size(); ++i) {
            if (filterContainer.isEmpty(i)) {
                updatedIndicators = tryUpdateIndicator(i, ExportingIndicator.NONE, updatedIndicators);
                continue;
            }
            updatedIndicators = tryUpdateIndicator(i, indicatorProvider.apply(j), updatedIndicators);
            j++;
        }
        if (updatedIndicators != null) {
            S2CPackets.sendExportingIndicatorUpdate(player, updatedIndicators);
        }
    }

    @Nullable
    private List<ExportingIndicatorUpdatePacket.UpdatedIndicator> tryUpdateIndicator(
        final int idx,
        final ExportingIndicator indicator,
        @Nullable final List<ExportingIndicatorUpdatePacket.UpdatedIndicator> updatedIndicators
    ) {
        if (indicators.get(idx) == indicator) {
            return updatedIndicators;
        }
        final List<ExportingIndicatorUpdatePacket.UpdatedIndicator> result = updatedIndicators == null
            ? new ArrayList<>()
            : updatedIndicators;
        result.add(new ExportingIndicatorUpdatePacket.UpdatedIndicator(idx, indicator));
        set(idx, indicator);
        return result;
    }

    public List<ExportingIndicator> getAll() {
        return Collections.unmodifiableList(indicators);
    }

    public ExportingIndicator get(final int idx) {
        return indicators.get(idx);
    }

    public void set(final int idx, final ExportingIndicator indicator) {
        indicators.set(idx, indicator);
    }

    public int size() {
        return indicators.size();
    }
}
