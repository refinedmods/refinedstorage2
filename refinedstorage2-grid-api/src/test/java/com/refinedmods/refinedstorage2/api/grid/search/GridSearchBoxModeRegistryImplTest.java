package com.refinedmods.refinedstorage2.api.grid.search;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@Rs2Test
class GridSearchBoxModeRegistryImplTest {
    private final GridSearchBoxModeRegistry gridSearchBoxModeRegistry = new GridSearchBoxModeRegistryImpl();

    @Test
    void Test_getting_default_when_no_search_box_modes_are_present() {
        // Act
        Executable action = gridSearchBoxModeRegistry::getDefault;

        // Assert
        Exception e = assertThrows(IllegalStateException.class, action);
        assertThat(e.getMessage()).isEqualTo("No search box modes are available");
    }

    @Test
    void Test_adding_and_getting_search_box_mode() {
        // Arrange
        GridSearchBoxMode searchBoxMode = createSearchBoxMode();

        gridSearchBoxModeRegistry.add(searchBoxMode);
        gridSearchBoxModeRegistry.add(createSearchBoxMode());

        // Act
        GridSearchBoxMode foundSearchBoxMode = gridSearchBoxModeRegistry.get(0);

        // Assert
        assertThat(foundSearchBoxMode).isSameAs(searchBoxMode);
    }

    @Test
    void Test_getting_default_search_box_mode_on_negative_index() {
        // Arrange
        GridSearchBoxMode searchBoxMode = createSearchBoxMode();

        gridSearchBoxModeRegistry.add(searchBoxMode);
        gridSearchBoxModeRegistry.add(createSearchBoxMode());

        // Act
        GridSearchBoxMode foundNegativeIndex = gridSearchBoxModeRegistry.get(-1);

        // Assert
        assertThat(foundNegativeIndex).isSameAs(searchBoxMode);
    }

    @Test
    void Test_getting_default_search_box_mode_on_too_high_index() {
        // Arrange
        GridSearchBoxMode searchBoxMode = createSearchBoxMode();

        gridSearchBoxModeRegistry.add(searchBoxMode);
        gridSearchBoxModeRegistry.add(createSearchBoxMode());

        // Act
        GridSearchBoxMode foundTooHighIndex = gridSearchBoxModeRegistry.get(2);

        // Assert
        assertThat(foundTooHighIndex).isSameAs(searchBoxMode);
    }

    @Test
    void Test_getting_id_of_search_box_mode() {
        // Arrange
        GridSearchBoxMode searchBoxMode1 = createSearchBoxMode();
        GridSearchBoxMode searchBoxMode2 = createSearchBoxMode();

        gridSearchBoxModeRegistry.add(searchBoxMode1);
        gridSearchBoxModeRegistry.add(searchBoxMode2);

        // Act
        int index1 = gridSearchBoxModeRegistry.getId(searchBoxMode1);
        int index2 = gridSearchBoxModeRegistry.getId(searchBoxMode2);

        // Assert
        assertThat(index1).isZero();
        assertThat(index2).isEqualTo(1);
    }

    @Test
    void Test_getting_id_of_unknown_search_box_mode() {
        // Arrange
        gridSearchBoxModeRegistry.add(createSearchBoxMode());

        // Act
        int index = gridSearchBoxModeRegistry.getId(createSearchBoxMode());

        // Assert
        assertThat(index).isZero();
    }

    @Test
    void Test_getting_next_search_box_mode() {
        // Arrange
        GridSearchBoxMode a = createSearchBoxMode();
        GridSearchBoxMode b = createSearchBoxMode();

        gridSearchBoxModeRegistry.add(a);
        gridSearchBoxModeRegistry.add(b);

        // Act
        GridSearchBoxMode next = gridSearchBoxModeRegistry.next(a);

        // Assert
        assertThat(next).isSameAs(b);
    }

    @Test
    void Test_getting_next_search_box_mode_for_last_mode() {
        // Arrange
        GridSearchBoxMode a = createSearchBoxMode();
        GridSearchBoxMode b = createSearchBoxMode();

        gridSearchBoxModeRegistry.add(a);
        gridSearchBoxModeRegistry.add(b);

        // Act
        GridSearchBoxMode next = gridSearchBoxModeRegistry.next(b);

        // Assert
        assertThat(next).isSameAs(a);
    }

    @Test
    void Test_getting_next_search_box_mode_for_unknown_search_box_mode() {
        // Arrange
        GridSearchBoxMode a = createSearchBoxMode();
        GridSearchBoxMode b = createSearchBoxMode();

        gridSearchBoxModeRegistry.add(a);
        gridSearchBoxModeRegistry.add(b);

        // Act
        GridSearchBoxMode next = gridSearchBoxModeRegistry.next(createSearchBoxMode());

        // Assert
        assertThat(next).isSameAs(a);
    }

    private GridSearchBoxMode createSearchBoxMode() {
        return mock(GridSearchBoxMode.class);
    }
}
