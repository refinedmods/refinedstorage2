package com.refinedmods.refinedstorage2.fabric.item;

import com.refinedmods.refinedstorage2.core.storage.disk.ItemDiskStorage;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.core.util.Quantities;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.coreimpl.storage.disk.ItemStorageType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class StorageDiskItem extends Item {
    private final ItemStorageType type;

    public StorageDiskItem(Settings settings, ItemStorageType type) {
        super(settings);
        this.type = type;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        if (stack.hasTag() && stack.getTag().containsUuid("id")) {
            UUID id = stack.getTag().getUuid("id");

            StorageDiskInfo info = RefinedStorage2Mod.API.getStorageDiskManager(world).getInfo(id);
            if (info.getCapacity() == -1) {
                tooltip.add(new TranslatableText("misc.refinedstorage2.stored", Quantities.formatWithUnits(info.getStored())).formatted(Formatting.GRAY));
            } else {
                tooltip.add(new TranslatableText("misc.refinedstorage2.stored_with_capacity", Quantities.formatWithUnits(info.getStored()), Quantities.formatWithUnits(info.getCapacity())).formatted(Formatting.GRAY));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!world.isClient() && !stack.hasTag() && entity instanceof PlayerEntity) {
            UUID id = UUID.randomUUID();

            RefinedStorage2Mod.API.getStorageDiskManager(world).setDisk(id, new ItemDiskStorage(type.getCapacity()));

            stack.setTag(new CompoundTag());
            stack.getTag().putUuid("id", id);
        }
    }
}
