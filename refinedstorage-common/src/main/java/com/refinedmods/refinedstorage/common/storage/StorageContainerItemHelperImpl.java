package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockEntity;
import com.refinedmods.refinedstorage.common.api.storage.StorageContainerItemHelper;
import com.refinedmods.refinedstorage.common.api.storage.StorageInfo;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.LongFunction;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageContainerItemHelperImpl implements StorageContainerItemHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageContainerItemHelperImpl.class);

    @Override
    public Optional<SerializableStorage> resolveStorage(final StorageRepository storageRepository,
                                                        final ItemStack stack) {
        return getId(stack).flatMap(storageRepository::get);
    }

    @Override
    public void loadStorageIfNecessary(final ItemStack stack,
                                       final Level level,
                                       final Entity entity,
                                       final Function<StorageRepository, SerializableStorage> factory) {
        if (!level.isClientSide() && !hasStorage(stack) && entity instanceof Player) {
            final StorageRepository storageRepository = RefinedStorageApi.INSTANCE.getStorageRepository(level);
            setStorage(storageRepository, stack, factory.apply(storageRepository));
        }
    }

    @Override
    public void transferStorageIfNecessary(final ItemStack stack,
                                           final Level level,
                                           final Entity entity,
                                           final Function<StorageRepository, SerializableStorage> factory) {
        if (!level.isClientSide()
            && !hasStorage(stack)
            && hasStorageToBeTransferred(stack)
            && entity instanceof Player player) {
            getIdToBeTransferred(stack).ifPresent(id -> doTransfer(level, factory, player, id, stack));
        }
    }

    private void doTransfer(final Level level,
                            final Function<StorageRepository, SerializableStorage> factory,
                            final Player player,
                            final UUID originalId,
                            final ItemStack stack) {
        final StorageRepository storageRepository = RefinedStorageApi.INSTANCE.getStorageRepository(level);
        final PlayerActor actor = new PlayerActor(player);
        storageRepository.get(originalId).ifPresent(originalStorage -> {
            final SerializableStorage transferStorage = factory.apply(storageRepository);
            originalStorage.getAll().forEach(
                original -> transferStorage.insert(original.resource(), original.amount(), Action.EXECUTE, actor)
            );
            setStorage(storageRepository, stack, transferStorage);
            storageRepository.remove(originalId);
            markAsTransferred(stack);
        });
    }

    private void setStorage(final StorageRepository storageRepository,
                            final ItemStack stack,
                            final SerializableStorage storage) {
        final UUID id = UUID.randomUUID();
        setId(stack, id);
        storageRepository.set(id, storage);
    }

    private boolean hasStorage(final ItemStack stack) {
        return stack.has(DataComponents.INSTANCE.getStorageReference());
    }

    private boolean hasStorageToBeTransferred(final ItemStack stack) {
        return stack.has(DataComponents.INSTANCE.getStorageReferenceToBeTransferred());
    }

    @Override
    public Optional<StorageInfo> getInfo(final StorageRepository storageRepository, final ItemStack stack) {
        return getId(stack).map(storageRepository::getInfo);
    }

    @Override
    public InteractionResultHolder<ItemStack> tryDisassembly(final Level level,
                                                             final Player player,
                                                             final ItemStack stack,
                                                             final ItemStack primaryByproduct,
                                                             @Nullable final ItemStack secondaryByproduct) {
        if (!(level instanceof ServerLevel) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.fail(stack);
        }

        final Optional<UUID> storageId = getId(stack);
        if (storageId.isEmpty()) {
            return returnByproducts(level, player, primaryByproduct, secondaryByproduct);
        }

        return storageId
            .flatMap(id -> RefinedStorageApi.INSTANCE.getStorageRepository(level).removeIfEmpty(id))
            .map(disk -> returnByproducts(level, player, primaryByproduct, secondaryByproduct))
            .orElseGet(() -> InteractionResultHolder.fail(stack));
    }

    private InteractionResultHolder<ItemStack> returnByproducts(final Level level,
                                                                final Player player,
                                                                final ItemStack primaryByproduct,
                                                                @Nullable final ItemStack secondaryByproduct) {
        tryReturnByproductToInventory(level, player, secondaryByproduct);
        return InteractionResultHolder.success(primaryByproduct);
    }

    private static void tryReturnByproductToInventory(final Level level,
                                                      final Player player,
                                                      @Nullable final ItemStack byproduct) {
        if (byproduct != null && !player.getInventory().add(byproduct.copy())) {
            level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), byproduct));
        }
    }

    @Override
    public void appendToTooltip(final ItemStack stack,
                                final StorageRepository storageRepository,
                                final List<Component> tooltip,
                                final TooltipFlag context,
                                final LongFunction<String> amountFormatter,
                                @Nullable final Long capacity) {
        final boolean transferring = hasStorageToBeTransferred(stack);
        getId(stack).or(() -> getIdToBeTransferred(stack)).ifPresent(id -> {
            final StorageInfo info = storageRepository.getInfo(id);
            if (capacity != null) {
                StorageTooltipHelper.addAmountStoredWithCapacity(
                    tooltip,
                    info.stored(),
                    transferring ? capacity : info.capacity(),
                    amountFormatter
                );
            } else {
                StorageTooltipHelper.addAmountStoredWithoutCapacity(
                    tooltip,
                    info.stored(),
                    amountFormatter
                );
            }
            if (context.isAdvanced()) {
                final MutableComponent idComponent = Component.literal(id.toString()).withStyle(ChatFormatting.GRAY);
                tooltip.add(idComponent);
            }
        });
    }

    @Override
    public void transferToBlockEntity(final ItemStack stack, final StorageBlockEntity blockEntity) {
        getId(stack).ifPresent(id -> {
            blockEntity.setStorageId(id);
            LOGGER.debug("Transferred storage {} to block entity {}", id, blockEntity);
        });
    }

    @Override
    public void transferFromBlockEntity(final ItemStack stack, final StorageBlockEntity blockEntity) {
        final UUID storageId = blockEntity.getStorageId();
        if (storageId != null) {
            LOGGER.debug("Transferred storage {} from block entity {} to stack", storageId, blockEntity);
            setId(stack, storageId);
        } else {
            LOGGER.warn("Could not transfer storage {} to stack, there is no storage ID!", blockEntity);
        }
    }

    @Override
    public void markAsToTransfer(final ItemStack from, final ItemStack to) {
        getId(from).ifPresent(id -> to.set(DataComponents.INSTANCE.getStorageReferenceToBeTransferred(), id));
    }

    private Optional<UUID> getId(final ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.INSTANCE.getStorageReference()));
    }

    @Override
    public boolean clear(final ItemStack stack) {
        if (getId(stack).isEmpty()) {
            return false;
        }

        stack.remove(DataComponents.INSTANCE.getStorageReference());
        return true;
    }

    private Optional<UUID> getIdToBeTransferred(final ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.INSTANCE.getStorageReferenceToBeTransferred()));
    }

    private void markAsTransferred(final ItemStack stack) {
        stack.remove(DataComponents.INSTANCE.getStorageReferenceToBeTransferred());
    }

    private void setId(final ItemStack stack, final UUID id) {
        stack.set(DataComponents.INSTANCE.getStorageReference(), id);
    }
}
