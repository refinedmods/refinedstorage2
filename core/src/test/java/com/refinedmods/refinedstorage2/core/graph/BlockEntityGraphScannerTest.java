package com.refinedmods.refinedstorage2.core.graph;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeWorldAdapter;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class BlockEntityGraphScannerTest {
    @Test
    void Test_scanning_from_origin_contains_origin() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());

        worldAdapter.setBlockEntity(new BlockPos(10, 10, 10), new FurnaceBlockEntity());

        GraphScanner<FurnaceBlockEntity> scanner = new BlockEntityGraphScanner<>(FurnaceBlockEntity.class);

        // Act
        GraphScannerResult<FurnaceBlockEntity> result = scanner.scanAt(worldAdapter, BlockPos.ORIGIN);

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01)
        );
    }

    @Test
    void Test_scanning_does_not_connect_to_incompatible_neighbor() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new BeehiveBlockEntity());

        worldAdapter.setBlockEntity(new BlockPos(10, 10, 10), new FurnaceBlockEntity());

        GraphScanner<FurnaceBlockEntity> scanner = new BlockEntityGraphScanner<>(FurnaceBlockEntity.class);

        // Act
        GraphScannerResult<FurnaceBlockEntity> result = scanner.scanAt(worldAdapter, BlockPos.ORIGIN);

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01)
        );
    }

    @Test
    void Test_scanning_does_connect_to_compatible_neighbor() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        FurnaceBlockEntity b02 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new FurnaceBlockEntity());
        FurnaceBlockEntity b03 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.up(), new FurnaceBlockEntity());

        worldAdapter.setBlockEntity(new BlockPos(10, 10, 10), new FurnaceBlockEntity());

        GraphScanner<FurnaceBlockEntity> scanner = new BlockEntityGraphScanner<>(FurnaceBlockEntity.class);

        // Act
        GraphScannerResult<FurnaceBlockEntity> result = scanner.scanAt(worldAdapter, BlockPos.ORIGIN);

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down(), b02),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.up(), b03)
        );
    }

    @Test
    void Test_scanning_blocks_compatible_neighbors_if_incompatible_neighbor_is_in_between() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new BeehiveBlockEntity());
        worldAdapter.setBlockEntity(BlockPos.ORIGIN.down().down(), new FurnaceBlockEntity());

        worldAdapter.setBlockEntity(new BlockPos(10, 10, 10), new FurnaceBlockEntity());

        GraphScanner<FurnaceBlockEntity> scanner = new BlockEntityGraphScanner<>(FurnaceBlockEntity.class);

        // Act
        GraphScannerResult<FurnaceBlockEntity> result = scanner.scanAt(worldAdapter, BlockPos.ORIGIN);

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01)
        );
    }

    @Test
    void Test_scanning_can_still_find_neighbor_with_alternate_route_if_is_blocked() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new BeehiveBlockEntity());
        FurnaceBlockEntity b02 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down().down(), new FurnaceBlockEntity());
        FurnaceBlockEntity b03 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.north(), new FurnaceBlockEntity());
        FurnaceBlockEntity b04 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.north().down(), new FurnaceBlockEntity());
        FurnaceBlockEntity b05 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.north().down().down(), new FurnaceBlockEntity());

        worldAdapter.setBlockEntity(new BlockPos(10, 10, 10), new FurnaceBlockEntity());

        GraphScanner<FurnaceBlockEntity> scanner = new BlockEntityGraphScanner<>(FurnaceBlockEntity.class);

        // Act
        GraphScannerResult<FurnaceBlockEntity> result = scanner.scanAt(worldAdapter, BlockPos.ORIGIN);

        // Assert
        assertThat(result.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down().down(), b02),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.north(), b03),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.north().down(), b04),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.north().down().down(), b05)
        );
    }

    @Test
    void Test_scanner_can_detect_new_entries() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());

        GraphScanner<FurnaceBlockEntity> scanner = new BlockEntityGraphScanner<>(FurnaceBlockEntity.class);

        // Act
        GraphScannerResult<FurnaceBlockEntity> result1 = scanner.scanAt(worldAdapter, BlockPos.ORIGIN);

        FurnaceBlockEntity b02 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new FurnaceBlockEntity());

        GraphScannerResult<FurnaceBlockEntity> result2 = scanner.scanAt(worldAdapter, BlockPos.ORIGIN, result1.getAllEntries());

        // Assert
        assertThat(result1.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01)
        );
        assertThat(result1.getRemovedEntries()).isEmpty();
        assertThat(result1.getNewEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01)
        );

        assertThat(result2.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down(), b02)
        );
        assertThat(result1.getRemovedEntries()).isEmpty();
        assertThat(result2.getNewEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down(), b02)
        );
    }

    @Test
    void Test_scanner_can_detect_removed_entries() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b00 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.up(), new FurnaceBlockEntity());
        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        FurnaceBlockEntity b02 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new FurnaceBlockEntity());
        FurnaceBlockEntity b03 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down().down(), new FurnaceBlockEntity());

        GraphScanner<FurnaceBlockEntity> scanner = new BlockEntityGraphScanner<>(FurnaceBlockEntity.class);

        // Act
        GraphScannerResult<FurnaceBlockEntity> result1 = scanner.scanAt(worldAdapter, BlockPos.ORIGIN);

        worldAdapter.removeBlockEntity(BlockPos.ORIGIN.down());

        GraphScannerResult<FurnaceBlockEntity> result2 = scanner.scanAt(worldAdapter, BlockPos.ORIGIN, result1.getAllEntries());

        // Assert
        assertThat(result1.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.up(), b00),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down(), b02),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down().down(), b03)
        );
        assertThat(result1.getRemovedEntries()).isEmpty();
        assertThat(result1.getNewEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.up(), b00),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down(), b02),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down().down(), b03)
        );

        assertThat(result2.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.up(), b00),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01)
        );
        assertThat(result2.getRemovedEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down(), b02),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down().down(), b03)
        );
        assertThat(result2.getNewEntries()).isEmpty();
    }

    @Test
    void Test_scanner_can_detect_new_and_removed_entries() {
        // Arrange
        FakeWorldAdapter worldAdapter = new FakeWorldAdapter();

        FurnaceBlockEntity b00 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.up(), new FurnaceBlockEntity());
        FurnaceBlockEntity b01 = worldAdapter.setBlockEntity(BlockPos.ORIGIN, new FurnaceBlockEntity());
        FurnaceBlockEntity b02 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down().down(), new FurnaceBlockEntity());

        GraphScanner<FurnaceBlockEntity> scanner = new BlockEntityGraphScanner<>(FurnaceBlockEntity.class);

        // Act
        GraphScannerResult<FurnaceBlockEntity> result1 = scanner.scanAt(worldAdapter, BlockPos.ORIGIN);

        worldAdapter.removeBlockEntity(BlockPos.ORIGIN.up());
        FurnaceBlockEntity b03 = worldAdapter.setBlockEntity(BlockPos.ORIGIN.down(), new FurnaceBlockEntity());

        GraphScannerResult<FurnaceBlockEntity> result2 = scanner.scanAt(worldAdapter, BlockPos.ORIGIN, result1.getAllEntries());

        // Assert
        assertThat(result1.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.up(), b00),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01)
        );
        assertThat(result1.getRemovedEntries()).isEmpty();
        assertThat(result1.getNewEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.up(), b00),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01)
        );

        assertThat(result2.getAllEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN, b01),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down(), b03),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down().down(), b02)
        );
        assertThat(result2.getRemovedEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.up(), b00)
        );
        assertThat(result2.getNewEntries()).containsExactlyInAnyOrder(
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down(), b03),
                new GraphEntry<>(FakeWorldAdapter.IDENTIFIER, BlockPos.ORIGIN.down().down(), b02)
        );
    }
}
