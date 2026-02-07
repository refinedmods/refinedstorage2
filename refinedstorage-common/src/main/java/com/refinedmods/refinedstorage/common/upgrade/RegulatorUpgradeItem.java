package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeMapping;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.containermenu.SingleAmountData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class RegulatorUpgradeItem extends AbstractUpgradeItem {
    private static final Component HELP = createTranslation("item", "regulator_upgrade.help");

    public RegulatorUpgradeItem(final UpgradeRegistry registry) {
        super(new Item.Properties().setId(
                net.minecraft.resources.ResourceKey.create(Registries.ITEM, ContentIds.REGULATOR_UPGRADE)),
            registry, HELP);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            final RegulatorUpgradeState initialState = stack.getOrDefault(
                DataComponents.INSTANCE.getRegulatorUpgradeState(),
                RegulatorUpgradeState.EMPTY
            );
            Platform.INSTANCE.getMenuOpener().openMenu(serverPlayer, new ExtendedMenuProviderImpl(
                stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME),
                createResourceFilterContainer(stack, initialState),
                initialState.amount(),
                newAmount -> setAmount(stack, newAmount),
                RefinedStorageApi.INSTANCE.createInventorySlotReference(player, hand)
            ));
        }
        return InteractionResult.SUCCESS;
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
        return Optional.of(new RegulatorTooltipComponent(getDestinations(), HELP, getConfiguredResource(stack)));
    }

    private void setAmount(final ItemStack stack, final double amount) {
        final RegulatorUpgradeState state = stack.getOrDefault(
            DataComponents.INSTANCE.getRegulatorUpgradeState(),
            RegulatorUpgradeState.EMPTY
        );
        stack.set(DataComponents.INSTANCE.getRegulatorUpgradeState(), state.withAmount(amount));
    }

    public void setAmount(final ItemStack regulatorStack, final PlatformResourceKey resource, final double amount) {
        final RegulatorUpgradeState state = regulatorStack.getOrDefault(
            DataComponents.INSTANCE.getRegulatorUpgradeState(),
            new RegulatorUpgradeState(amount, Optional.of(resource))
        );
        regulatorStack.set(DataComponents.INSTANCE.getRegulatorUpgradeState(), state);
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
            if (normalizedAmount <= 0) {
                return null;
            }
            return new ResourceAmount(resource, normalizedAmount);
        }).orElse(null);
    }

    public long getDesiredAmount(final ItemStack stack, final ResourceKey resource) {
        final RegulatorUpgradeState state = stack.getOrDefault(
            DataComponents.INSTANCE.getRegulatorUpgradeState(),
            RegulatorUpgradeState.EMPTY
        );
        if (state.resource().isEmpty()) {
            return 0;
        }
        final PlatformResourceKey configuredResource = state.resource().get();
        if (!configuredResource.equals(resource)) {
            return 0;
        }
        final double amount = state.amount();
        return configuredResource.getResourceType().normalizeAmount(amount);
    }

    public record RegulatorTooltipComponent(Set<UpgradeMapping> destinations,
                                            Component helpText,
                                            @Nullable ResourceAmount configuredResource)
        implements TooltipComponent {
    }

    private record ExtendedMenuProviderImpl(@Nullable Component name,
                                            ResourceContainer resourceContainer,
                                            double amount,
                                            Consumer<Double> amountAcceptor,
                                            SlotReference slotReference)
        implements ExtendedMenuProvider<SingleAmountData> {
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
            return name == null ? ContentNames.REGULATOR_UPGRADE : name;
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
