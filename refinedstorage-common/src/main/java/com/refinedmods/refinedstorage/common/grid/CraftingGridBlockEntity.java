package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class CraftingGridBlockEntity extends AbstractGridBlockEntity
    implements NetworkNodeExtendedMenuProvider<GridData>, CraftingGrid {
    private static final String TAG_MATRIX = "matrix";

    private final RecipeMatrix<CraftingRecipe, CraftingInput> craftingRecipe = RecipeMatrix.crafting(
        this::setChanged,
        this::getLevel
    );

    public CraftingGridBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getCraftingGrid(),
            pos,
            state,
            Platform.INSTANCE.getConfig().getCraftingGrid().getEnergyUsage()
        );
    }

    @Override
    public RecipeMatrixContainer getCraftingMatrix() {
        return craftingRecipe.getMatrix();
    }

    @Override
    public ResultContainer getCraftingResult() {
        return craftingRecipe.getResult();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final Player player, final CraftingInput input) {
        return craftingRecipe.getRemainingItems(level, player, input);
    }

    @Override
    public ExtractTransaction startExtractTransaction(final Player player, final boolean directCommit) {
        return getNetwork()
            .map(network -> network.getComponent(StorageNetworkComponent.class))
            .map(storage -> directCommit
                ? new DirectCommitExtractTransaction(storage)
                : new SnapshotExtractTransaction(player, storage, getCraftingMatrix()))
            .orElse(ExtractTransaction.NOOP);
    }

    @Override
    public boolean clearMatrix(final Player player, final boolean toPlayerInventory) {
        return toPlayerInventory
            ? getCraftingMatrix().clearToPlayerInventory(player)
            : clearMatrixIntoStorage(player);
    }

    private boolean clearMatrixIntoStorage(final Player player) {
        return getNetwork()
            .map(network -> network.getComponent(StorageNetworkComponent.class))
            .map(storage -> getCraftingMatrix().clearIntoStorage(storage, player))
            .orElse(false);
    }

    @Override
    public void transferRecipe(final Player player, final List<List<ItemResource>> recipe) {
        getCraftingMatrix().transferRecipe(
            player,
            getNetwork().map(network -> network.getComponent(StorageNetworkComponent.class)).orElse(null),
            recipe
        );
    }

    @Override
    public void acceptQuickCraft(final Player player, final ItemStack craftedStack) {
        if (player.getInventory().add(craftedStack)) {
            return;
        }
        final long inserted = getNetwork()
            .map(network -> network.getComponent(StorageNetworkComponent.class))
            .map(rootStorage -> rootStorage.insert(
                ItemResource.ofItemStack(craftedStack),
                craftedStack.getCount(),
                Action.EXECUTE,
                new PlayerActor(player)
            ))
            .orElse(0L);
        if (inserted != craftedStack.getCount()) {
            final long remainder = craftedStack.getCount() - inserted;
            final ItemStack remainderStack = craftedStack.copyWithCount((int) remainder);
            player.drop(remainderStack, false);
        }
    }

    @Override
    public GridData getMenuData() {
        return GridData.of(this);
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, GridData> getMenuCodec() {
        return GridData.STREAM_CODEC;
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.CRAFTING_GRID);
    }

    @Override
    @Nullable
    public AbstractGridContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new CraftingGridContainerMenu(syncId, inventory, this);
    }

    @Override
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store(TAG_MATRIX, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(craftingRecipe.getMatrix().getItems()));
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        input.read(TAG_MATRIX, ItemContainerContents.CODEC).ifPresent(craftingRecipe::load);
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        craftingRecipe.updateResult(level);
    }

    @Override
    public void preRemoveSideEffects(final BlockPos pos, final BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            final NonNullList<ItemStack> drops = NonNullList.create();
            for (int i = 0; i < craftingRecipe.getMatrix().getContainerSize(); ++i) {
                drops.add(craftingRecipe.getMatrix().getItem(i));
            }
            Containers.dropContents(level, pos, drops);
        }
    }

    private Optional<Network> getNetwork() {
        if (!mainNetworkNode.isActive()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mainNetworkNode.getNetwork());
    }
}
