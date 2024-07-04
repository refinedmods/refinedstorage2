package com.refinedmods.refinedstorage.platform.common;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.platform.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.common.content.Blocks;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public final class GameTestUtil {
    public static final Blocks RSBLOCKS = Blocks.INSTANCE;

    private GameTestUtil() {
    }

    @Nullable
    private static Network getNetwork(final GameTestHelper helper, final BlockPos pos) {
        try {
            final var be = requireBlockEntity(helper, pos, AbstractNetworkNodeContainerBlockEntity.class);
            final var field = AbstractNetworkNodeContainerBlockEntity.class.getDeclaredField("mainNode");
            field.setAccessible(true);
            final NetworkNode mainNode = (NetworkNode) field.get(be);
            return mainNode.getNetwork();
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static Runnable networkIsAvailable(final GameTestHelper helper,
                                              final BlockPos networkPos,
                                              final Consumer<Network> networkConsumer) {
        return () -> {
            final Network network = getNetwork(helper, networkPos);
            helper.assertTrue(network != null, "Network is not available");
            networkConsumer.accept(network);
        };
    }

    public static void insert(final GameTestHelper helper,
                              final Network network,
                              final Item resource,
                              final long amount) {
        insert(helper, network, new ItemResource(resource), amount);
    }

    public static void insert(final GameTestHelper helper,
                              final Network network,
                              final Fluid resource,
                              final long amount) {
        insert(helper, network, new FluidResource(resource), amount);
    }

    public static void insert(final GameTestHelper helper,
                              final Network network,
                              final ResourceKey resource,
                              final long amount) {
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        final long inserted = storage.insert(resource, amount, Action.EXECUTE, EmptyActor.INSTANCE);
        helper.assertTrue(inserted == amount, "Resource couldn't be inserted");
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> T requireBlockEntity(
        final GameTestHelper helper,
        final BlockPos pos,
        final Class<T> clazz
    ) {
        final BlockEntity blockEntity = helper.getBlockEntity(pos);
        if (blockEntity == null) {
            throw new GameTestAssertException("Block entity not found at " + pos);
        }
        if (!clazz.isInstance(blockEntity)) {
            throw new GameTestAssertException(
                "Expected block entity of type " + clazz + " but was " + blockEntity.getClass()
            );
        }
        return (T) blockEntity;
    }

    public static void assertFluidPresent(final GameTestHelper helper,
                                          final BlockPos pos,
                                          final Fluid fluid,
                                          final int level) {
        final FluidState fluidState = helper.getLevel().getFluidState(helper.absolutePos(pos));
        helper.assertTrue(
            fluidState.getType() == fluid && fluidState.getAmount() == level,
            "Unexpected " + fluidState.getType() + ", " + fluidState.getAmount()
        );
    }

    public static Runnable containerContainsExactly(final GameTestHelper helper,
                                                    final BlockPos pos,
                                                    final ResourceAmount... expected) {
        final var containerBlockEntity = requireBlockEntity(helper, pos, BaseContainerBlockEntity.class);

        return () -> {
            for (final ResourceAmount expectedStack : expected) {
                final boolean contains = IntStream.range(0, containerBlockEntity.getContainerSize())
                    .mapToObj(containerBlockEntity::getItem)
                    .anyMatch(inContainer -> asResource(inContainer).equals(expectedStack.getResource())
                        && inContainer.getCount() == expectedStack.getAmount());
                helper.assertTrue(contains, "Expected resource is missing from storage: "
                    + expectedStack + " with count: " + expectedStack.getAmount());
            }
            for (int i = 0; i < containerBlockEntity.getContainerSize(); i++) {
                final ItemStack inContainer = containerBlockEntity.getItem(i);

                if (inContainer.getItem() != Items.AIR) {
                    final boolean wasExpected = Arrays.stream(expected).anyMatch(
                        expectedStack -> expectedStack.getResource().equals(asResource(inContainer))
                            && expectedStack.getAmount() == inContainer.getCount()
                    );
                    helper.assertTrue(wasExpected, "Unexpected resource found in storage: "
                        + inContainer.getDescriptionId() + " with count: " + inContainer.getCount());
                }
            }
        };
    }

    public static Runnable storageContainsExactly(final GameTestHelper helper,
                                                  final BlockPos networkPos,
                                                  final ResourceAmount... expected) {
        return networkIsAvailable(helper, networkPos, network -> {
            final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
            for (final ResourceAmount expectedResource : expected) {
                final boolean contains = storage.getAll()
                    .stream()
                    .anyMatch(inStorage -> inStorage.getResource().equals(expectedResource.getResource())
                        && inStorage.getAmount() == expectedResource.getAmount());
                helper.assertTrue(contains, "Expected resource is missing from storage: " + expectedResource);
            }
            for (final ResourceAmount inStorage : storage.getAll()) {
                final boolean wasExpected = Arrays.stream(expected).anyMatch(
                    expectedResource -> expectedResource.getResource().equals(inStorage.getResource())
                        && expectedResource.getAmount() == inStorage.getAmount()
                );
                helper.assertTrue(wasExpected, "Unexpected resource found in storage: " + inStorage);
            }
        });
    }

    public static ItemResource asResource(final Item item) {
        return new ItemResource(item);
    }

    public static ItemResource asResource(final ItemStack itemStack) {
        return ItemResource.ofItemStack(itemStack);
    }

    public static FluidResource asResource(final Fluid fluid) {
        return new FluidResource(fluid);
    }
}
