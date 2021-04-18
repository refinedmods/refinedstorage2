package com.refinedmods.refinedstorage2.core.util;

import java.util.ArrayList;
import java.util.Arrays;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class ItemFilterTest {
    @Test
    void Test_defaults() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        // Assert
        assertThat(filter.getMode()).isEqualTo(FilterMode.BLOCK);
        assertThat(filter.isExact()).isTrue();
    }

    @Test
    void Test_empty_blocklist_allows_all() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        // Act
        boolean allowed = filter.isAllowed(new ItemStack(Items.DIRT));

        // Assert
        assertThat(allowed).isTrue();
    }

    @Test
    void Test_empty_allowlist_allows_none() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        filter.setMode(FilterMode.ALLOW);

        // Act
        boolean allowed = filter.isAllowed(new ItemStack(Items.DIRT));

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    void Test_exact_allowlist() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        filter.setMode(FilterMode.ALLOW);
        filter.setExact(true);
        setTemplates(filter, new ItemStack(Items.DIRT), new ItemStack(Items.STONE));

        // Act
        boolean allowsDirt = filter.isAllowed(new ItemStack(Items.DIRT));
        boolean allowsDirtWithTag = filter.isAllowed(applyTag(new ItemStack(Items.DIRT)));
        boolean allowsStone = filter.isAllowed(new ItemStack(Items.STONE));
        boolean allowsSponge = filter.isAllowed(new ItemStack(Items.SPONGE));

        // Assert
        assertThat(allowsDirt).isTrue();
        assertThat(allowsDirtWithTag).isFalse();
        assertThat(allowsStone).isTrue();
        assertThat(allowsSponge).isFalse();
    }

    @Test
    void Test_exact_blocklist() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        filter.setExact(true);
        setTemplates(filter, new ItemStack(Items.DIRT), new ItemStack(Items.STONE));

        // Act
        boolean allowsDirt = filter.isAllowed(new ItemStack(Items.DIRT));
        boolean allowsDirtWithTag = filter.isAllowed(applyTag(new ItemStack(Items.DIRT)));
        boolean allowsStone = filter.isAllowed(new ItemStack(Items.STONE));
        boolean allowsSponge = filter.isAllowed(new ItemStack(Items.SPONGE));

        // Assert
        assertThat(allowsDirt).isFalse();
        assertThat(allowsDirtWithTag).isTrue();
        assertThat(allowsStone).isFalse();
        assertThat(allowsSponge).isTrue();
    }

    @Test
    void Test_non_exact_allowlist() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        filter.setMode(FilterMode.ALLOW);
        filter.setExact(false);
        setTemplates(filter, new ItemStack(Items.DIRT), new ItemStack(Items.STONE));

        // Act
        boolean allowsDirt = filter.isAllowed(new ItemStack(Items.DIRT));
        boolean allowsDirtWithTag = filter.isAllowed(applyTag(new ItemStack(Items.DIRT)));
        boolean allowsStone = filter.isAllowed(new ItemStack(Items.STONE));
        boolean allowsSponge = filter.isAllowed(new ItemStack(Items.SPONGE));

        // Assert
        assertThat(allowsDirt).isTrue();
        assertThat(allowsDirtWithTag).isTrue();
        assertThat(allowsStone).isTrue();
        assertThat(allowsSponge).isFalse();
    }

    @Test
    void Test_non_exact_blocklist() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        filter.setExact(false);
        setTemplates(filter, new ItemStack(Items.DIRT), new ItemStack(Items.STONE));

        // Act
        boolean allowsDirt = filter.isAllowed(new ItemStack(Items.DIRT));
        boolean allowsDirtWithTag = filter.isAllowed(applyTag(new ItemStack(Items.DIRT)));
        boolean allowsStone = filter.isAllowed(new ItemStack(Items.STONE));
        boolean allowsSponge = filter.isAllowed(new ItemStack(Items.SPONGE));

        // Assert
        assertThat(allowsDirt).isFalse();
        assertThat(allowsDirtWithTag).isFalse();
        assertThat(allowsStone).isFalse();
        assertThat(allowsSponge).isTrue();
    }

    @Test
    void Test_changing_templates() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        // Act
        setTemplates(filter, new ItemStack(Items.STONE));

        boolean allowsDirt = filter.isAllowed(new ItemStack(Items.DIRT));
        boolean allowsStone = filter.isAllowed(new ItemStack(Items.STONE));
        boolean allowsSponge = filter.isAllowed(new ItemStack(Items.SPONGE));

        setTemplates(filter, new ItemStack(Items.SPONGE), new ItemStack(Items.DIRT));

        boolean allowsDirtAfter = filter.isAllowed(new ItemStack(Items.DIRT));
        boolean allowsStoneAfter = filter.isAllowed(new ItemStack(Items.STONE));
        boolean allowsSpongeAfter = filter.isAllowed(new ItemStack(Items.SPONGE));

        // Assert
        assertThat(allowsDirt).isTrue();
        assertThat(allowsStone).isFalse();
        assertThat(allowsSponge).isTrue();

        assertThat(allowsDirtAfter).isFalse();
        assertThat(allowsStoneAfter).isTrue();
        assertThat(allowsSpongeAfter).isFalse();
    }

    @Test
    void Test_changing_exact_mode() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        setTemplates(filter, new ItemStack(Items.STONE));

        // Act
        boolean allowsStone = filter.isAllowed(new ItemStack(Items.STONE));
        boolean allowsStoneWithTag = filter.isAllowed(applyTag(new ItemStack(Items.STONE)));

        filter.setExact(false);

        boolean allowsStoneAfter = filter.isAllowed(new ItemStack(Items.STONE));
        boolean allowsStoneWithTagAfter = filter.isAllowed(applyTag(new ItemStack(Items.STONE)));

        // Assert
        assertThat(allowsStone).isFalse();
        assertThat(allowsStoneWithTag).isTrue();

        assertThat(allowsStoneAfter).isFalse();
        assertThat(allowsStoneWithTagAfter).isFalse();
    }

    private void setTemplates(ItemFilter filter, ItemStack... templates) {
        filter.setTemplates(new ArrayList<>(Arrays.asList(templates)));
    }

    private ItemStack applyTag(ItemStack stack) {
        stack.setTag(new CompoundTag());
        stack.getTag().putString("bla", "bla");
        return stack;
    }
}
