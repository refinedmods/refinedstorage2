package com.refinedmods.refinedstorage.platform.neoforge.importer;

import com.refinedmods.refinedstorage.api.network.impl.node.importer.ImporterSource;
import com.refinedmods.refinedstorage.api.network.impl.node.importer.ImporterTransferStrategyImpl;
import com.refinedmods.refinedstorage.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage.platform.api.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.platform.common.content.Items;
import com.refinedmods.refinedstorage.platform.neoforge.storage.CapabilityCacheImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class ItemHandlerImporterTransferStrategyFactory implements ImporterTransferStrategyFactory {
    @Override
    public ImporterTransferStrategy create(final ServerLevel level,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final UpgradeState upgradeState,
                                           final AmountOverride amountOverride) {
        final ImporterSource source = new ItemHandlerImporterSource(new CapabilityCacheImpl(
            level,
            pos,
            direction
        ), amountOverride);
        final int transferQuota = upgradeState.has(Items.INSTANCE.getStackUpgrade()) ? 64 : 1;
        return new ImporterTransferStrategyImpl(source, transferQuota);
    }
}
