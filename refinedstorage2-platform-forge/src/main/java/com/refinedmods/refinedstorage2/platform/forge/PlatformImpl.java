package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.AbstractPlatform;
import com.refinedmods.refinedstorage2.platform.common.Config;
import com.refinedmods.refinedstorage2.platform.common.ContainedFluid;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.TransferManager;
import com.refinedmods.refinedstorage2.platform.common.util.BucketAmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.util.CustomBlockPlaceContext;
import com.refinedmods.refinedstorage2.platform.forge.grid.strategy.ItemGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.grid.view.ForgeFluidGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.forge.grid.view.ForgeItemGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;
import com.refinedmods.refinedstorage2.platform.forge.packet.c2s.ClientToServerCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.forge.packet.s2c.ServerToClientCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.forge.support.containermenu.ContainerTransferDestination;
import com.refinedmods.refinedstorage2.platform.forge.support.containermenu.MenuOpenerImpl;
import com.refinedmods.refinedstorage2.platform.forge.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage2.platform.forge.support.render.FluidStackFluidRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.ofFluidStack;
import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.toFluidStack;

public final class PlatformImpl extends AbstractPlatform {
    private static final TagKey<Item> WRENCH_TAG = TagKey.create(
        ForgeRegistries.ITEMS.getRegistryKey(),
        new ResourceLocation("forge", "tools/wrench")
    );

    private final ConfigImpl config = new ConfigImpl();

    public PlatformImpl(final NetworkManager networkManager) {
        super(
            new ServerToClientCommunicationsImpl(networkManager),
            new ClientToServerCommunicationsImpl(networkManager),
            new MenuOpenerImpl(),
            new BucketAmountFormatting(FluidType.BUCKET_VOLUME),
            new FluidStackFluidRenderer(),
            ItemGridInsertionStrategy::new
        );
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.getSpec());
    }

    @Override
    public long getBucketAmount() {
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public TagKey<Item> getWrenchTag() {
        return WRENCH_TAG;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public boolean canEditBoxLoseFocus(final EditBox editBox) {
        return editBox.canLoseFocus;
    }

    @Override
    public boolean isKeyDown(final KeyMapping keyMapping) {
        return !keyMapping.isUnbound() && InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow().getWindow(),
            keyMapping.getKey().getValue()
        );
    }

    @Override
    public GridResourceFactory getItemGridResourceFactory() {
        return new ForgeItemGridResourceFactory();
    }

    @Override
    public GridResourceFactory getFluidGridResourceFactory() {
        return new ForgeFluidGridResourceFactory();
    }

    @Override
    public Optional<ContainedFluid> getContainedFluid(final ItemStack stack) {
        final FluidTank tank = new FluidTank(Integer.MAX_VALUE);
        final FluidActionResult result = FluidUtil.tryEmptyContainer(
            stack,
            tank,
            Integer.MAX_VALUE,
            null,
            true
        );
        if (!result.isSuccess() || tank.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ContainedFluid(
            result.getResult(),
            new ResourceAmount<>(ofFluidStack(tank.getFluid()), tank.getFluidAmount())
        ));
    }

    @Override
    public Optional<FluidResource> convertJeiIngredientToFluid(final Object ingredient) {
        if (ingredient instanceof FluidStack fluidStack) {
            return Optional.of(ofFluidStack(fluidStack));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ItemStack> convertToBucket(final FluidResource fluidResource) {
        return new ItemStack(Items.BUCKET).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).map(dest -> {
            dest.fill(
                toFluidStack(fluidResource, FluidType.BUCKET_VOLUME),
                IFluidHandler.FluidAction.EXECUTE
            );
            return dest.getContainer();
        });
    }

    @Override
    public TransferManager createTransferManager(final AbstractContainerMenu containerMenu) {
        return new TransferManager(containerMenu, ContainerTransferDestination::new);
    }

    @Override
    public long insertIntoContainer(final Container container,
                                    final ItemResource itemResource,
                                    final long amount,
                                    final Action action) {
        final InvWrapper wrapper = new InvWrapper(container);
        final ItemStack stack = itemResource.toItemStack(amount);
        final ItemStack remainder = ItemHandlerHelper.insertItem(wrapper, stack, action == Action.SIMULATE);
        return amount - remainder.getCount();
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state,
                                       final Level level,
                                       final BlockHitResult hitResult,
                                       final Player player) {
        return state.getCloneItemStack(hitResult, level, hitResult.getBlockPos(), player);
    }

    @Override
    public NonNullList<ItemStack> getRemainingCraftingItems(final Player player,
                                                            final CraftingRecipe craftingRecipe,
                                                            final CraftingContainer container) {
        ForgeHooks.setCraftingPlayer(player);
        final NonNullList<ItemStack> remainingItems = craftingRecipe.getRemainingItems(container);
        ForgeHooks.setCraftingPlayer(null);
        return remainingItems;
    }

    @Override
    public void onItemCrafted(final Player player, final ItemStack craftedStack, final CraftingContainer container) {
        ForgeEventFactory.firePlayerCraftingEvent(player, craftedStack, container);
    }

    @Override
    public Player getFakePlayer(final ServerLevel level, @Nullable final UUID playerId) {
        return Optional.ofNullable(playerId)
            .flatMap(id -> level.getServer().getProfileCache().get(id))
            .map(profile -> FakePlayerFactory.get(level, profile))
            .orElseGet(() -> FakePlayerFactory.getMinecraft(level));
    }

    @Override
    public boolean canBreakBlock(final Level level, final BlockPos pos, final BlockState state, final Player player) {
        final BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(level, pos, state, player);
        return !MinecraftForge.EVENT_BUS.post(e);
    }

    @Override
    public boolean placeBlock(
        final Level level,
        final BlockPos pos,
        final Direction direction,
        final Player player,
        final ItemStack stack
    ) {
        final BlockPlaceContext ctx = new CustomBlockPlaceContext(
            level,
            player,
            InteractionHand.MAIN_HAND,
            stack,
            new BlockHitResult(Vec3.ZERO, direction, pos, false)
        );
        final InteractionResult result = ForgeHooks.onPlaceItemIntoWorld(ctx);
        return result.consumesAction();
    }

    @Override
    public boolean placeFluid(
        final Level level,
        final BlockPos pos,
        final Direction direction,
        final Player player,
        final FluidResource fluidResource
    ) {
        if (level.getBlockState(pos).getFluidState().isSource()) {
            return false;
        }
        final FluidStack stack = toFluidStack(fluidResource, FluidType.BUCKET_VOLUME);
        final FluidTank tank = new FluidTank(FluidType.BUCKET_VOLUME);
        tank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        return FluidUtil.tryPlaceFluid(
            player,
            level,
            InteractionHand.MAIN_HAND,
            pos,
            tank,
            toFluidStack(fluidResource, FluidType.BUCKET_VOLUME)
        );
    }

    @Override
    public ItemStack getBlockAsItemStack(final Block block,
                                         final BlockState state,
                                         final Direction direction,
                                         final BlockGetter level,
                                         final BlockPos position,
                                         final Player player) {
        return block.getCloneItemStack(
            state,
            new BlockHitResult(Vec3.ZERO, direction, position, false),
            level,
            position,
            player
        );
    }

    @Override
    public Optional<SoundEvent> getBucketPickupSound(final LiquidBlock liquidBlock, final BlockState state) {
        return liquidBlock.getPickupSound(state);
    }

    @Override
    public List<ClientTooltipComponent> processTooltipComponents(
        final ItemStack stack,
        final GuiGraphics graphics,
        final int mouseX,
        final Optional<TooltipComponent> imageComponent,
        final List<Component> components
    ) {
        return new ArrayList<>(ForgeHooksClient.gatherTooltipComponents(
            stack,
            components,
            imageComponent,
            mouseX,
            graphics.guiWidth(),
            graphics.guiHeight(),
            Minecraft.getInstance().font
        )); // make modifiable
    }

    @Override
    public void renderTooltip(final GuiGraphics graphics,
                              final List<ClientTooltipComponent> components,
                              final int x,
                              final int y) {
        graphics.renderTooltipInternal(
            Minecraft.getInstance().font,
            components,
            x,
            y,
            DefaultTooltipPositioner.INSTANCE
        );
    }

    @Override
    public Optional<EnergyStorage> getEnergyStorage(final ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
            .filter(EnergyStorageAdapter.class::isInstance)
            .map(EnergyStorageAdapter.class::cast)
            .map(EnergyStorageAdapter::getEnergyStorage);
    }
}
