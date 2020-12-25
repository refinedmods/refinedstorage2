package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class HistoryTest {
    @Test
    void Test_older() {
        // Arrange
        List<String> items = new ArrayList<>();
        items.add("A");
        items.add("B");
        items.add("C");

        History history = new History(items);

        // Act & assert
        assertThat(history.older()).isEqualTo("C");
        assertThat(history.older()).isEqualTo("B");
        assertThat(history.older()).isEqualTo("A");
        assertThat(history.older()).isEqualTo("A");
    }

    @Test
    void Test_newer_initially() {
        // Arrange
        List<String> items = new ArrayList<>();
        items.add("A");
        items.add("B");
        items.add("C");

        History history = new History(items);

        // Act & assert
        assertThat(history.newer()).isEqualTo("");
    }

    @Test
    void Test_older_and_newer() {
        // Arrange
        List<String> items = new ArrayList<>();
        items.add("A");
        items.add("B");
        items.add("C");

        History history = new History(items);

        // Act & assert
        assertThat(history.older()).isEqualTo("C");
        assertThat(history.older()).isEqualTo("B");
        assertThat(history.older()).isEqualTo("A");

        assertThat(history.newer()).isEqualTo("B");
        assertThat(history.newer()).isEqualTo("C");
        assertThat(history.newer()).isEqualTo("");
        assertThat(history.newer()).isEqualTo("");
        assertThat(history.newer()).isEqualTo("");

        assertThat(history.older()).isEqualTo("C");

        assertThat(history.newer()).isEqualTo("");

        assertThat(history.older()).isEqualTo("C");
        assertThat(history.older()).isEqualTo("B");

        assertThat(history.newer()).isEqualTo("C");
        assertThat(history.newer()).isEqualTo("");
    }

    @Test
    void Test_saving() {
        // Arrange
        History history = new History(new ArrayList<>());

        // Act & assert
        assertThat(history.save("Hello")).isTrue();
    }

    @Test
    void Test_saving_duplicates() {
        // Arrange
        History history = new History(new ArrayList<>());

        // Act & assert
        assertThat(history.save("Hello1")).isTrue();
        assertThat(history.save("Hello1")).isFalse();

        assertThat(history.save("Hello2")).isTrue();
        assertThat(history.save("Hello2")).isFalse();

        assertThat(history.save("Hello1")).isTrue();
        assertThat(history.save("Hello1")).isFalse();
    }

    @Test
    void Test_saving_empty_values() {
        // Arrange
        History history = new History(new ArrayList<>());

        // Act & assert
        assertThat(history.save("")).isFalse();
        assertThat(history.save("    ")).isFalse();
    }
}
