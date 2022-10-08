package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.InterfaceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class InterfaceBlockEntity
    extends AbstractInternalNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements ExtendedMenuProvider {
    private static final String TAG_EXPORT_CONFIG = "ec";
    private static final String TAG_UPGRADES = "u";

    private final ResourceFilterContainer exportConfig;
    private final UpgradeContainer upgradeContainer;

    public InterfaceBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getInterface(),
            pos,
            state,
            new SimpleNetworkNode(Platform.INSTANCE.getConfig().getInterface().getEnergyUsage())
        );
        this.exportConfig = new FilteredResourceFilterContainer(
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            9,
            this::exportConfigChanged,
            ItemResourceType.INSTANCE,
            64
        );
        this.upgradeContainer = new UpgradeContainer(
            1,
            UpgradeDestinations.INTERFACE,
            PlatformApi.INSTANCE.getUpgradeRegistry(),
            this::upgradeContainerChanged
        );
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_UPGRADES, upgradeContainer.createTag());
        tag.put(TAG_EXPORT_CONFIG, exportConfig.toTag());
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_EXPORT_CONFIG)) {
            exportConfig.load(tag.getCompound(TAG_EXPORT_CONFIG));
        }
        initializeExportConfig();
        if (tag.contains(TAG_UPGRADES)) {
            upgradeContainer.fromTag(tag.getList(TAG_UPGRADES, Tag.TAG_COMPOUND));
        }
        configureAccordingToUpgrades();
        super.load(tag);
    }

    private void exportConfigChanged() {
        initializeExportConfig();
        setChanged();
    }

    private void initializeExportConfig() {
        // TODO!
    }

    private void upgradeContainerChanged() {
        configureAccordingToUpgrades();
        setChanged();
    }

    private void configureAccordingToUpgrades() {
        // TODO!
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new InterfaceContainerMenu(syncId, player, exportConfig, upgradeContainer);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        exportConfig.writeToUpdatePacket(buf);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "interface");
    }
}
