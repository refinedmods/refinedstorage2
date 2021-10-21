package com.refinedmods.refinedstorage2.platform.fabric.api.item;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.item.StorageDiskItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// TODO immunity for despawning
// TODO tags in recipes
public abstract class StorageDiskItemImpl extends Item implements StorageDiskItem {
    private static final String TAG_ID = "id";

    protected StorageDiskItemImpl(Settings settings) {
        super(settings);
    }

    @Override
    public Optional<UUID> getDiskId(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().containsUuid(TAG_ID)) {
            return Optional.of(stack.getNbt().getUuid(TAG_ID));
        }
        return Optional.empty();
    }

    @Override
    public Optional<StorageInfo> getInfo(@Nullable World world, ItemStack stack) {
        if (world == null) {
            return Optional.empty();
        }
        return getDiskId(stack).map(Rs2PlatformApiFacade.INSTANCE.getStorageRepository(world)::getInfo);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        getInfo(world, stack).ifPresent(info -> {
            if (info.capacity() == -1) {
                tooltip.add(Rs2PlatformApiFacade.INSTANCE.createTranslation(
                        "misc",
                        "stored",
                        formatQuantity(info.stored())
                ).formatted(Formatting.GRAY));
            } else {
                tooltip.add(Rs2PlatformApiFacade.INSTANCE.createTranslation(
                        "misc",
                        "stored_with_capacity",
                        formatQuantity(info.stored()),
                        formatQuantity(info.capacity())
                ).formatted(Formatting.GRAY));
            }
        });

        if (context.isAdvanced()) {
            getDiskId(stack).ifPresent(id -> tooltip.add(new LiteralText(id.toString()).formatted(Formatting.GRAY)));
        }
    }

    protected String formatQuantity(long qty) {
        return QuantityFormatter.formatWithUnits(qty);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        Optional<ItemStack> storagePart = createStoragePart(stack.getCount());

        if (!(world instanceof ServerWorld) || !user.isSneaking() || storagePart.isEmpty()) {
            return TypedActionResult.fail(stack);
        }

        return getDiskId(stack)
                .flatMap(id -> Rs2PlatformApiFacade.INSTANCE.getStorageRepository(world).disassemble(id))
                .map(disk -> {
                    if (!user.getInventory().insertStack(storagePart.get().copy())) {
                        world.spawnEntity(new ItemEntity(world, user.getX(), user.getY(), user.getZ(), storagePart.get()));
                    }

                    return TypedActionResult.success(createDisassemblyByproduct());
                })
                .orElse(TypedActionResult.fail(stack));
    }

    protected abstract Optional<ItemStack> createStoragePart(int count);

    protected abstract Storage<?> createStorage(World world);

    protected abstract ItemStack createDisassemblyByproduct();

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!world.isClient() && !stack.hasNbt() && entity instanceof PlayerEntity) {
            UUID id = UUID.randomUUID();

            Rs2PlatformApiFacade.INSTANCE.getStorageRepository(world).set(id, createStorage(world));

            stack.setNbt(new NbtCompound());
            stack.getNbt().putUuid(TAG_ID, id);
        }
    }
}
