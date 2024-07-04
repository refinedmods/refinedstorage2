package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.api.network.impl.node.security.SecurityDecisionProviderProxyNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.security.SecurityDecisionProviderImpl;
import com.refinedmods.refinedstorage.platform.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.platform.api.security.SecurityPolicyContainerItem;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.platform.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.NetworkNodeMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.common.util.ContainerUtil;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SecurityManagerBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<SecurityDecisionProviderProxyNetworkNode>
    implements BlockEntityWithDrops, NetworkNodeMenuProvider {
    static final int CARD_AMOUNT = 18;

    private static final String TAG_SECURITY_CARDS = "sc";
    private static final String TAG_FALLBACK_SECURITY_CARD = "fsc";

    private final FilteredContainer securityCards = new FilteredContainer(
        CARD_AMOUNT,
        SecurityManagerBlockEntity::isValidSecurityCard
    );
    private final FilteredContainer fallbackSecurityCard = new FilteredContainer(
        1,
        SecurityManagerBlockEntity::isValidFallbackSecurityCard
    );

    private final SecurityDecisionProviderImpl securityDecisionProvider = new SecurityDecisionProviderImpl();

    public SecurityManagerBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getSecurityManager(),
            pos,
            state,
            new SecurityDecisionProviderProxyNetworkNode(
                Platform.INSTANCE.getConfig().getSecurityManager().getEnergyUsage()
            )
        );
        securityCards.addListener(c -> invalidate());
        fallbackSecurityCard.addListener(c -> invalidate());
        mainNode.setDelegate(securityDecisionProvider);
    }

    private void invalidate() {
        if (level != null) {
            setChanged();
        }
        securityDecisionProvider.clearPolicies();
        long energyUsage = Platform.INSTANCE.getConfig().getSecurityManager().getEnergyUsage();
        for (int i = 0; i < securityCards.getContainerSize(); ++i) {
            final ItemStack securityCard = securityCards.getItem(i);
            if (!(securityCard.getItem() instanceof SecurityPolicyContainerItem securityPolicyContainerItem)) {
                continue;
            }
            energyUsage += securityPolicyContainerItem.getEnergyUsage();
            securityPolicyContainerItem.getPolicy(securityCard).ifPresent(
                policy -> securityPolicyContainerItem.getActor(securityCard).ifPresent(
                    actor -> securityDecisionProvider.setPolicy(actor, policy)));
        }
        energyUsage += updateDefaultPolicyAndGetEnergyUsage();
        mainNode.setEnergyUsage(energyUsage);
    }

    private long updateDefaultPolicyAndGetEnergyUsage() {
        final ItemStack fallbackSecurityCardStack = fallbackSecurityCard.getItem(0);
        if (fallbackSecurityCardStack.getItem() instanceof SecurityPolicyContainerItem securityPolicyContainerItem) {
            securityPolicyContainerItem.getPolicy(fallbackSecurityCardStack).ifPresentOrElse(
                securityDecisionProvider::setDefaultPolicy,
                () -> securityDecisionProvider.setDefaultPolicy(null)
            );
            return securityPolicyContainerItem.getEnergyUsage();
        }
        securityDecisionProvider.setDefaultPolicy(null);
        return 0;
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_SECURITY_CARDS)) {
            ContainerUtil.read(tag.getCompound(TAG_SECURITY_CARDS), securityCards, provider);
        }
        if (tag.contains(TAG_FALLBACK_SECURITY_CARD)) {
            ContainerUtil.read(tag.getCompound(TAG_FALLBACK_SECURITY_CARD), fallbackSecurityCard, provider);
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_SECURITY_CARDS, ContainerUtil.write(securityCards, provider));
        tag.put(TAG_FALLBACK_SECURITY_CARD, ContainerUtil.write(fallbackSecurityCard, provider));
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < securityCards.getContainerSize(); ++i) {
            drops.add(securityCards.getItem(i));
        }
        drops.add(fallbackSecurityCard.getItem(0));
        return drops;
    }

    FilteredContainer getSecurityCards() {
        return securityCards;
    }

    FilteredContainer getFallbackSecurityCard() {
        return fallbackSecurityCard;
    }

    static boolean isValidSecurityCard(final ItemStack stack) {
        return stack.getItem() instanceof SecurityPolicyContainerItem securityPolicyContainerItem
            && securityPolicyContainerItem.isValid(stack)
            && !(stack.getItem() instanceof FallbackSecurityCardItem);
    }

    static boolean isValidFallbackSecurityCard(final ItemStack stack) {
        return stack.getItem() instanceof FallbackSecurityCardItem;
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.SECURITY_MANAGER;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new SecurityManagerContainerMenu(syncId, inventory, this);
    }

    @Override
    public boolean canOpen(final ServerPlayer player) {
        final boolean isAllowedViaSecuritySystem = NetworkNodeMenuProvider.super.canOpen(player)
            && SecurityHelper.isAllowed(player, BuiltinPermission.SECURITY, getContainers());
        return isAllowedViaSecuritySystem || isPlacedBy(player.getGameProfile().getId());
    }

    @Override
    public boolean canBuild(final ServerPlayer player) {
        return super.canBuild(player) || isPlacedBy(player.getGameProfile().getId());
    }

    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        for (final Direction direction : Direction.values()) {
            if (direction == Direction.UP) {
                continue;
            }
            sink.tryConnectInSameDimension(worldPosition.relative(direction), direction.getOpposite());
        }
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction incomingDirection, final BlockState connectingState) {
        if (!colorsAllowConnecting(connectingState)) {
            return false;
        }
        return incomingDirection != Direction.UP;
    }
}
