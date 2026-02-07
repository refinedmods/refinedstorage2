package com.refinedmods.refinedstorage.common.api.storage;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public abstract class AbstractStorageContainerItem extends Item implements StorageContainerItem {
    protected final StorageContainerItemHelper helper;

    protected AbstractStorageContainerItem(final Properties properties, final StorageContainerItemHelper helper) {
        super(properties);
        this.helper = helper;
    }

    @Override
    public Optional<SerializableStorage> resolve(final StorageRepository storageRepository, final ItemStack stack) {
        return helper.resolveStorage(storageRepository, stack);
    }

    @Override
    public Optional<StorageInfo> getInfo(final StorageRepository storageRepository, final ItemStack stack) {
        return helper.getInfo(storageRepository, stack);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        final ItemStack primaryByproduct = createPrimaryDisassemblyByproduct(stack.getCount());
        final ItemStack secondaryByproduct = createSecondaryDisassemblyByproduct(stack.getCount());
        return helper.tryDisassembly(level, player, stack, primaryByproduct, secondaryByproduct);
    }

    @Override
    public void inventoryTick(final ItemStack stack, final ServerLevel level, final Entity entity,
                              @Nullable final EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        helper.transferStorageIfNecessary(stack, level, entity, this::createStorage);
        helper.loadStorageIfNecessary(stack, level, entity, this::createStorage);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final TooltipDisplay display,
                                final Consumer<Component> builder,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        final StorageRepository storageRepository = RefinedStorageApi.INSTANCE.getClientStorageRepository();
        helper.appendToTooltip(stack, storageRepository, builder, flag, this::formatAmount, getCapacity());
    }

    @Nullable
    protected abstract Long getCapacity();

    protected abstract String formatAmount(long amount);

    protected abstract SerializableStorage createStorage(StorageRepository storageRepository);

    protected abstract ItemStack createPrimaryDisassemblyByproduct(int count);

    @Nullable
    protected abstract ItemStack createSecondaryDisassemblyByproduct(int count);
}
