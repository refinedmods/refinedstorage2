package com.refinedmods.refinedstorage2.platform.api.item;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.LongFunction;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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

    public static Optional<UUID> getStorageId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().hasUUID(TAG_ID)) {
            return Optional.of(stack.getTag().getUUID(TAG_ID));
        }
        return Optional.empty();
    }

    public static void setStorageId(ItemStack stack, UUID id) {
        CompoundTag tag = stack.hasTag() ? stack.getTag() : new CompoundTag();
        tag.putUUID(TAG_ID, id);
        stack.setTag(tag);
    }

    static Optional<StorageInfo> getInfo(Level level, ItemStack stack) {
        if (level == null) {
            return Optional.empty();
        }
        return getStorageId(stack).map(Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level)::getInfo);
    }

    public static void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag context, LongFunction<String> quantityFormatter, Consumer<StorageInfo> additionalTooltipAdder) {
        getInfo(level, stack).ifPresent(info -> {
            appendStorageInfoToHoverText(tooltip, info, quantityFormatter);
            additionalTooltipAdder.accept(info);
        });
        if (context.isAdvanced()) {
            getStorageId(stack).ifPresent(id -> tooltip.add(new TextComponent(id.toString()).withStyle(ChatFormatting.GRAY)));
        }
    }

    private static void appendStorageInfoToHoverText(List<Component> tooltip, StorageInfo info, LongFunction<String> quantityFormatter) {
        if (info.capacity() == 0) {
            tooltip.add(Rs2PlatformApiFacade.INSTANCE.createTranslation(
                    "misc",
                    "stored",
                    new TextComponent(quantityFormatter.apply(info.stored())).withStyle(ChatFormatting.GREEN)
            ).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Rs2PlatformApiFacade.INSTANCE.createTranslation(
                    "misc",
                    "stored_with_capacity",
                    new TextComponent(quantityFormatter.apply(info.stored())).withStyle(ChatFormatting.GREEN),
                    new TextComponent(quantityFormatter.apply(info.capacity())).withStyle(ChatFormatting.BLUE)
            ).withStyle(ChatFormatting.GRAY));
        }
    }

    public static void appendStacksHoverText(List<Component> tooltip, StorageInfo info, LongFunction<String> quantityFormatter) {
        long stacksAmount = info.stored() / 64L;
        if (info.capacity() == 0) {
            tooltip.add(Rs2PlatformApiFacade.INSTANCE.createTranslation(
                    "misc",
                    "stacks",
                    new TextComponent(quantityFormatter.apply(stacksAmount)).withStyle(ChatFormatting.GREEN)
            ).withStyle(ChatFormatting.GRAY));
        } else {
            long maxStacksAmount = info.capacity() / 64L;
            tooltip.add(Rs2PlatformApiFacade.INSTANCE.createTranslation(
                    "misc",
                    "stacks_with_capacity",
                    new TextComponent(quantityFormatter.apply(stacksAmount)).withStyle(ChatFormatting.GREEN),
                    new TextComponent(quantityFormatter.apply(maxStacksAmount)).withStyle(ChatFormatting.BLUE)
            ).withStyle(ChatFormatting.GRAY));
        }
    }

    public static InteractionResultHolder<ItemStack> tryDisassembly(Level level,
                                                                    Player player,
                                                                    ItemStack stack,
                                                                    ItemStack primaryDisassemblyByproduct,
                                                                    ItemStack secondaryDisassemblyByproduct) {
        if (!(level instanceof ServerLevel) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.fail(stack);
        }

        Optional<UUID> storageId = getStorageId(stack);
        if (storageId.isEmpty()) {
            return returnByproducts(level, player, primaryDisassemblyByproduct, secondaryDisassemblyByproduct);
        }

        return storageId
                .flatMap(id -> Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level).disassemble(id))
                .map(disk -> returnByproducts(level, player, primaryDisassemblyByproduct, secondaryDisassemblyByproduct))
                .orElseGet(() -> InteractionResultHolder.fail(stack));
    }

    private static InteractionResultHolder<ItemStack> returnByproducts(Level level, Player player, ItemStack primaryDisassemblyByproduct, ItemStack secondaryDisassemblyByproduct) {
        tryReturnByproductToInventory(level, player, secondaryDisassemblyByproduct);
        return InteractionResultHolder.success(primaryDisassemblyByproduct);
    }

    private static void tryReturnByproductToInventory(Level level, Player player, ItemStack byproduct) {
        if (byproduct != null && !player.getInventory().add(byproduct.copy())) {
            level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), byproduct));
        }
    }
}
