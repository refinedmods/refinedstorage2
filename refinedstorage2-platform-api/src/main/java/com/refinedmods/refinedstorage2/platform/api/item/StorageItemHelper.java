package com.refinedmods.refinedstorage2.platform.api.item;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongFunction;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class StorageItemHelper {
    private static final String TAG_ID = "id";

    private StorageItemHelper() {
    }

    public static Optional<UUID> getStorageId(final ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null && stack.getTag().hasUUID(TAG_ID)) {
            return Optional.of(stack.getTag().getUUID(TAG_ID));
        }
        return Optional.empty();
    }

    public static void setStorageId(final ItemStack stack, final UUID id) {
        final CompoundTag tag = stack.hasTag() && stack.getTag() != null ? stack.getTag() : new CompoundTag();
        tag.putUUID(TAG_ID, id);
        stack.setTag(tag);
    }

    static Optional<StorageInfo> getInfo(@Nullable final Level level, final ItemStack stack) {
        if (level == null) {
            return Optional.empty();
        }
        return getStorageId(stack).map(PlatformApi.INSTANCE.getStorageRepository(level)::getInfo);
    }

    public static void appendToTooltip(final ItemStack stack,
                                       @Nullable final Level level,
                                       final List<Component> tooltip,
                                       final TooltipFlag context,
                                       final LongFunction<String> quantityFormatter,
                                       final LongFunction<String> stackInfoQuantityFormatter,
                                       final Set<StorageTooltipHelper.TooltipOption> options) {
        getInfo(level, stack).ifPresent(info -> StorageTooltipHelper.appendToTooltip(
                tooltip,
                info.stored(),
                info.capacity(),
                quantityFormatter,
                stackInfoQuantityFormatter,
                options
        ));
        if (context.isAdvanced()) {
            getStorageId(stack).ifPresent(id -> {
                final MutableComponent idComponent = Component.literal(id.toString()).withStyle(ChatFormatting.GRAY);
                tooltip.add(idComponent);
            });
        }
    }

    public static InteractionResultHolder<ItemStack> tryDisassembly(final Level level,
                                                                    final Player player,
                                                                    final ItemStack stack,
                                                                    final ItemStack primaryByproduct,
                                                                    @Nullable final ItemStack secondaryByproduct) {
        if (!(level instanceof ServerLevel) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.fail(stack);
        }

        final Optional<UUID> storageId = getStorageId(stack);
        if (storageId.isEmpty()) {
            return returnByproducts(level, player, primaryByproduct, secondaryByproduct);
        }

        return storageId
                .flatMap(id -> PlatformApi.INSTANCE.getStorageRepository(level).disassemble(id))
                .map(disk -> returnByproducts(level, player, primaryByproduct, secondaryByproduct))
                .orElseGet(() -> InteractionResultHolder.fail(stack));
    }

    private static InteractionResultHolder<ItemStack> returnByproducts(final Level level,
                                                                       final Player player,
                                                                       final ItemStack primaryByproduct,
                                                                       @Nullable final ItemStack secondaryByproduct) {
        tryReturnByproductToInventory(level, player, secondaryByproduct);
        return InteractionResultHolder.success(primaryByproduct);
    }

    private static void tryReturnByproductToInventory(final Level level,
                                                      final Player player,
                                                      @Nullable final ItemStack byproduct) {
        if (byproduct != null && !player.getInventory().add(byproduct.copy())) {
            level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), byproduct));
        }
    }
}
