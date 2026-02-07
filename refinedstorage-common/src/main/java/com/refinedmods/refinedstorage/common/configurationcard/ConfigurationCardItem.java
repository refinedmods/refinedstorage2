package com.refinedmods.refinedstorage.common.configurationcard;

import com.refinedmods.refinedstorage.common.api.configurationcard.ConfigurationCardTarget;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ConfigurationCardItem extends Item {
    private static final Component EMPTY_HELP = createTranslation("item", "configuration_card.empty_help");
    private static final Component CONFIGURED_HELP = createTranslation("item", "configuration_card.configured_help");
    private static final Component EMPTY = createTranslation("item", "configuration_card.empty")
        .withStyle(ChatFormatting.GRAY);

    public ConfigurationCardItem() {
        super(new Item.Properties().stacksTo(1)
            .setId(ResourceKey.create(Registries.ITEM, ContentIds.CONFIGURATION_CARD)));
    }

    @Override
    public InteractionResult useOn(final UseOnContext ctx) {
        if (!(ctx.getPlayer() instanceof ServerPlayer serverPlayer)) {
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
                target.getUpgrades()
            ));
            sendCopiedConfigurationMessage(serverPlayer, blockEntity.getType());
            return InteractionResult.CONSUME;
        }
        return applyConfiguration(serverPlayer, blockEntity, target, state, ctx.getLevel().registryAccess());
    }

    private InteractionResult applyConfiguration(
        final ServerPlayer player,
        final BlockEntity targetBlockEntity,
        final ConfigurationCardTarget target,
        final ConfigurationCardState state,
        final HolderLookup.Provider registries
    ) {
        if (state.blockEntityType() != targetBlockEntity.getType()) {
            return configurationCardIsConfiguredForDifferentType(player, state.blockEntityType());
        }
        final ValueInput config = TagValueInput.create(ProblemReporter.DISCARDING, registries, state.config());
        target.readConfiguration(config);
        tryTransferUpgrades(player, target, state.upgrades());
        targetBlockEntity.setChanged();
        player.sendSystemMessage(createTranslation("item", "configuration_card.applied_configuration"));
        return InteractionResult.SUCCESS;
    }

    private void tryTransferUpgrades(final Player player,
                                     final ConfigurationCardTarget target,
                                     final List<ItemStack> upgrades) {
        for (final ItemStack upgrade : upgrades) {
            final int upgradeIndexInPlayerInventory = player.getInventory().findSlotMatchingItem(upgrade);
            if (upgradeIndexInPlayerInventory >= 0 && target.addUpgrade(upgrade)) {
                player.getInventory().removeItem(upgradeIndexInPlayerInventory, 1);
            }
        }
    }

    private InteractionResult configurationCardIsConfiguredForDifferentType(
        final ServerPlayer player,
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

    private CompoundTag createConfigTag(final ConfigurationCardTarget target, final HolderLookup.Provider registries) {
        final TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        target.writeConfiguration(output);
        return output.buildResult();
    }

    @SuppressWarnings("resource")
    private void sendCopiedConfigurationMessage(final ServerPlayer player, final BlockEntityType<?> configuredType) {
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
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (player.isCrouching()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(createTranslation("item", "configuration_card.cleared_configuration"));
            }
            return InteractionResult.CONSUME.heldItemTransformedTo(getDefaultInstance());
        }
        return super.use(level, player, hand);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display,
                                final Consumer<Component> builder, final TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
        final ConfigurationCardState state = stack.get(DataComponents.INSTANCE.getConfigurationCardState());
        if (state == null) {
            builder.accept(EMPTY);
            return;
        }
        builder.accept(createTranslation(
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
        final Identifier typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        if (typeId == null) {
            return Component.empty();
        }
        return Component.translatable("block." + typeId.getNamespace() + "." + typeId.getPath());
    }
}
