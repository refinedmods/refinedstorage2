package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.api.blockentity.ConfigurationCardTarget;
import com.refinedmods.refinedstorage2.platform.api.item.HelpTooltipComponent;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNull;

// TODO: textures
public class ConfigurationCardItem extends Item {
    private static final Component EMPTY_HELP = createTranslation("item", "configuration_card.empty_help");
    private static final Component CONFIGURED_HELP = createTranslation("item", "configuration_card.configured_help");

    private static final Component EMPTY = createTranslation("item", "configuration_card.empty")
        .withStyle(ChatFormatting.GRAY);

    private static final String TAG_TYPE = "type";
    private static final String TAG_CONFIG = "config";
    private static final String TAG_UPGRADES = "upgrades";

    public ConfigurationCardItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(final UseOnContext ctx) {
        if (ctx.getLevel().isClientSide() || ctx.getPlayer() == null) {
            return InteractionResult.CONSUME;
        }
        final BlockEntity blockEntity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (!(blockEntity instanceof ConfigurationCardTarget target)) {
            return InteractionResult.CONSUME;
        }
        final ItemStack stack = ctx.getItemInHand();
        if (stack.getTag() == null) {
            writeConfiguration(stack, ctx.getPlayer(), target, blockEntity.getType());
            return InteractionResult.CONSUME;
        }
        return applyConfiguration(ctx.getPlayer(), blockEntity, target, stack.getTag());
    }

    private InteractionResult applyConfiguration(
        final Player player,
        final BlockEntity targetBlockEntity,
        final ConfigurationCardTarget target,
        final CompoundTag tag
    ) {
        final BlockEntityType<?> existingConfiguredType = getConfiguredType(tag);
        if (existingConfiguredType != targetBlockEntity.getType()) {
            return configurationCardIsConfiguredForDifferentType(player, existingConfiguredType);
        }
        target.readConfiguration(tag.getCompound(TAG_CONFIG));
        tryTransferUpgrades(player, target, tag);
        targetBlockEntity.setChanged();
        player.sendSystemMessage(createTranslation("item", "configuration_card.applied_configuration"));
        return InteractionResult.SUCCESS;
    }

    private void tryTransferUpgrades(final Player player, final ConfigurationCardTarget target, final CompoundTag tag) {
        final ListTag upgradesTag = tag.getList(TAG_UPGRADES, Tag.TAG_STRING);
        for (final Tag upgradeItemTag : upgradesTag) {
            final ResourceLocation upgradeItemKey = new ResourceLocation(upgradeItemTag.getAsString());
            final Item upgradeItem = BuiltInRegistries.ITEM.get(upgradeItemKey);
            final int upgradeIndexInPlayerInventory = player.getInventory().findSlotMatchingItem(
                new ItemStack(upgradeItem)
            );
            if (upgradeIndexInPlayerInventory >= 0 && target.addUpgradeItem(upgradeItem)) {
                player.getInventory().removeItem(upgradeIndexInPlayerInventory, 1);
            }
        }
    }

    private InteractionResult configurationCardIsConfiguredForDifferentType(
        final Player player,
        @Nullable final BlockEntityType<?> existingConfiguredType
    ) {
        if (existingConfiguredType != null) {
            player.sendSystemMessage(createTranslation(
                "item",
                "configuration_card.cannot_apply_configuration",
                getConfiguredTypeTranslation(existingConfiguredType).withStyle(ChatFormatting.YELLOW)
            ));
        }
        return InteractionResult.CONSUME;
    }

    private void writeConfiguration(final ItemStack stack,
                                    final Player player,
                                    final ConfigurationCardTarget target,
                                    final BlockEntityType<?> type) {
        final CompoundTag tag = new CompoundTag();
        tag.putString(TAG_TYPE, requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type)).toString());
        tag.put(TAG_CONFIG, createConfigTag(target));
        tag.put(TAG_UPGRADES, createUpgradesTag(target));
        stack.setTag(tag);
        sendCopiedConfigurationMessage(player, type);
    }

    private CompoundTag createConfigTag(final ConfigurationCardTarget target) {
        final CompoundTag tag = new CompoundTag();
        target.writeConfiguration(tag);
        return tag;
    }

    private ListTag createUpgradesTag(final ConfigurationCardTarget target) {
        final ListTag tag = new ListTag();
        target.getUpgradeItems().forEach(item -> {
            final ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(item);
            tag.add(StringTag.valueOf(itemKey.toString()));
        });
        return tag;
    }

    private void sendCopiedConfigurationMessage(final Player player, final BlockEntityType<?> configuredType) {
        if (player.level().isClientSide()) {
            return;
        }
        player.sendSystemMessage(createTranslation(
            "item",
            "configuration_card.copied_configuration",
            getConfiguredTypeTranslation(configuredType).withStyle(ChatFormatting.YELLOW)
        ));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        if (player.isCrouching()) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(createTranslation("item", "configuration_card.cleared_configuration"));
            }
            return new InteractionResultHolder<>(InteractionResult.CONSUME, new ItemStack(this));
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                @Nullable final Level level,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        if (stack.getTag() == null) {
            lines.add(EMPTY);
            return;
        }
        final BlockEntityType<?> configuredType = getConfiguredType(stack.getTag());
        if (configuredType == null) {
            return;
        }
        lines.add(createTranslation(
            "item",
            "configuration_card.configured",
            getConfiguredTypeTranslation(configuredType).withStyle(ChatFormatting.WHITE)
        ).withStyle(ChatFormatting.GRAY));
    }

    @Nullable
    private BlockEntityType<?> getConfiguredType(final CompoundTag tag) {
        final ResourceLocation type = new ResourceLocation(tag.getString(TAG_TYPE));
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.get(type);
    }

    private MutableComponent getConfiguredTypeTranslation(final BlockEntityType<?> type) {
        final ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        if (typeId == null) {
            return Component.empty();
        }
        return Component.translatable("block." + typeId.getNamespace() + "." + typeId.getPath());
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(isActive(stack) ? CONFIGURED_HELP : EMPTY_HELP));
    }

    public boolean isActive(final ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(TAG_TYPE);
    }
}
