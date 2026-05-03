package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.energy.TransferableBlockEntityEnergy;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.energy.BlockEntityEnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.ItemBlockEnergyStorage;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ControllerBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<ControllerNetworkNode>
    implements NetworkNodeExtendedMenuProvider<ControllerData>, TransferableBlockEntityEnergy {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerBlockEntity.class);

    private static final String TAG_CAPACITY = "capacity";

    private final ControllerType type;
    private final EnergyStorage energyStorage;
    private final RateLimiter energyStateChangeRateLimiter = RateLimiter.create(1);

    public ControllerBlockEntity(final ControllerType type, final BlockPos pos, final BlockState state) {
        super(getBlockEntityType(type), pos, state, new ControllerNetworkNode());
        this.type = type;
        this.energyStorage = createEnergyStorage(type, this);
        this.mainNetworkNode.setEnergyStorage(energyStorage);
    }

    private static EnergyStorage createEnergyStorage(final ControllerType type, final BlockEntity blockEntity) {
        if (type == ControllerType.CREATIVE) {
            return CreativeEnergyStorage.INSTANCE;
        }
        return new BlockEntityEnergyStorage(
            new EnergyStorageImpl(
                Math.clamp(Platform.INSTANCE.getConfig().getController().getEnergyCapacity(), 1, Long.MAX_VALUE)
            ),
            blockEntity
        );
    }

    private static BlockEntityType<ControllerBlockEntity> getBlockEntityType(final ControllerType type) {
        return type == ControllerType.CREATIVE
            ? BlockEntities.INSTANCE.getCreativeController()
            : BlockEntities.INSTANCE.getController();
    }

    public void updateEnergyTypeInLevel(final BlockState state) {
        final ControllerEnergyType currentEnergyType = state.getValue(AbstractControllerBlock.ENERGY_TYPE);
        final ControllerEnergyType newEnergyType = ControllerEnergyType.ofState(mainNetworkNode.getState());
        if (newEnergyType != currentEnergyType && level != null && energyStateChangeRateLimiter.tryAcquire()) {
            LOGGER.debug(
                "Energy type state change for controller at {}: {} -> {}",
                getBlockPos(),
                currentEnergyType,
                newEnergyType
            );
            level.setBlockAndUpdate(getBlockPos(), state.setValue(AbstractControllerBlock.ENERGY_TYPE, newEnergyType));
        }
    }

    @Override
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        ItemBlockEnergyStorage.store(output, mainNetworkNode.getActualStored());
        saveRenderingInfo(output);
    }

    private void saveRenderingInfo(final ValueOutput output) {
        output.putLong(TAG_CAPACITY, mainNetworkNode.getActualCapacity());
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        ItemBlockEnergyStorage.read(energyStorage, input);
    }

    @Override
    public Component getName() {
        final Component defaultName = type == ControllerType.CREATIVE
            ? ContentNames.CREATIVE_CONTROLLER
            : ContentNames.CONTROLLER;
        return overrideName(defaultName);
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inv, final Player player) {
        return new ControllerContainerMenu(syncId, inv, this, player);
    }

    @Override
    public ControllerData getMenuData() {
        return new ControllerData(getActualStored(), getActualCapacity(), getNodeEnergyBreakdown());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, ControllerData> getMenuCodec() {
        return ControllerData.STREAM_CODEC;
    }

    long getActualStored() {
        return mainNetworkNode.getActualStored();
    }

    long getActualCapacity() {
        return mainNetworkNode.getActualCapacity();
    }

    public record NodeEnergyEntry(String name, long usage, int count, ItemStack icon, String translatedName) {}

    public List<NodeEnergyEntry> getNodeEnergyBreakdown() {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return List.of();
        }

        record Aggregate(long[] usage, int[] count, ItemStack icon, String translatedName) {}
        final Map<String, Aggregate> aggregates = new LinkedHashMap<>();

        network.getComponent(GraphNetworkComponent.class)
                .getContainers()
                .stream()
                .filter(InWorldNetworkNodeContainer.class::isInstance)
                .map(InWorldNetworkNodeContainer.class::cast)
                .forEach(c -> {
                    if (!(c.getNode() instanceof AbstractNetworkNode node)) return;
                    final long usage = node.getEnergyUsage();
                    if (usage <= 0) return;

                    final BlockEntity be = c.getBlockEntity();
                    final var key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
                    final String name = key != null ? key.getPath() : "unknown";

                    aggregates.compute(name, (k, existing) -> {
                        if (existing != null) {
                            existing.usage()[0] += usage;
                            existing.count()[0]++;
                            return existing;
                        }
                        final BlockState state = be.getBlockState();
                        final Item item = state.getBlock().asItem();
                        final ItemStack icon = item != Items.AIR ? new ItemStack(item) : ItemStack.EMPTY;
                        final String translatedName = state.getBlock().getName().getString();
                        return new Aggregate(new long[]{usage}, new int[]{1}, icon, translatedName);
                    });
                });

        return aggregates.entrySet().stream()
                .map(e -> {
                    final Aggregate a = e.getValue();
                    return new NodeEnergyEntry(e.getKey(), a.usage()[0], a.count()[0], a.icon(), a.translatedName());
                })
                .toList();
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}
