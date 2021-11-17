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

    protected StorageDiskItemImpl(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<UUID> getDiskId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().hasUUID(TAG_ID)) {
            return Optional.of(stack.getTag().getUUID(TAG_ID));
        }
        return Optional.empty();
    }

    @Override
    public Optional<StorageInfo> getInfo(@Nullable Level level, ItemStack stack) {
        if (level == null) {
            return Optional.empty();
        }
        return getDiskId(stack).map(Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level)::getInfo);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);

        getInfo(level, stack).ifPresent(info -> {
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
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);

        Optional<ItemStack> storagePart = createStoragePart(stack.getCount());

        if (!(level instanceof ServerLevel) || !user.isShiftKeyDown() || storagePart.isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        return getDiskId(stack)
                .flatMap(id -> Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level).disassemble(id))
                .map(disk -> {
                    if (!user.getInventory().add(storagePart.get().copy())) {
                        level.addFreshEntity(new ItemEntity(level, user.getX(), user.getY(), user.getZ(), storagePart.get()));
                    }

                    return InteractionResultHolder.success(createDisassemblyByproduct());
                })
                .orElse(InteractionResultHolder.fail(stack));
    }

    protected abstract Optional<ItemStack> createStoragePart(int count);

    protected abstract Storage<?> createStorage(Level level);

    protected abstract ItemStack createDisassemblyByproduct();

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (!level.isClientSide() && !stack.hasTag() && entity instanceof Player) {
            UUID id = UUID.randomUUID();

            Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level).set(id, createStorage(level));

            stack.setTag(new CompoundTag());
            stack.getTag().putUUID(TAG_ID, id);
        }
    }
}
