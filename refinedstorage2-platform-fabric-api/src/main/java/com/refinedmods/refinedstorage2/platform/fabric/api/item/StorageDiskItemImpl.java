package com.refinedmods.refinedstorage2.platform.fabric.api.item;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.item.StorageDiskItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// TODO immunity for despawning
// TODO tags in recipes
public abstract class StorageDiskItemImpl extends Item implements StorageDiskItem {
    private static final String TAG_ID = "id";

    protected StorageDiskItemImpl(Properties settings) {
        super(settings);
    }

    @Override
    public Optional<UUID> getDiskId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().hasUUID(TAG_ID)) {
            return Optional.of(stack.getTag().getUUID(TAG_ID));
        }
        return Optional.empty();
    }

    @Override
    public Optional<StorageInfo> getInfo(@Nullable Level world, ItemStack stack) {
        if (world == null) {
            return Optional.empty();
        }
        return getDiskId(stack).map(Rs2PlatformApiFacade.INSTANCE.getStorageRepository(world)::getInfo);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);

        getInfo(world, stack).ifPresent(info -> {
            if (info.capacity() == -1) {
                tooltip.add(Rs2PlatformApiFacade.INSTANCE.createTranslation(
                        "misc",
                        "stored",
                        formatQuantity(info.stored())
                ).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Rs2PlatformApiFacade.INSTANCE.createTranslation(
                        "misc",
                        "stored_with_capacity",
                        formatQuantity(info.stored()),
                        formatQuantity(info.capacity())
                ).withStyle(ChatFormatting.GRAY));
            }
        });

        if (context.isAdvanced()) {
            getDiskId(stack).ifPresent(id -> tooltip.add(new TextComponent(id.toString()).withStyle(ChatFormatting.GRAY)));
        }
    }

    protected String formatQuantity(long qty) {
        return QuantityFormatter.formatWithUnits(qty);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);

        Optional<ItemStack> storagePart = createStoragePart(stack.getCount());

        if (!(world instanceof ServerLevel) || !user.isShiftKeyDown() || storagePart.isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        return getDiskId(stack)
                .flatMap(id -> Rs2PlatformApiFacade.INSTANCE.getStorageRepository(world).disassemble(id))
                .map(disk -> {
                    if (!user.getInventory().add(storagePart.get().copy())) {
                        world.addFreshEntity(new ItemEntity(world, user.getX(), user.getY(), user.getZ(), storagePart.get()));
                    }

                    return InteractionResultHolder.success(createDisassemblyByproduct());
                })
                .orElse(InteractionResultHolder.fail(stack));
    }

    protected abstract Optional<ItemStack> createStoragePart(int count);

    protected abstract Storage<?> createStorage(Level world);

    protected abstract ItemStack createDisassemblyByproduct();

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!world.isClientSide() && !stack.hasTag() && entity instanceof Player) {
            UUID id = UUID.randomUUID();

            Rs2PlatformApiFacade.INSTANCE.getStorageRepository(world).set(id, createStorage(world));

            stack.setTag(new CompoundTag());
            stack.getTag().putUUID(TAG_ID, id);
        }
    }
}
