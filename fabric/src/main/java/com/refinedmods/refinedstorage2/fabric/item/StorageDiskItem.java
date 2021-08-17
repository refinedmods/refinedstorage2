package com.refinedmods.refinedstorage2.fabric.item;

import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.core.util.Quantities;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.fabric.api.storage.disk.FabricItemDiskStorage;
import com.refinedmods.refinedstorage2.fabric.api.storage.disk.FabricStorageDiskManager;

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

public class StorageDiskItem extends Item {
    private static final String TAG_ID = "id";

    private final ItemStorageType type;

    public StorageDiskItem(Settings settings, ItemStorageType type) {
        super(settings);
        this.type = type;
    }

    public static Optional<UUID> getId(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().containsUuid(TAG_ID)) {
            return Optional.of(stack.getNbt().getUuid(TAG_ID));
        }
        return Optional.empty();
    }

    public static Optional<StorageDiskInfo> getInfo(@Nullable World world, ItemStack stack) {
        if (world == null) {
            return Optional.empty();
        }
        return getId(stack).map(Rs2PlatformApiFacade.INSTANCE.getStorageDiskManager(world)::getInfo);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        getInfo(world, stack).ifPresent(info -> {
            if (info.getCapacity() == -1) {
                tooltip.add(Rs2Mod.createTranslation("misc", "stored", Quantities.formatWithUnits(info.getStored())).formatted(Formatting.GRAY));
            } else {
                tooltip.add(Rs2Mod.createTranslation("misc", "stored_with_capacity", Quantities.formatWithUnits(info.getStored()), Quantities.formatWithUnits(info.getCapacity())).formatted(Formatting.GRAY));
            }
        });

        if (context.isAdvanced()) {
            getId(stack).ifPresent(id -> tooltip.add(new LiteralText(id.toString()).formatted(Formatting.GRAY)));
        }
    }

    // TODO immunity for despawning
    // TODO tags in recipes

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!(world instanceof ServerWorld) || !user.isSneaking() || type == ItemStorageType.CREATIVE) {
            return TypedActionResult.fail(stack);
        }

        return getId(stack)
                .flatMap(id -> Rs2PlatformApiFacade.INSTANCE.getStorageDiskManager(world).disassembleDisk(id))
                .map(disk -> {
                    ItemStack storagePart = createStoragePart(stack.getCount());

                    if (!user.getInventory().insertStack(storagePart.copy())) {
                        world.spawnEntity(new ItemEntity(world, user.getX(), user.getY(), user.getZ(), storagePart));
                    }

                    return TypedActionResult.success(new ItemStack(Rs2Mod.ITEMS.getStorageHousing()));
                })
                .orElse(TypedActionResult.fail(stack));
    }

    private ItemStack createStoragePart(int count) {
        return new ItemStack(Rs2Mod.ITEMS.getStoragePart(type), count);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!world.isClient() && !stack.hasNbt() && entity instanceof PlayerEntity) {
            UUID id = UUID.randomUUID();

            Rs2PlatformApiFacade.INSTANCE.getStorageDiskManager(world).setDisk(id, new FabricItemDiskStorage(type.getCapacity(),
                    () -> ((FabricStorageDiskManager) Rs2PlatformApiFacade.INSTANCE.getStorageDiskManager(world)).markDirty()));

            stack.setNbt(new NbtCompound());
            stack.getNbt().putUuid(TAG_ID, id);
        }
    }

    public enum ItemStorageType {
        ONE_K("1k", 1000),
        FOUR_K("4k", 4000),
        SIXTEEN_K("16k", 16_000),
        SIXTY_FOUR_K("64k", 64_000),
        CREATIVE("creative", -1);

        private final String name;
        private final int capacity;

        ItemStorageType(String name, int capacity) {
            this.name = name;
            this.capacity = capacity;
        }

        public String getName() {
            return name;
        }

        public int getCapacity() {
            return capacity;
        }
    }
}
