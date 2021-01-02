package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.util.ItemStackIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class StorageTrackerTest {
    private final MutableLong clock = new MutableLong(0);
    private final StorageTracker<ItemStack, ItemStackIdentifier> tracker = new StorageTracker<>(ItemStackIdentifier::new, clock::getValue);

    @Test
    void Test_change() {
        // Act
        clock.setValue(1000);
        tracker.onChanged(new ItemStack(Items.DIRT), "Raoul");

        Optional<StorageTracker.Entry> entry = tracker.getEntry(new ItemStack(Items.DIRT));

        // Assert
        assertThat(entry).isPresent();
        assertThat(entry.get().getName()).isEqualTo("Raoul");
        assertThat(entry.get().getTime()).isEqualTo(1000);
    }

    @Test
    void Test_multiple_changes() {
        // Act
        clock.setValue(1000);
        tracker.onChanged(new ItemStack(Items.DIRT), "Raoul");
        Optional<StorageTracker.Entry> entry1 = tracker.getEntry(new ItemStack(Items.DIRT));

        clock.setValue(2000);
        tracker.onChanged(new ItemStack(Items.DIRT), "VdB");
        Optional<StorageTracker.Entry> entry2 = tracker.getEntry(new ItemStack(Items.DIRT));

        clock.setValue(3000);
        tracker.onChanged(new ItemStack(Items.GLASS), "Robin");

        Optional<StorageTracker.Entry> entry3 = tracker.getEntry(new ItemStack(Items.DIRT));

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
        Optional<StorageTracker.Entry> entry = tracker.getEntry(new ItemStack(Items.SPONGE));

        // Act
        assertThat(entry).isEmpty();
    }
}
