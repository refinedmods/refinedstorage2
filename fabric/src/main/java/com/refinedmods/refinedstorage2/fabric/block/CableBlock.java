package com.refinedmods.refinedstorage2.fabric.block;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeAdapter;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class CableBlock extends Block implements BlockEntityProvider {
    private static final BooleanProperty NORTH = BooleanProperty.of("north");
    private static final BooleanProperty EAST = BooleanProperty.of("east");
    private static final BooleanProperty SOUTH = BooleanProperty.of("south");
    private static final BooleanProperty WEST = BooleanProperty.of("west");
    private static final BooleanProperty UP = BooleanProperty.of("up");
    private static final BooleanProperty DOWN = BooleanProperty.of("down");

    private static final VoxelShape SHAPE_CORE = createCuboidShape(6, 6, 6, 10, 10, 10);
    private static final VoxelShape SHAPE_NORTH = createCuboidShape(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SHAPE_EAST = createCuboidShape(10, 6, 6, 16, 10, 10);
    private static final VoxelShape SHAPE_SOUTH = createCuboidShape(6, 6, 10, 10, 10, 16);
    private static final VoxelShape SHAPE_WEST = createCuboidShape(0, 6, 6, 6, 10, 10);
    private static final VoxelShape SHAPE_UP = createCuboidShape(6, 10, 6, 10, 16, 10);
    private static final VoxelShape SHAPE_DOWN = createCuboidShape(6, 0, 6, 10, 6, 10);

    public CableBlock() {
        super(FabricBlockSettings.of(Material.GLASS).sounds(BlockSoundGroup.GLASS).strength(0.35F, 0.35F));

        setDefaultState(getStateManager().getDefaultState()
            .with(NORTH, false)
            .with(EAST, false)
            .with(SOUTH, false)
            .with(WEST, false)
            .with(UP, false)
            .with(DOWN, false));
    }


    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (world instanceof ServerWorld)
            RefinedStorage2Mod.API.getNetworkManager((ServerWorld) world).onNodeAdded(new FabricNetworkNodeAdapter(world), (NetworkNode) world.getBlockEntity(pos));

        System.out.println("Placed! " + world.getBlockEntity(pos));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        return getState(state, world, pos);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getState(getDefaultState(), ctx.getWorld(), ctx.getBlockPos());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = SHAPE_CORE;

        if (Boolean.TRUE.equals(state.get(NORTH))) {
            shape = VoxelShapes.union(shape, SHAPE_NORTH);
        }

        if (Boolean.TRUE.equals(state.get(EAST))) {
            shape = VoxelShapes.union(shape, SHAPE_EAST);
        }

        if (Boolean.TRUE.equals(state.get(SOUTH))) {
            shape = VoxelShapes.union(shape, SHAPE_SOUTH);
        }

        if (Boolean.TRUE.equals(state.get(WEST))) {
            shape = VoxelShapes.union(shape, SHAPE_WEST);
        }

        if (Boolean.TRUE.equals(state.get(UP))) {
            shape = VoxelShapes.union(shape, SHAPE_UP);
        }

        if (Boolean.TRUE.equals(state.get(DOWN))) {
            shape = VoxelShapes.union(shape, SHAPE_DOWN);
        }

        return shape;
    }

    private boolean hasConnection(WorldAccess world, BlockPos pos, Direction direction) {
        return world.getBlockState(pos).getBlock() instanceof CableBlock;
    }

    private BlockState getState(BlockState currentState, WorldAccess world, BlockPos pos) {
        boolean north = hasConnection(world, pos.offset(Direction.NORTH), Direction.SOUTH);
        boolean east = hasConnection(world, pos.offset(Direction.EAST), Direction.WEST);
        boolean south = hasConnection(world, pos.offset(Direction.SOUTH), Direction.NORTH);
        boolean west = hasConnection(world, pos.offset(Direction.WEST), Direction.EAST);
        boolean up = hasConnection(world, pos.offset(Direction.UP), Direction.DOWN);
        boolean down = hasConnection(world, pos.offset(Direction.DOWN), Direction.UP);

        return currentState
            .with(NORTH, north)
            .with(EAST, east)
            .with(SOUTH, south)
            .with(WEST, west)
            .with(UP, up)
            .with(DOWN, down);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockView world) {
        return new CableBlockEntity();
    }
}
