package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.api.network.impl.node.security.SecurityDecisionProviderProxyNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.security.SecurityDecisionProviderImpl;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.security.SecurityPolicyContainerItem;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeContainerProviderImpl;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
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
    extends AbstractBaseNetworkNodeContainerBlockEntity<SecurityDecisionProviderProxyNetworkNode>
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
        securityCards.addListener(card -> invalidate());
        fallbackSecurityCard.addListener(card -> invalidate());
        mainNetworkNode.setDelegate(securityDecisionProvider);
    }

    @Override
    protected NetworkNodeContainerProvider createContainerProvider() {
        return new NetworkNodeContainerProviderImpl() {
            @Override
            public boolean canBuild(final ServerPlayer player) {
                return super.canBuild(player) || isPlacedBy(player.getGameProfile().getId());
            }
        };
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(
        final SecurityDecisionProviderProxyNetworkNode networkNode) {
        return RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, networkNode)
            .connectionStrategy(new SecurityManagerConnectionStrategy(this::getBlockState, getBlockPos()))
            .build();
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
        mainNetworkNode.setEnergyUsage(energyUsage);
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
    public final NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < securityCards.getContainerSize(); ++i) {
            drops.add(securityCards.getItem(i));
        }
        drops.add(fallbackSecurityCard.getItem(0));
        return drops;
    }

    public FilteredContainer getSecurityCards() {
        return securityCards;
    }

    public FilteredContainer getFallbackSecurityCard() {
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
    public Component getName() {
        return overrideName(ContentNames.SECURITY_MANAGER);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new SecurityManagerContainerMenu(syncId, inventory, this);
    }

    @Override
    public boolean canOpen(final ServerPlayer player) {
        final boolean isAllowedViaSecuritySystem = NetworkNodeMenuProvider.super.canOpen(player)
            && SecurityHelper.isAllowed(player, BuiltinPermission.SECURITY, containers.getContainers());
        return isAllowedViaSecuritySystem || isPlacedBy(player.getGameProfile().getId());
    }
}
