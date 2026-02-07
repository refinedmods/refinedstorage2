package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridInsertMode;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.common.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewStyle;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

public final class C2SPackets {
    private C2SPackets() {
    }

    public static void sendGridExtract(final PlatformResourceKey resource,
                                       final GridExtractMode mode,
                                       final boolean cursor) {
        Platform.INSTANCE.sendPacketToServer(new GridExtractPacket(resource, mode, cursor));
    }

    public static void sendGridScroll(final PlatformResourceKey resource,
                                      final GridScrollMode mode,
                                      final int slotIndex) {
        Platform.INSTANCE.sendPacketToServer(new GridScrollPacket(resource, mode, slotIndex));
    }

    public static void sendGridInsert(final GridInsertMode mode, final boolean tryAlternatives) {
        Platform.INSTANCE.sendPacketToServer(new GridInsertPacket(mode, tryAlternatives));
    }

    public static void sendCraftingGridClear(final boolean toPlayerInventory) {
        Platform.INSTANCE.sendPacketToServer(new CraftingGridClearPacket(toPlayerInventory));
    }

    public static void sendPatternGridClear() {
        Platform.INSTANCE.sendPacketToServer(PatternGridClearPacket.INSTANCE);
    }

    public static void sendPatternGridCreatePattern() {
        Platform.INSTANCE.sendPacketToServer(PatternGridCreatePatternPacket.INSTANCE);
    }

    public static void sendCraftingGridRecipeTransfer(final List<List<ItemResource>> recipe) {
        Platform.INSTANCE.sendPacketToServer(new CraftingGridRecipeTransferPacket(recipe));
    }

    public static <T> void sendPropertyChange(final PropertyType<T> type, final T value) {
        Platform.INSTANCE.sendPacketToServer(new PropertyChangePacket(type.id(), type.serializer().apply(value)));
    }

    public static void sendStorageInfoRequest(final UUID storageId) {
        Platform.INSTANCE.sendPacketToServer(new StorageInfoRequestPacket(storageId));
    }

    public static void sendResourceSlotChange(final int slotIndex, final boolean tryAlternatives) {
        Platform.INSTANCE.sendPacketToServer(new ResourceSlotChangePacket(slotIndex, tryAlternatives));
    }

    @API(status = API.Status.INTERNAL)
    public static void sendFilterSlotChange(final ItemStack stack, final int slotIndex) {
        Platform.INSTANCE.sendPacketToServer(new FilterSlotChangePacket(slotIndex, stack));
    }

    @API(status = API.Status.INTERNAL)
    public static void sendResourceFilterSlotChange(final PlatformResourceKey resource, final int slotIndex) {
        Platform.INSTANCE.sendPacketToServer(new ResourceFilterSlotChangePacket(slotIndex, resource));
    }

    public static void sendResourceSlotAmountChange(final int slotIndex, final long amount) {
        Platform.INSTANCE.sendPacketToServer(new ResourceSlotAmountChangePacket(slotIndex, amount));
    }

    public static void sendSingleAmountChange(final double amount) {
        Platform.INSTANCE.sendPacketToServer(new SingleAmountChangePacket(amount));
    }

    public static void sendUseSlotReferencedItem(final SlotReference slotReference) {
        Platform.INSTANCE.sendPacketToServer(new UseSlotReferencedItemPacket(slotReference));
    }

    public static void sendSecurityCardPermission(final PlatformPermission permission, final boolean allowed) {
        Platform.INSTANCE.sendPacketToServer(new SecurityCardPermissionPacket(permission, allowed));
    }

    public static void sendSecurityCardResetPermission(final PlatformPermission permission) {
        Platform.INSTANCE.sendPacketToServer(new SecurityCardResetPermissionPacket(permission));
    }

    public static void sendSecurityCardBoundPlayer(final UUID playerId) {
        Platform.INSTANCE.sendPacketToServer(new SecurityCardBoundPlayerPacket(playerId));
    }

    public static void sendPatternGridAllowedAlternativesChange(final int slotIndex,
                                                                final Set<Identifier> ids) {
        Platform.INSTANCE.sendPacketToServer(new PatternGridAllowedAlternativesChangePacket(slotIndex, ids));
    }

    public static void sendPatternGridCraftingRecipeTransfer(final List<List<ItemResource>> recipe) {
        Platform.INSTANCE.sendPacketToServer(new PatternGridCraftingRecipeTransferPacket(recipe));
    }

    public static void sendPatternGridProcessingRecipeTransfer(final List<List<ResourceAmount>> inputs,
                                                               final List<List<ResourceAmount>> outputs) {
        Platform.INSTANCE.sendPacketToServer(new PatternGridProcessingRecipeTransferPacket(inputs, outputs));
    }

    public static void sendPatternGridStonecutterRecipeTransfer(final ItemResource input,
                                                                final ItemResource selectedOutput) {
        Platform.INSTANCE.sendPacketToServer(new PatternGridStonecutterRecipeTransferPacket(input, selectedOutput));
    }

    public static void sendPatternGridSmithingTableRecipeTransfer(final List<ItemResource> template,
                                                                  final List<ItemResource> base,
                                                                  final List<ItemResource> addition) {
        Platform.INSTANCE.sendPacketToServer(
            new PatternGridSmithingTableRecipeTransferPacket(template, base, addition)
        );
    }

    public static void sendAutocrafterNameChange(final String name) {
        Platform.INSTANCE.sendPacketToServer(new AutocrafterNameChangePacket(name));
    }

    public static void sendAutocraftingPreviewRequest(final UUID id,
                                                      final PlatformResourceKey resource,
                                                      final long amount,
                                                      final AutocraftingPreviewStyle style) {
        Platform.INSTANCE.sendPacketToServer(new AutocraftingPreviewRequestPacket(id, resource, amount, style));
    }

    public static void sendAutocraftingRequest(final UUID id,
                                               final PlatformResourceKey resource,
                                               final long amount,
                                               final boolean notify) {
        Platform.INSTANCE.sendPacketToServer(new AutocraftingRequestPacket(id, resource, amount, notify));
    }

    public static void sendAutocraftingPreviewCancelRequest() {
        Platform.INSTANCE.sendPacketToServer(AutocraftingPreviewCancelRequestPacket.INSTANCE);
    }

    public static void sendAutocraftingPreviewMaxAmountRequest(final PlatformResourceKey resource) {
        Platform.INSTANCE.sendPacketToServer(new AutocraftingPreviewMaxAmountRequestPacket(resource));
    }

    public static void sendAutocraftingMonitorCancel(final TaskId taskId) {
        Platform.INSTANCE.sendPacketToServer(new AutocraftingMonitorCancelPacket(taskId));
    }

    public static void sendAutocraftingMonitorCancelAll() {
        Platform.INSTANCE.sendPacketToServer(AutocraftingMonitorCancelAllPacket.INSTANCE);
    }
}
