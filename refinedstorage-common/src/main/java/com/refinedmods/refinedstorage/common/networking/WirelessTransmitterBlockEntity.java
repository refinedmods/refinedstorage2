package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class WirelessTransmitterBlockEntity
    extends AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<WirelessTransmitterData>, BlockEntityWithDrops {
    private static final String TAG_UPGRADES = "upgr";

    private final UpgradeContainer upgradeContainer;

    public WirelessTransmitterBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getWirelessTransmitter(), pos, state, new SimpleNetworkNode(
            Platform.INSTANCE.getConfig().getWirelessTransmitter().getEnergyUsage()
        ));
        this.upgradeContainer = new UpgradeContainer(UpgradeDestinations.WIRELESS_TRANSMITTER,
            (c, upgradeEnergyUsage) -> {
                final long baseUsage = Platform.INSTANCE.getConfig().getWirelessTransmitter().getEnergyUsage();
                mainNetworkNode.setEnergyUsage(baseUsage + upgradeEnergyUsage);
                setChanged();
            });
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final SimpleNetworkNode networkNode) {
        return new WirelessTransmitterNetworkNodeContainer(
            this,
            networkNode,
            "main",
            new WirelessTransmitterConnectionStrategy(this::getBlockState, getBlockPos())
        );
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_UPGRADES, ContainerUtil.write(upgradeContainer, provider));
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_UPGRADES)) {
            ContainerUtil.read(tag.getCompound(TAG_UPGRADES), upgradeContainer, provider);
        }
        super.loadAdditional(tag, provider);
    }

    public Container getUpgradeContainer() {
        return upgradeContainer;
    }

    @Override
    public List<ItemStack> getUpgrades() {
        return upgradeContainer.getUpgrades();
    }

    @Override
    public boolean addUpgrade(final ItemStack upgradeStack) {
        return upgradeContainer.addUpgrade(upgradeStack);
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.WIRELESS_TRANSMITTER);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new WirelessTransmitterContainerMenu(syncId, inventory, this, upgradeContainer);
    }

    @Override
    public WirelessTransmitterData getMenuData() {
        return new WirelessTransmitterData(getRange(), isActive());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, WirelessTransmitterData> getMenuCodec() {
        return WirelessTransmitterData.STREAM_CODEC;
    }

    int getRange() {
        return RefinedStorageApi.INSTANCE.getWirelessTransmitterRangeModifier().modifyRange(upgradeContainer, 0);
    }

    @Override
    public final NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            drops.add(upgradeContainer.getItem(i));
        }
        return drops;
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }

    boolean isActive() {
        return mainNetworkNode.isActive();
    }
}
