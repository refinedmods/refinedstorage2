package com.refinedmods.refinedstorage2.platform.common.internal.item;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.item.StorageContainerItemHelper;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.StorageTooltipHelper;

import java.util.List;
import java.util.Optional;
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

public class StorageContainerItemHelperImpl implements StorageContainerItemHelper {
    private static final String TAG_ID = "id";

    @Override
    public Optional<Storage<?>> resolve(final StorageRepository storageRepository, final ItemStack stack) {
        return getId(stack).flatMap(storageRepository::get);
    }

    @Override
    public void set(final StorageRepository storageRepository, final ItemStack stack, final Storage<?> storage) {
        final UUID id = UUID.randomUUID();
        setId(stack, id);
        storageRepository.set(id, storage);
    }

    @Override
    public void setId(final ItemStack stack, final UUID id) {
        final CompoundTag tag = stack.hasTag() && stack.getTag() != null ? stack.getTag() : new CompoundTag();
        tag.putUUID(TAG_ID, id);
        stack.setTag(tag);
    }

    @Override
    public Optional<StorageInfo> getInfo(final StorageRepository storageRepository, final ItemStack stack) {
        return getId(stack).map(storageRepository::getInfo);
    }

    @Override
    public InteractionResultHolder<ItemStack> tryDisassembly(final Level level,
                                                             final Player player,
                                                             final ItemStack stack,
                                                             final ItemStack primaryByproduct,
                                                             @Nullable final ItemStack secondaryByproduct) {
        if (!(level instanceof ServerLevel) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.fail(stack);
        }

        final Optional<UUID> storageId = getId(stack);
        if (storageId.isEmpty()) {
            return returnByproducts(level, player, primaryByproduct, secondaryByproduct);
        }

        return storageId
            .flatMap(id -> PlatformApi.INSTANCE.getStorageRepository(level).removeIfEmpty(id))
            .map(disk -> returnByproducts(level, player, primaryByproduct, secondaryByproduct))
            .orElseGet(() -> InteractionResultHolder.fail(stack));
    }

    private InteractionResultHolder<ItemStack> returnByproducts(final Level level,
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

    @Override
    public void appendToTooltip(final ItemStack stack,
                                final StorageRepository storageRepository,
                                final List<Component> tooltip,
                                final TooltipFlag context,
                                final LongFunction<String> amountFormatter,
                                final boolean hasCapacity) {
        getInfo(storageRepository, stack).ifPresent(info -> {
            if (hasCapacity) {
                StorageTooltipHelper.addAmountStoredWithCapacity(
                    tooltip,
                    info.stored(),
                    info.capacity(),
                    amountFormatter
                );
            } else {
                StorageTooltipHelper.addAmountStoredWithoutCapacity(
                    tooltip,
                    info.stored(),
                    amountFormatter
                );
            }
        });
        if (context.isAdvanced()) {
            getId(stack).ifPresent(id -> {
                final MutableComponent idComponent = Component.literal(id.toString()).withStyle(ChatFormatting.GRAY);
                tooltip.add(idComponent);
            });
        }
    }

    @Override
    public Optional<UUID> getId(final ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null && stack.getTag().hasUUID(TAG_ID)) {
            return Optional.of(stack.getTag().getUUID(TAG_ID));
        }
        return Optional.empty();
    }
}
