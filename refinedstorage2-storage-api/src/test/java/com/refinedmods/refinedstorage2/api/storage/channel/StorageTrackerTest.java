package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class StorageTrackerTest {
    private final AtomicLong clock = new AtomicLong(0);
    private final StorageTracker<String> tracker = new StorageTracker<>(clock::get);

    @Test
    void Test_change() {
        // Act

        clock.set(1000);
        tracker.onChanged("A", "Raoul");

        Optional<StorageTracker.Entry> entry = tracker.getEntry("A");

        // Assert
        assertThat(entry).isPresent();
        assertThat(entry.get().name()).isEqualTo("Raoul");
        assertThat(entry.get().time()).isEqualTo(1000);
    }

    @Test
    void Test_multiple_changes() {
        // Act
        clock.set(1000);
        tracker.onChanged("A", "Raoul");
        Optional<StorageTracker.Entry> entry1 = tracker.getEntry("A");

        clock.set(2000);
        tracker.onChanged("A", "VdB");
        Optional<StorageTracker.Entry> entry2 = tracker.getEntry("A");

        clock.set(3000);
        tracker.onChanged("B", "Robin");

        Optional<StorageTracker.Entry> entry3 = tracker.getEntry("A");

        // Assert
        assertThat(entry1).isPresent();
        assertThat(entry1.get().name()).isEqualTo("Raoul");
        assertThat(entry1.get().time()).isEqualTo(1000);

        assertThat(entry2).isPresent();
        assertThat(entry2.get().name()).isEqualTo("VdB");
        assertThat(entry2.get().time()).isEqualTo(2000);

        assertThat(entry3).isPresent();
        assertThat(entry3.get().name()).isEqualTo("VdB");
        assertThat(entry3.get().time()).isEqualTo(2000);
    }

    @Test
    void Test_getting_non_existent_entry() {
        // Act
        Optional<StorageTracker.Entry> entry = tracker.getEntry("X");

        // Act
        assertThat(entry).isEmpty();
    }
}
