package com.refinedmods.refinedstorage.neoforge.debug;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DebugStickItem extends Item {
    public DebugStickItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        final Level level = context.getLevel();
        final BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (blockEntity != null) {
            dump(level, blockEntity, player);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player,
                                                  final InteractionHand usedHand) {
        if (!level.isClientSide()) {
            final ItemStack storageDisk = new ItemStack(Items.INSTANCE.getItemStorageDisk(ItemStorageVariant.CREATIVE));
            storageDisk.inventoryTick(level, player, 0, false);
            ((StorageContainerItem) storageDisk.getItem()).resolve(
                RefinedStorageApi.INSTANCE.getStorageRepository(level),
                storageDisk
            ).ifPresent(storage -> {
                int size = 0;
                for (final Item item : BuiltInRegistries.ITEM) {
                    final ItemStack stack = item.getDefaultInstance();
                    if (stack.isDamageableItem() && player.isCrouching()) {
                        for (int i = 0; i < stack.getMaxDamage(); ++i) {
                            final ItemStack damaged = stack.copy();
                            damaged.setDamageValue(i);
                            storage.insert(ItemResource.ofItemStack(damaged), Integer.MAX_VALUE, Action.EXECUTE,
                                Actor.EMPTY);
                            size++;
                        }
                    } else {
                        storage.insert(ItemResource.ofItemStack(stack), Integer.MAX_VALUE, Action.EXECUTE, Actor.EMPTY);
                        size++;
                    }
                }
                player.getInventory().add(storageDisk);
                player.sendSystemMessage(Component.literal("Gave a storage disk with " + size + " item types"));
            });
        }
        return super.use(level, player, usedHand);
    }

    private static void dump(final Level level, final BlockEntity blockEntity, final Player player) {
        if (level.isClientSide()) {
            return;
        }
        if (!(blockEntity instanceof AbstractBaseNetworkNodeContainerBlockEntity<?> provider)) {
            return;
        }
        provider.getContainerProvider().getContainers().forEach(container -> dump(player, container));
        player.sendSystemMessage(Component.literal("---"));
    }

    private static void dump(final Player player, final InWorldNetworkNodeContainer container) {
        final Network network = container.getNode().getNetwork();
        player.sendSystemMessage(Component.literal(
            container.getNode().getClass().getSimpleName() + " --> ").append((network == null
            ? Component.literal("<NULL>").withStyle(ChatFormatting.RED)
            : Component.literal("" + network.hashCode()).withStyle(ChatFormatting.AQUA)
        )));
        if (network != null) {
            dump(player, network);
        }
    }

    private static void dump(final Player player, final Network network) {
        final Map<String, Integer> nodesByType = network.getComponent(GraphNetworkComponent.class)
            .getContainers()
            .stream()
            .map(c -> c.getNode().getClass().getSimpleName())
            .reduce(new LinkedHashMap<>(), (map, name) -> {
                map.put(name, map.getOrDefault(name, 0) + 1);
                return map;
            }, (m1, m2) -> {
                m1.putAll(m2);
                return m1;
            });
        nodesByType.forEach((type, amount) -> player.sendSystemMessage(Component.literal(amount + "x ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(type).withStyle(ChatFormatting.GOLD))
        ));
    }
}
