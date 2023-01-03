package com.refinedmods.refinedstorage2.platform.api.item;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

// TODO: Immunity for despawning
// TODO: Tags/ore dict in recipes
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public abstract class AbstractStorageContainerItem<T> extends Item implements StorageContainerItem {
    protected final StorageContainerHelper helper;
    private final StorageChannelType<T> type;

    protected AbstractStorageContainerItem(final Properties properties,
                                           final StorageChannelType<T> type,
                                           final StorageContainerHelper helper) {
        super(properties);
        this.type = type;
        this.helper = helper;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<TypedStorage<?>> resolve(final StorageRepository storageRepository, final ItemStack stack) {
        return helper.resolve(storageRepository, stack).map(storage -> new TypedStorage(storage, type));
    }

    @Override
    public Optional<StorageInfo> getInfo(final StorageRepository storageRepository, final ItemStack stack) {
        return helper.getInfo(storageRepository, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        final ItemStack primaryByproduct = createPrimaryDisassemblyByproduct(stack.getCount());
        final ItemStack secondaryByproduct = createSecondaryDisassemblyByproduct(stack.getCount());
        return helper.tryDisassembly(level, player, stack, primaryByproduct, secondaryByproduct);
    }

    @Override
    public void inventoryTick(final ItemStack stack,
                              final Level level,
                              final Entity entity,
                              final int slot,
                              final boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide() && !stack.hasTag() && entity instanceof Player) {
            final StorageRepository storageRepository = PlatformApi.INSTANCE.getStorageRepository(level);
            helper.set(storageRepository, stack, createStorage(storageRepository));
        }
    }

    protected abstract Storage<T> createStorage(StorageRepository storageRepository);

    protected abstract ItemStack createPrimaryDisassemblyByproduct(int count);

    @Nullable
    protected abstract ItemStack createSecondaryDisassemblyByproduct(int count);
}
