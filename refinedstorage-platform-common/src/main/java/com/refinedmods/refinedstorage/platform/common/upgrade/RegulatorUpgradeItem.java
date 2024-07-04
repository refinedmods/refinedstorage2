package com.refinedmods.refinedstorage.platform.common.upgrade;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.content.DataComponents;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.SingleAmountData;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerImpl;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class RegulatorUpgradeItem extends AbstractUpgradeItem {
    private static final Component HELP = createTranslation("item", "regulator_upgrade.help");

    public RegulatorUpgradeItem(final UpgradeRegistry registry) {
        super(new Item.Properties(), registry);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            final RegulatorUpgradeState initialState = stack.getOrDefault(
                DataComponents.INSTANCE.getRegulatorUpgradeState(),
                RegulatorUpgradeState.EMPTY
            );
            Platform.INSTANCE.getMenuOpener().openMenu(serverPlayer, new ExtendedMenuProviderImpl(
                createResourceFilterContainer(stack, initialState),
                initialState.amount(),
                newAmount -> setAmount(stack, newAmount),
                PlatformApi.INSTANCE.createInventorySlotReference(player, hand)
            ));
        }
        return InteractionResultHolder.success(stack);
    }

    private ResourceContainer createResourceFilterContainer(final ItemStack stack,
                                                            final RegulatorUpgradeState initialState) {
        final ResourceContainer container = ResourceContainerImpl.createForFilter(1);
        container.setListener(() -> {
            final RegulatorUpgradeState state = stack.getOrDefault(
                DataComponents.INSTANCE.getRegulatorUpgradeState(),
                RegulatorUpgradeState.EMPTY
            );
            final PlatformResourceKey resource = container.getResource(0);
            stack.set(DataComponents.INSTANCE.getRegulatorUpgradeState(), state.withResource(resource));
        });
        initialState.resource().ifPresent(initialResource -> container.set(0, new ResourceAmount(initialResource, 1)));
        return container;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new RegulatorTooltipComponent(HELP, getConfiguredResource(stack)));
    }

    private void setAmount(final ItemStack stack, final double amount) {
        final RegulatorUpgradeState state = stack.getOrDefault(
            DataComponents.INSTANCE.getRegulatorUpgradeState(),
            RegulatorUpgradeState.EMPTY
        );
        stack.set(DataComponents.INSTANCE.getRegulatorUpgradeState(), state.withAmount(amount));
    }

    @Override
    public long getEnergyUsage() {
        return Platform.INSTANCE.getConfig().getUpgrade().getRegulatorUpgradeEnergyUsage();
    }

    @Nullable
    private ResourceAmount getConfiguredResource(final ItemStack stack) {
        final RegulatorUpgradeState state = stack.get(DataComponents.INSTANCE.getRegulatorUpgradeState());
        if (state == null) {
            return null;
        }
        return state.resource().map(resource -> {
            final double amount = state.amount();
            final long normalizedAmount = resource.getResourceType().normalizeAmount(amount);
            return new ResourceAmount(resource, normalizedAmount);
        }).orElse(null);
    }

    public OptionalLong getDesiredAmount(final ItemStack stack, final ResourceKey resource) {
        final RegulatorUpgradeState state = stack.getOrDefault(
            DataComponents.INSTANCE.getRegulatorUpgradeState(),
            RegulatorUpgradeState.EMPTY
        );
        return state.resource().flatMap(configuredResource -> {
            final boolean same = configuredResource.equals(resource);
            if (!same) {
                return Optional.empty();
            }
            return Optional.of(configuredResource.getResourceType());
        }).map(type -> {
            final double amount = state.amount();
            final long normalizedAmount = type.normalizeAmount(amount);
            return OptionalLong.of(normalizedAmount);
        }).orElse(OptionalLong.empty());
    }

    public record RegulatorTooltipComponent(Component helpText, @Nullable ResourceAmount configuredResource)
        implements TooltipComponent {
    }

    private static class ExtendedMenuProviderImpl implements ExtendedMenuProvider<SingleAmountData> {
        private final ResourceContainer resourceContainer;
        private final double amount;
        private final Consumer<Double> amountAcceptor;
        private final SlotReference slotReference;

        private ExtendedMenuProviderImpl(final ResourceContainer resourceContainer,
                                         final double amount,
                                         final Consumer<Double> amountAcceptor,
                                         final SlotReference slotReference) {
            this.resourceContainer = resourceContainer;
            this.amount = amount;
            this.amountAcceptor = amountAcceptor;
            this.slotReference = slotReference;
        }

        @Override
        public SingleAmountData getMenuData() {
            return new SingleAmountData(
                Optional.of(slotReference),
                amount,
                ResourceContainerData.of(resourceContainer)
            );
        }

        @Override
        public StreamEncoder<RegistryFriendlyByteBuf, SingleAmountData> getMenuCodec() {
            return SingleAmountData.STREAM_CODEC;
        }

        @Override
        public Component getDisplayName() {
            return ContentNames.REGULATOR_UPGRADE;
        }

        @Override
        public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
            return new RegulatorUpgradeContainerMenu(
                syncId,
                player,
                resourceContainer,
                amountAcceptor,
                slotReference
            );
        }
    }
}
