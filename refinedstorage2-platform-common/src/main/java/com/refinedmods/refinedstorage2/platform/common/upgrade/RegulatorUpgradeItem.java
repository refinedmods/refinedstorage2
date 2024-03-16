package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractSingleAmountContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceContainerImpl;

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
                PlatformApi.INSTANCE.createInventorySlotReference(player, hand)
            ));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new RegulatorTooltipComponent(HELP, getConfiguredResource(stack)));
    }

    @Nullable
    private ResourceAmount getConfiguredResource(final ItemStack stack) {
        final ResourceContainer container = getResourceFilterContainer(stack);
        final PlatformResourceKey resource = container.getResource(0);
        if (resource == null) {
            return null;
        }
        final double amount = getAmount(stack);
        final long normalizedAmount = resource.getResourceType().normalizeAmount(amount);
        return new ResourceAmount(resource, normalizedAmount);
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

    public OptionalLong getDesiredAmount(final ItemStack stack, final ResourceKey resource) {
        final ResourceContainer container = getResourceFilterContainer(stack);
        final PlatformResourceKey configuredResource = container.getResource(0);
        if (configuredResource == null) {
            return OptionalLong.empty();
        }
        final boolean same = configuredResource.equals(resource);
        if (!same) {
            return OptionalLong.empty();
        }
        final double amount = getAmount(stack);
        final long normalizedAmount = configuredResource.getResourceType().normalizeAmount(amount);
        return OptionalLong.of(normalizedAmount);
    }

    public record RegulatorTooltipComponent(Component helpText,
                                            @Nullable ResourceAmount configuredResource)
        implements TooltipComponent {
    }

    private static class ExtendedMenuProviderImpl implements ExtendedMenuProvider {
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
        public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
            AbstractSingleAmountContainerMenu.writeToBuf(buf, amount, resourceContainer, slotReference);
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
