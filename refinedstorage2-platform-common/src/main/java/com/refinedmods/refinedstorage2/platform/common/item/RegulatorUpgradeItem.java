package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.api.item.AbstractUpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractSingleAmountContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.RegulatorUpgradeContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class RegulatorUpgradeItem extends AbstractUpgradeItem {
    private static final Component HELP = createTranslation("item", "regulator_upgrade.help");
    private static final String TAG_AMOUNT = "a";
    private static final String TAG_RESOURCE_FILTER_CONTAINER = "rf";

    public RegulatorUpgradeItem(final UpgradeRegistry registry) {
        super(new Item.Properties(), registry);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            Platform.INSTANCE.getMenuOpener().openMenu(serverPlayer, new ExtendedMenuProviderImpl(
                getResourceFilterContainer(stack),
                getAmount(stack),
                newAmount -> setAmount(stack, newAmount),
                PlayerSlotReference.of(player, hand)
            ));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new RegulatorTooltipComponent<>(HELP, getFilteredResource(stack)));
    }

    @Nullable
    private ResourceAmountTemplate<?> getFilteredResource(final ItemStack stack) {
        final ResourceContainer container = getResourceFilterContainer(stack);
        final ResourceAmountTemplate<?> resourceAmount = container.get(0);
        if (resourceAmount == null) {
            return null;
        }
        final double amount = getAmount(stack);
        final long normalizedAmount = resourceAmount.getStorageChannelType().normalizeAmount(amount);
        return resourceAmount.withAmount(normalizedAmount);
    }

    public double getAmount(final ItemStack stack) {
        return stack.getTag() == null ? 1 : stack.getTag().getDouble(TAG_AMOUNT);
    }

    private void setAmount(final ItemStack stack, final double amount) {
        stack.getOrCreateTag().putDouble(TAG_AMOUNT, amount);
    }

    private ResourceContainer getResourceFilterContainer(final ItemStack stack) {
        final ResourceContainer container = ResourceContainerImpl.createForFilter(1);
        container.setListener(() -> stack.getOrCreateTag().put(TAG_RESOURCE_FILTER_CONTAINER, container.toTag()));
        final CompoundTag tag = stack.getTagElement(TAG_RESOURCE_FILTER_CONTAINER);
        if (tag != null) {
            container.fromTag(tag);
        }
        return container;
    }

    @Override
    public long getEnergyUsage() {
        return Platform.INSTANCE.getConfig().getUpgrade().getRegulatorUpgradeEnergyUsage();
    }

    public OptionalLong getDesiredAmount(final ItemStack stack, final Object resource) {
        final ResourceContainer container = getResourceFilterContainer(stack);
        final ResourceAmountTemplate<?> filteredResource = container.get(0);
        if (filteredResource == null) {
            return OptionalLong.empty();
        }
        final boolean same = filteredResource.getResource().equals(resource);
        if (!same) {
            return OptionalLong.empty();
        }
        final double amount = getAmount(stack);
        final long normalizedAmount = filteredResource.getStorageChannelType().normalizeAmount(amount);
        return OptionalLong.of(normalizedAmount);
    }

    public record RegulatorTooltipComponent<T>(Component helpText,
                                               @Nullable ResourceAmountTemplate<T> filteredResource)
        implements TooltipComponent {
    }

    private static class ExtendedMenuProviderImpl implements ExtendedMenuProvider {
        private final ResourceContainer resourceContainer;
        private final double amount;
        private final Consumer<Double> amountAcceptor;
        private final PlayerSlotReference playerSlotReference;

        private ExtendedMenuProviderImpl(final ResourceContainer resourceContainer,
                                         final double amount,
                                         final Consumer<Double> amountAcceptor,
                                         final PlayerSlotReference playerSlotReference) {
            this.resourceContainer = resourceContainer;
            this.amount = amount;
            this.amountAcceptor = amountAcceptor;
            this.playerSlotReference = playerSlotReference;
        }

        @Override
        public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
            AbstractSingleAmountContainerMenu.writeToBuf(buf, amount, resourceContainer, playerSlotReference);
        }

        @Override
        public Component getDisplayName() {
            return createTranslation("item", "regulator_upgrade");
        }

        @Override
        public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
            return new RegulatorUpgradeContainerMenu(
                syncId,
                player,
                resourceContainer,
                amountAcceptor,
                playerSlotReference
            );
        }
    }
}
