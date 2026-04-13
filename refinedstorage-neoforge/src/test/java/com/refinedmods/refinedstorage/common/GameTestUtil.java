package com.refinedmods.refinedstorage.common;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.iface.ExportedResourcesContainer;
import com.refinedmods.refinedstorage.common.iface.InterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import static net.minecraft.world.item.Items.AIR;

public final class GameTestUtil {
    public static final Blocks MOD_BLOCKS = Blocks.INSTANCE;
    public static final Items MOD_ITEMS = Items.INSTANCE;

    private GameTestUtil() {
    }

    @Nullable
    private static Network getNetwork(final GameTestHelper helper, final BlockPos pos) {
        try {
            final var be = helper.getBlockEntity(pos, AbstractNetworkNodeContainerBlockEntity.class);
            final var field = AbstractNetworkNodeContainerBlockEntity.class.getDeclaredField("mainNetworkNode");
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

    public static void checkBlockEntityActiveness(final GameTestHelper helper,
                                                  final BlockPos pos,
                                                  final boolean expectedActive) {
        final var blockEntity = helper.getBlockEntity(
            pos,
            AbstractBaseNetworkNodeContainerBlockEntity.class
        );
        final boolean actualActive = blockEntity.getBlockState().getValue(AbstractActiveColoredDirectionalBlock.ACTIVE);
        helper.assertTrue(actualActive == expectedActive, "Activeness of Block Entity should be " + expectedActive
            + " but is " + actualActive);
    }

    public static void insert(final GameTestHelper helper,
                              final Network network,
                              final Item resource,
                              final long amount,
                              final boolean shouldSucceed) {
        insert(helper, network, new ItemResource(resource), amount, shouldSucceed);
    }

    public static void insert(final GameTestHelper helper,
                              final Network network,
                              final Item resource,
                              final long amount) {
        insert(helper, network, new ItemResource(resource), amount, true);
    }

    public static void insert(final GameTestHelper helper,
                              final Network network,
                              final Fluid resource,
                              final long amount,
                              final boolean shouldSucceed) {
        insert(helper, network, new FluidResource(resource), amount, shouldSucceed);
    }

    public static void insert(final GameTestHelper helper,
                              final Network network,
                              final Fluid resource,
                              final long amount) {
        insert(helper, network, new FluidResource(resource), amount, true);
    }

    public static void insert(final GameTestHelper helper,
                              final Network network,
                              final ResourceKey resource,
                              final long amount) {
        insert(helper, network, resource, amount, true);
    }

    public static void insert(final GameTestHelper helper,
                              final Network network,
                              final ResourceKey resource,
                              final long amount,
                              final boolean shouldSucceed) {
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        final long inserted = storage.insert(resource, amount, Action.EXECUTE, Actor.EMPTY);
        if (shouldSucceed) {
            helper.assertTrue(inserted == amount, "Resource couldn't be inserted");
        } else {
            helper.assertFalse(inserted == amount, "Resource could be inserted");
        }
    }

    public static void extract(final GameTestHelper helper,
                               final Network network,
                               final Item resource,
                               final long amount,
                               final boolean shouldSucceed) {
        extract(helper, network, new ItemResource(resource), amount, shouldSucceed);
    }

    public static void extract(final GameTestHelper helper,
                               final Network network,
                               final Item resource,
                               final long amount) {
        extract(helper, network, new ItemResource(resource), amount, true);
    }

    public static void extract(final GameTestHelper helper,
                               final Network network,
                               final Fluid resource,
                               final long amount,
                               final boolean shouldSucceed) {
        extract(helper, network, new FluidResource(resource), amount, shouldSucceed);
    }

    public static void extract(final GameTestHelper helper,
                               final Network network,
                               final Fluid resource,
                               final long amount) {
        extract(helper, network, new FluidResource(resource), amount, true);
    }

    public static void extract(final GameTestHelper helper,
                               final Network network,
                               final ResourceKey resource,
                               final long amount,
                               final boolean shouldSucceed) {
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        final long extracted = storage.extract(resource, amount, Action.EXECUTE, Actor.EMPTY);
        if (shouldSucceed) {
            helper.assertTrue(extracted == amount, "Resource couldn't be extracted");
        } else {
            helper.assertFalse(extracted == amount, "Resource could be extracted");
        }
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

    public static void assertItemEntityPresentExactly(final GameTestHelper helper,
                                                      final ItemStack itemStack,
                                                      final BlockPos pos,
                                                      final double expansionAmount) {
        final BlockPos blockpos = helper.absolutePos(pos);
        final Iterator<ItemEntity> entityIterator = helper.getLevel().getEntities(EntityType.ITEM,
            (new AABB(blockpos)).inflate(expansionAmount), Entity::isAlive).iterator();

        ItemEntity itemEntity;
        do {
            if (!entityIterator.hasNext()) {
                throw helper.assertionException("Expected " + itemStack.getItemName().getString()
                    + " item at: " + blockpos + " with count: " + itemStack.getCount());
            }
            itemEntity = entityIterator.next();
        } while (!itemEntity.getItem().getItem().equals(itemStack.getItem())
            || itemEntity.getItem().getCount() != itemStack.getCount());
    }

    public static Runnable assertInterfaceEmpty(final GameTestHelper helper,
                                                final BlockPos pos) {
        final var interfaceBlockEntity = helper.getBlockEntity(pos, InterfaceBlockEntity.class);
        return assertResourceContainerEmpty(
            interfaceBlockEntity.getDisplayName(),
            interfaceBlockEntity.getExportedResources(),
            helper
        );
    }

    private static Runnable assertResourceContainerEmpty(final Component displayName,
                                                         final ResourceContainer container,
                                                         final GameTestHelper helper) {
        return () -> {
            if (!container.isEmpty()) {
                throw helper.assertionException(displayName.getString() + " should be empty");
            }
        };
    }

    public static Runnable checkEnergyInNetwork(final GameTestHelper helper,
                                                final BlockPos pos,
                                                final Function<Long, Long> storedConsumer) {
        return networkIsAvailable(helper, pos, network -> {
            final EnergyNetworkComponent energyComponent = network.getComponent(EnergyNetworkComponent.class);

            long storedEnergy = energyComponent.getStored();
            storedEnergy = storedConsumer.apply(storedEnergy);

            energyStoredExactly(storedEnergy, energyComponent.getCapacity(), helper);
        });
    }

    public static void energyStoredExactly(final long storedEnergy,
                                           final long energyAmount,
                                           final GameTestHelper helper) {
        if (storedEnergy != energyAmount) {
            throw helper.assertionException("Energy stored should be: " + energyAmount + " but is " + storedEnergy);
        }
    }

    public static Runnable interfaceContainsExactly(final GameTestHelper helper,
                                                    final BlockPos pos,
                                                    final ResourceAmount... expected) {
        final var interfaceBlockEntity = helper.getBlockEntity(pos, InterfaceBlockEntity.class);
        return resourceContainerContainsExactly(helper, interfaceBlockEntity.getExportedResources(), expected);
    }

    private static Runnable resourceContainerContainsExactly(final GameTestHelper helper,
                                                             final ResourceContainer container,
                                                             final ResourceAmount... expected) {
        final ResourceList expectedList = toResourceList(expected);
        return () -> {
            final MutableResourceList given = MutableResourceListImpl.create();
            for (int i = 0; i < container.size(); i++) {
                final ResourceAmount item = container.get(i);
                if (item != null) {
                    given.add(item);
                }
            }
            listContainsExactly(given, expectedList, helper);
        };
    }

    public static Runnable containerContainsExactly(final GameTestHelper helper,
                                                    final BlockPos pos,
                                                    final ResourceAmount... expected) {
        final var containerBlockEntity = helper.getBlockEntity(pos, BaseContainerBlockEntity.class);
        final ResourceList expectedList = toResourceList(expected);
        return () -> {
            final MutableResourceList given = MutableResourceListImpl.create();
            for (int i = 0; i < containerBlockEntity.getContainerSize(); i++) {
                final ItemStack itemStack = containerBlockEntity.getItem(i);
                if (!itemStack.isEmpty()) {
                    given.add(asResource(itemStack), itemStack.getCount());
                }
            }
            listContainsExactly(given, expectedList, helper);
        };
    }

    public static Runnable storageContainsExactly(final GameTestHelper helper,
                                                  final BlockPos networkPos,
                                                  final ResourceAmount... expected) {
        final ResourceList expectedList = toResourceList(expected);
        return networkIsAvailable(helper, networkPos, network -> {
            final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
            listContainsExactly(toResourceList(storage.getAll()), expectedList, helper);
        });
    }

    public static Runnable storageIsEmpty(final GameTestHelper helper,
                                          final BlockPos networkPos) {
        return networkIsAvailable(helper, networkPos, network -> {
            final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
            helper.assertTrue(storage.getStored() == 0, "Storage is not empty");
        });
    }

    private static ResourceList toResourceList(final ResourceAmount... resources) {
        return toResourceList(Arrays.asList(resources));
    }

    private static ResourceList toResourceList(final Collection<ResourceAmount> resources) {
        final MutableResourceList list = MutableResourceListImpl.create();
        for (final ResourceAmount resource : resources) {
            list.add(resource);
        }
        return list;
    }

    private static void listContainsExactly(final ResourceList given, final ResourceList expected,
                                            final GameTestHelper helper) {
        for (final ResourceAmount expectedItem : expected.copyState()) {
            final long givenAmount = given.get(expectedItem.resource());
            if (givenAmount != expectedItem.amount()) {
                throw helper.assertionException(
                    "Expected " + expectedItem.amount() + " of " + expectedItem.resource() + ", but was " + givenAmount
                );
            }
        }
        for (final ResourceAmount givenItem : given.copyState()) {
            final long expectedAmount = expected.get(givenItem.resource());
            if (expectedAmount != givenItem.amount()) {
                throw helper.assertionException(
                    "Expected " + expectedAmount + " of " + givenItem.resource() + ", but was " + givenItem.amount()
                );
            }
        }
    }

    public static void prepareChest(final GameTestHelper helper,
                                    final BlockPos pos,
                                    final ItemStack... stacks) {
        helper.setBlock(pos, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState());
        final var chestBlockEntity = helper.getBlockEntity(pos, BaseContainerBlockEntity.class);
        for (int i = 0; i < stacks.length; i++) {
            chestBlockEntity.setItem(i, stacks[i]);
        }
    }

    public static void addItemToChest(final GameTestHelper helper,
                                      final BlockPos pos,
                                      final ItemStack stack) {
        final var chestBlockEntity = helper.getBlockEntity(pos, BaseContainerBlockEntity.class);
        for (int i = 0; i < chestBlockEntity.getContainerSize(); i++) {
            if (chestBlockEntity.getItem(i).isEmpty()) {
                chestBlockEntity.setItem(i, stack);
                return;
            }
        }
    }

    public static void removeItemFromChest(final GameTestHelper helper,
                                           final BlockPos pos,
                                           final ItemStack stack) {
        final var chestBlockEntity = helper.getBlockEntity(pos, BaseContainerBlockEntity.class);
        for (int i = 0; i < chestBlockEntity.getContainerSize(); i++) {
            if (chestBlockEntity.getItem(i).is(stack.getItem())) {
                chestBlockEntity.removeItem(i, stack.getCount());
            }
        }
    }

    public static void prepareInterface(final GameTestHelper helper,
                                        final BlockPos pos,
                                        final ResourceAmount... resources) {
        helper.setBlock(pos, MOD_BLOCKS.getInterface());
        final var interfaceBlockEntity = helper.getBlockEntity(pos, InterfaceBlockEntity.class);
        final ExportedResourcesContainer exportedResources = interfaceBlockEntity.getExportedResources();

        for (int i = 0; i < resources.length; i++) {
            exportedResources.set(i, resources[i]);
        }
    }

    public static void addFluidToInterface(final GameTestHelper helper,
                                           final BlockPos pos,
                                           final ResourceAmount resource) {
        final var interfaceBlockEntity = helper.getBlockEntity(pos, InterfaceBlockEntity.class);
        final ExportedResourcesContainer exportedResources = interfaceBlockEntity.getExportedResources();

        exportedResources.insert(resource.resource(), resource.amount(), Action.EXECUTE);
    }

    public static void removeFluidFromInterface(final GameTestHelper helper,
                                                final BlockPos pos,
                                                final ResourceAmount resource) {
        final var interfaceBlockEntity = helper.getBlockEntity(pos, InterfaceBlockEntity.class);
        final ExportedResourcesContainer exportedResources = interfaceBlockEntity.getExportedResources();

        final long extracted = exportedResources.extract(resource.resource(), resource.amount(), Action.EXECUTE);

        if (extracted <= 0) {
            throw helper.assertionException(
                "Resource " + resource.resource() + " with amount " + resource.amount() + " could not be extracted "
            );
        }
    }

    public static Runnable startAutocraftingTask(final GameTestHelper helper,
                                                 final BlockPos pos,
                                                 final ResourceAmount resource) {
        return networkIsAvailable(helper, pos, network -> {
            network.getComponent(AutocraftingNetworkComponent.class).startTask(
                resource.resource(), resource.amount(), Actor.EMPTY, false, CancellationToken.NONE);
        });
    }

    public static void tickFurnace(final GameTestHelper helper,
                                   final BlockPos pos,
                                   final int amount) {
        final AbstractFurnaceBlockEntity furnaceBlockEntity =
            helper.getBlockEntity(pos.below(), AbstractFurnaceBlockEntity.class);
        for (int i = 0; i < amount; i++) {
            AbstractFurnaceBlockEntity.serverTick(helper.getLevel(), pos.below(),
                furnaceBlockEntity.getBlockState(), furnaceBlockEntity);
        }
    }

    public static ItemStack[] createStacks(final Item item, final int count, final int amount) {
        final ItemStack[] stacks = new ItemStack[amount];
        for (int i = 0; i < amount; i++) {
            stacks[i] = item.getDefaultInstance().copyWithCount(count);
        }
        return stacks;
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

    public static ItemStack getItemAsDamaged(final ItemStack stack, final int damageValue) {
        stack.setDamageValue(damageValue);
        return stack;
    }

    public static CraftingInput.Positioned createCraftingMatrix(final List<Item> items,
                                                                final List<Integer> itemIndices) {
        final List<ItemStack> craftingMatrix = new ArrayList<>(Collections.nCopies(9, AIR.getDefaultInstance()));
        for (final Integer index : itemIndices) {
            craftingMatrix.set(index, items.get(index).getDefaultInstance());
        }
        return CraftingInput.ofPositioned(3, 3, craftingMatrix);
    }
}
