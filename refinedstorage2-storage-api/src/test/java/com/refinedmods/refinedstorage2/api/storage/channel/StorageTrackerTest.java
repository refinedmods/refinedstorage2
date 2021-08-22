package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStackIdentifier;
import com.refinedmods.refinedstorage2.api.stack.test.ItemStubs;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class StorageTrackerTest {
    private final AtomicLong clock = new AtomicLong(0);
    private final StorageTracker<Rs2ItemStack, Rs2ItemStackIdentifier> tracker = new StorageTracker<>(Rs2ItemStackIdentifier::new, clock::get);

    @Test
    void Test_change() {
        // Act
        clock.set(1000);
        tracker.onChanged(new Rs2ItemStack(ItemStubs.DIRT), "Raoul");

        Optional<StorageTracker.Entry> entry = tracker.getEntry(new Rs2ItemStack(ItemStubs.DIRT));

        // Assert
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo("Raoul");
        assertThat(entry.get().getTime()).isEqualTo(1000);
    }

    @Test
    void Test_multiple_changes() {
        // Act
        clock.set(1000);
        tracker.onChanged(new Rs2ItemStack(ItemStubs.DIRT), "Raoul");
        Optional<StorageTracker.Entry> entry1 = tracker.getEntry(new Rs2ItemStack(ItemStubs.DIRT));

        clock.set(2000);
        tracker.onChanged(new Rs2ItemStack(ItemStubs.DIRT), "VdB");
        Optional<StorageTracker.Entry> entry2 = tracker.getEntry(new Rs2ItemStack(ItemStubs.DIRT));

        clock.set(3000);
        tracker.onChanged(new Rs2ItemStack(ItemStubs.GLASS), "Robin");

        Optional<StorageTracker.Entry> entry3 = tracker.getEntry(new Rs2ItemStack(ItemStubs.DIRT));

        // Assert
        assertThat(entry1).isPresent();
        assertThat(entry1.get().getName()).isEqualTo("Raoul");
        assertThat(entry1.get().getTime()).isEqualTo(1000);

        assertThat(entry2).isPresent();
        assertThat(entry2.get().getName()).isEqualTo("VdB");
        assertThat(entry2.get().getTime()).isEqualTo(2000);

        assertThat(entry3).isPresent();
        assertThat(entry3.get().getName()).isEqualTo("VdB");
        assertThat(entry3.get().getTime()).isEqualTo(2000);
    }

    @Test
    void Test_getting_non_existent_entry() {
        // Act
        Optional<StorageTracker.Entry> entry = tracker.getEntry(new Rs2ItemStack(ItemStubs.SPONGE));

        // Act
        assertThat(entry).isEmpty();
    }
}
