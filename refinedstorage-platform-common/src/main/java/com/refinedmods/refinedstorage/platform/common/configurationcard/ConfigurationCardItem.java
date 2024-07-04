package com.refinedmods.refinedstorage.platform.common.configurationcard;

import com.refinedmods.refinedstorage.platform.api.configurationcard.ConfigurationCardTarget;
import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.content.DataComponents;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class ConfigurationCardItem extends Item {
    private static final Component EMPTY_HELP = createTranslation("item", "configuration_card.empty_help");
    private static final Component CONFIGURED_HELP = createTranslation("item", "configuration_card.configured_help");
    private static final Component EMPTY = createTranslation("item", "configuration_card.empty")
        .withStyle(ChatFormatting.GRAY);

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
        final ConfigurationCardState state = stack.get(DataComponents.INSTANCE.getConfigurationCardState());
        if (state == null) {
            stack.set(DataComponents.INSTANCE.getConfigurationCardState(), new ConfigurationCardState(
                blockEntity.getType(),
                createConfigTag(target, ctx.getLevel().registryAccess()),
                target.getUpgradeItems()
            ));
            sendCopiedConfigurationMessage(ctx.getPlayer(), blockEntity.getType());
            return InteractionResult.CONSUME;
        }
        return applyConfiguration(ctx.getPlayer(), blockEntity, target, state, ctx.getLevel().registryAccess());
    }

    private InteractionResult applyConfiguration(
        final Player player,
        final BlockEntity targetBlockEntity,
        final ConfigurationCardTarget target,
        final ConfigurationCardState state,
        final HolderLookup.Provider provider
    ) {
        if (state.blockEntityType() != targetBlockEntity.getType()) {
            return configurationCardIsConfiguredForDifferentType(player, state.blockEntityType());
        }
        target.readConfiguration(state.config(), provider);
        tryTransferUpgrades(player, target, state.upgradeItems());
        targetBlockEntity.setChanged();
        player.sendSystemMessage(createTranslation("item", "configuration_card.applied_configuration"));
        return InteractionResult.SUCCESS;
    }

    private void tryTransferUpgrades(final Player player,
                                     final ConfigurationCardTarget target,
                                     final List<Item> upgradeItems) {
        for (final Item upgradeItem : upgradeItems) {
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

    private CompoundTag createConfigTag(final ConfigurationCardTarget target, final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        target.writeConfiguration(tag, provider);
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
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        final ConfigurationCardState state = stack.get(DataComponents.INSTANCE.getConfigurationCardState());
        if (state == null) {
            lines.add(EMPTY);
            return;
        }
        lines.add(createTranslation(
            "item",
            "configuration_card.configured",
            getConfiguredTypeTranslation(state.blockEntityType()).withStyle(ChatFormatting.WHITE)
        ).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(isActive(stack) ? CONFIGURED_HELP : EMPTY_HELP));
    }

    boolean isActive(final ItemStack stack) {
        return stack.has(DataComponents.INSTANCE.getConfigurationCardState());
    }

    private static MutableComponent getConfiguredTypeTranslation(final BlockEntityType<?> type) {
        final ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        if (typeId == null) {
            return Component.empty();
        }
        return Component.translatable("block." + typeId.getNamespace() + "." + typeId.getPath());
    }
}
