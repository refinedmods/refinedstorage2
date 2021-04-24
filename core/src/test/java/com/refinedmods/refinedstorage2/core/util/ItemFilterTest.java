package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.item.ItemStubs;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
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
        boolean allowed = filter.isAllowed(new Rs2ItemStack(ItemStubs.DIRT));

        // Assert
        assertThat(allowed).isTrue();
    }

    @Test
    void Test_empty_allowlist_allows_none() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        filter.setMode(FilterMode.ALLOW);

        // Act
        boolean allowed = filter.isAllowed(new Rs2ItemStack(ItemStubs.DIRT));

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    void Test_exact_allowlist() {
        // Arrange
        ItemFilter filter = new ItemFilter();

        filter.setMode(FilterMode.ALLOW);
        filter.setExact(true);
        setTemplates(filter, new Rs2ItemStack(ItemStubs.DIRT), new Rs2ItemStack(ItemStubs.STONE));

        // Act
        boolean allowsDirt = filter.isAllowed(new Rs2ItemStack(ItemStubs.DIRT));
        boolean allowsDirtWithTag = filter.isAllowed(applyTag(new Rs2ItemStack(ItemStubs.DIRT)));
        boolean allowsStone = filter.isAllowed(new Rs2ItemStack(ItemStubs.STONE));
        boolean allowsSponge = filter.isAllowed(new Rs2ItemStack(ItemStubs.SPONGE));

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
        setTemplates(filter, new Rs2ItemStack(ItemStubs.DIRT), new Rs2ItemStack(ItemStubs.STONE));

        // Act
        boolean allowsDirt = filter.isAllowed(new Rs2ItemStack(ItemStubs.DIRT));
        boolean allowsDirtWithTag = filter.isAllowed(applyTag(new Rs2ItemStack(ItemStubs.DIRT)));
        boolean allowsStone = filter.isAllowed(new Rs2ItemStack(ItemStubs.STONE));
        boolean allowsSponge = filter.isAllowed(new Rs2ItemStack(ItemStubs.SPONGE));

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
        setTemplates(filter, new Rs2ItemStack(ItemStubs.DIRT), new Rs2ItemStack(ItemStubs.STONE));

        // Act
        boolean allowsDirt = filter.isAllowed(new Rs2ItemStack(ItemStubs.DIRT));
        boolean allowsDirtWithTag = filter.isAllowed(applyTag(new Rs2ItemStack(ItemStubs.DIRT)));
        boolean allowsStone = filter.isAllowed(new Rs2ItemStack(ItemStubs.STONE));
        boolean allowsSponge = filter.isAllowed(new Rs2ItemStack(ItemStubs.SPONGE));

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
        setTemplates(filter, new Rs2ItemStack(ItemStubs.DIRT), new Rs2ItemStack(ItemStubs.STONE));

        // Act
        boolean allowsDirt = filter.isAllowed(new Rs2ItemStack(ItemStubs.DIRT));
        boolean allowsDirtWithTag = filter.isAllowed(applyTag(new Rs2ItemStack(ItemStubs.DIRT)));
        boolean allowsStone = filter.isAllowed(new Rs2ItemStack(ItemStubs.STONE));
        boolean allowsSponge = filter.isAllowed(new Rs2ItemStack(ItemStubs.SPONGE));

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
        setTemplates(filter, new Rs2ItemStack(ItemStubs.STONE));

        boolean allowsDirt = filter.isAllowed(new Rs2ItemStack(ItemStubs.DIRT));
        boolean allowsStone = filter.isAllowed(new Rs2ItemStack(ItemStubs.STONE));
        boolean allowsSponge = filter.isAllowed(new Rs2ItemStack(ItemStubs.SPONGE));

        setTemplates(filter, new Rs2ItemStack(ItemStubs.SPONGE), new Rs2ItemStack(ItemStubs.DIRT));

        boolean allowsDirtAfter = filter.isAllowed(new Rs2ItemStack(ItemStubs.DIRT));
        boolean allowsStoneAfter = filter.isAllowed(new Rs2ItemStack(ItemStubs.STONE));
        boolean allowsSpongeAfter = filter.isAllowed(new Rs2ItemStack(ItemStubs.SPONGE));

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

        setTemplates(filter, new Rs2ItemStack(ItemStubs.STONE));

        // Act
        boolean allowsStone = filter.isAllowed(new Rs2ItemStack(ItemStubs.STONE));
        boolean allowsStoneWithTag = filter.isAllowed(applyTag(new Rs2ItemStack(ItemStubs.STONE)));

        filter.setExact(false);

        boolean allowsStoneAfter = filter.isAllowed(new Rs2ItemStack(ItemStubs.STONE));
        boolean allowsStoneWithTagAfter = filter.isAllowed(applyTag(new Rs2ItemStack(ItemStubs.STONE)));

        // Assert
        assertThat(allowsStone).isFalse();
        assertThat(allowsStoneWithTag).isTrue();

        assertThat(allowsStoneAfter).isFalse();
        assertThat(allowsStoneWithTagAfter).isFalse();
    }

    private void setTemplates(ItemFilter filter, Rs2ItemStack... templates) {
        filter.setTemplates(new ArrayList<>(Arrays.asList(templates)));
    }

    private Rs2ItemStack applyTag(Rs2ItemStack stack) {
        stack.setTag("bla");
        return stack;
    }
}
