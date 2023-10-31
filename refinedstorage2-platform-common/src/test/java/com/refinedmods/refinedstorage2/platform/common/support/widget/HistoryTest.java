package com.refinedmods.refinedstorage2.platform.common.support.widget;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryTest {
    private List<String> simpleItems() {
        final List<String> items = new ArrayList<>();
        items.add("A");
        items.add("B");
        items.add("C");
        return items;
    }

    @Test
    void shouldRetrieveOlderItems() {
        // Arrange
        final History history = new History(simpleItems());

        // Act & assert
        assertThat(history.older()).isEqualTo("C");
        assertThat(history.older()).isEqualTo("B");
        assertThat(history.older()).isEqualTo("A");
        assertThat(history.older()).isEqualTo("A");
    }

    @Test
    void shouldNotRetrieveOlderItemIfThereIsNoHistory() {
        // Arrange
        final History history = new History(new ArrayList<>());

        // Act & assert
        assertThat(history.older()).isEmpty();
    }

    @Test
    void shouldNotRetrieveNewerItemIfAtTopOfHistory() {
        // Arrange
        final History history = new History(simpleItems());

        // Act & assert
        assertThat(history.newer()).isEmpty();
    }

    @Test
    void shouldBeAbleToSwitchBetweenOlderAndNewer() {
        // Arrange
        final History history = new History(simpleItems());

        // Act & assert
        assertThat(history.older()).isEqualTo("C");
        assertThat(history.older()).isEqualTo("B");
        assertThat(history.older()).isEqualTo("A");

        assertThat(history.newer()).isEqualTo("B");
        assertThat(history.newer()).isEqualTo("C");
        assertThat(history.newer()).isEmpty();
        assertThat(history.newer()).isEmpty();
        assertThat(history.newer()).isEmpty();

        assertThat(history.older()).isEqualTo("C");

        assertThat(history.newer()).isEmpty();

        assertThat(history.older()).isEqualTo("C");
        assertThat(history.older()).isEqualTo("B");

        assertThat(history.newer()).isEqualTo("C");
        assertThat(history.newer()).isEmpty();
    }

    @Test
    void shouldBeAbleToSaveItem() {
        // Arrange
        final History history = new History(new ArrayList<>());

        // Act & assert
        assertThat(history.save("Hello")).isTrue();
    }

    @Test
    void shouldNotSaveDuplicateItems() {
        // Arrange
        final History history = new History(new ArrayList<>());

        // Act & assert
        assertThat(history.save("Hello1")).isTrue();
        assertThat(history.save("Hello1")).isFalse();

        assertThat(history.save("Hello2")).isTrue();
        assertThat(history.save("Hello2")).isFalse();

        assertThat(history.save("Hello1")).isTrue();
        assertThat(history.save("Hello1")).isFalse();
    }

    @Test
    void shouldNotSaveEmptyItems() {
        // Arrange
        final History history = new History(new ArrayList<>());

        // Act & assert
        assertThat(history.save("")).isFalse();
        assertThat(history.save("    ")).isFalse();
    }
}
