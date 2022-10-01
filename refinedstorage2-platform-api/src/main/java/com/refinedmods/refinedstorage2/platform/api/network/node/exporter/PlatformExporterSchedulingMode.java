package com.refinedmods.refinedstorage2.platform.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterSchedulingMode;

import net.minecraft.network.chat.Component;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface PlatformExporterSchedulingMode extends ExporterSchedulingMode {
    Component getName();
}
