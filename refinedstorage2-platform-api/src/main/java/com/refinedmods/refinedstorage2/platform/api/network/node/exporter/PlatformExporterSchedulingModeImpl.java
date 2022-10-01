package com.refinedmods.refinedstorage2.platform.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterSchedulingMode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.List;

import net.minecraft.network.chat.Component;

public class PlatformExporterSchedulingModeImpl implements PlatformExporterSchedulingMode {
    private final ExporterSchedulingMode delegate;
    private final Component name;

    public PlatformExporterSchedulingModeImpl(final ExporterSchedulingMode delegate, final Component name) {
        this.delegate = delegate;
        this.name = name;
    }

    @Override
    public void execute(final List<Object> templates,
                        final ExporterTransferStrategy strategy,
                        final Network network,
                        final Actor actor) {
        delegate.execute(templates, strategy, network, actor);
    }

    @Override
    public Component getName() {
        return name;
    }
}
