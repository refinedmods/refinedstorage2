package com.refinedmods.refinedstorage2.platform.api.support.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;

import java.util.Collection;
import java.util.Optional;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.platform.test.TagHelper.createDummyTag;
import static org.assertj.core.api.Assertions.assertThat;

@SetupMinecraft
class FuzzyResourceListImplTest {
    private static final ItemResource DUMMY_A = new ItemResource(Items.DIRT, null);
    private static final ItemResource DUMMY_B = new ItemResource(Items.DIRT, createDummyTag("b"));
    private static final ItemResource DUMMY_C = new ItemResource(Items.DIRT, createDummyTag("c"));
    private static final ItemResource DUMMY_D = new ItemResource(Items.GLASS, null);
    private static final ItemResource DUMMY_E = new ItemResource(Items.DARK_OAK_DOOR, null);

    FuzzyResourceList<ItemResource> sut;

    @BeforeEach
    void setUp() {
        sut = new FuzzyResourceListImpl<ItemResource>(new ResourceListImpl<>());
    }

    @Test
    void testRetrievingFuzzy() {
        // Arrange
        sut.add(DUMMY_A, 5);
        sut.add(DUMMY_A, 5);
        sut.remove(DUMMY_A, 9);

        sut.add(DUMMY_B, 15);

        sut.add(DUMMY_C, 20);

        sut.add(DUMMY_D, 10);
        sut.add(DUMMY_D, 15);

        // Act
        final Optional<ResourceAmount<ItemResource>> strictA = sut.get(DUMMY_A);
        final Optional<ResourceAmount<ItemResource>> strictB = sut.get(DUMMY_B);
        final Optional<ResourceAmount<ItemResource>> strictC = sut.get(DUMMY_C);
        final Optional<ResourceAmount<ItemResource>> strictD = sut.get(DUMMY_D);
        final Optional<ResourceAmount<ItemResource>> strictE = sut.get(DUMMY_E);

        final Collection<ResourceAmount<ItemResource>> fuzzyA = sut.getFuzzy(DUMMY_A);
        final Collection<ResourceAmount<ItemResource>> fuzzyB = sut.getFuzzy(DUMMY_B);
        final Collection<ResourceAmount<ItemResource>> fuzzyC = sut.getFuzzy(DUMMY_C);
        final Collection<ResourceAmount<ItemResource>> fuzzyD = sut.getFuzzy(DUMMY_D);
        final Collection<ResourceAmount<ItemResource>> fuzzyE = sut.getFuzzy(DUMMY_E);

        // Assert
        assertThat(strictA).get().usingRecursiveComparison().isEqualTo(new ResourceAmount<>(DUMMY_A, 1));
        assertThat(strictB).get().usingRecursiveComparison().isEqualTo(new ResourceAmount<>(DUMMY_B, 15));
        assertThat(strictC).get().usingRecursiveComparison().isEqualTo(new ResourceAmount<>(DUMMY_C, 20));
        assertThat(strictD).get().usingRecursiveComparison().isEqualTo(new ResourceAmount<>(DUMMY_D, 25));
        assertThat(strictE).isNotPresent();

        assertThat(fuzzyA).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>(DUMMY_A, 1),
            new ResourceAmount<>(DUMMY_B, 15),
            new ResourceAmount<>(DUMMY_C, 20)
        );
        assertThat(fuzzyB).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>(DUMMY_A, 1),
            new ResourceAmount<>(DUMMY_B, 15),
            new ResourceAmount<>(DUMMY_C, 20)
        );
        assertThat(fuzzyC).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>(DUMMY_A, 1),
            new ResourceAmount<>(DUMMY_B, 15),
            new ResourceAmount<>(DUMMY_C, 20)
        );
        assertThat(fuzzyD).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>(DUMMY_D, 25)
        );
        assertThat(fuzzyE).isEmpty();
    }

    @Test
    void shouldRemoveEntireResourceFromFuzzyIndexAfterRemoval() {
        // Arrange
        sut.add(DUMMY_A, 5);
        sut.add(DUMMY_A, 5);
        sut.remove(DUMMY_A, 10);

        sut.add(DUMMY_B, 15);
        sut.remove(DUMMY_B, 15);

        sut.add(DUMMY_C, 20);
        sut.remove(DUMMY_C, 20);

        sut.add(DUMMY_D, 10);
        sut.add(DUMMY_D, 15);

        // Act
        final Optional<ResourceAmount<ItemResource>> strictA = sut.get(DUMMY_A);
        final Optional<ResourceAmount<ItemResource>> strictB = sut.get(DUMMY_B);
        final Optional<ResourceAmount<ItemResource>> strictC = sut.get(DUMMY_C);
        final Optional<ResourceAmount<ItemResource>> strictD = sut.get(DUMMY_D);
        final Optional<ResourceAmount<ItemResource>> strictE = sut.get(DUMMY_E);

        final Collection<ResourceAmount<ItemResource>> fuzzyA = sut.getFuzzy(DUMMY_A);
        final Collection<ResourceAmount<ItemResource>> fuzzyB = sut.getFuzzy(DUMMY_B);
        final Collection<ResourceAmount<ItemResource>> fuzzyC = sut.getFuzzy(DUMMY_C);
        final Collection<ResourceAmount<ItemResource>> fuzzyD = sut.getFuzzy(DUMMY_D);
        final Collection<ResourceAmount<ItemResource>> fuzzyE = sut.getFuzzy(DUMMY_E);

        // Assert
        assertThat(strictA).isNotPresent();
        assertThat(strictB).isNotPresent();
        assertThat(strictC).isNotPresent();
        assertThat(strictD).get().usingRecursiveComparison().isEqualTo(new ResourceAmount<>(DUMMY_D, 25));
        assertThat(strictE).isNotPresent();

        assertThat(fuzzyA).isEmpty();
        assertThat(fuzzyB).isEmpty();
        assertThat(fuzzyC).isEmpty();
        assertThat(fuzzyD).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>(DUMMY_D, 25)
        );
        assertThat(fuzzyE).isEmpty();
    }

    @Test
    void shouldRemoveSingleResourceFromFuzzyIndexAfterRemoval() {
        // Arrange
        sut.add(DUMMY_A, 5);
        sut.add(DUMMY_A, 5);
        sut.remove(DUMMY_A, 10);

        sut.add(DUMMY_B, 15);

        sut.add(DUMMY_C, 20);

        sut.add(DUMMY_D, 10);
        sut.add(DUMMY_D, 15);

        // Act
        final Optional<ResourceAmount<ItemResource>> strictA = sut.get(DUMMY_A);
        final Optional<ResourceAmount<ItemResource>> strictB = sut.get(DUMMY_B);
        final Optional<ResourceAmount<ItemResource>> strictC = sut.get(DUMMY_C);
        final Optional<ResourceAmount<ItemResource>> strictD = sut.get(DUMMY_D);
        final Optional<ResourceAmount<ItemResource>> strictE = sut.get(DUMMY_E);

        final Collection<ResourceAmount<ItemResource>> fuzzyA = sut.getFuzzy(DUMMY_A);
        final Collection<ResourceAmount<ItemResource>> fuzzyB = sut.getFuzzy(DUMMY_B);
        final Collection<ResourceAmount<ItemResource>> fuzzyC = sut.getFuzzy(DUMMY_C);
        final Collection<ResourceAmount<ItemResource>> fuzzyD = sut.getFuzzy(DUMMY_D);
        final Collection<ResourceAmount<ItemResource>> fuzzyE = sut.getFuzzy(DUMMY_E);

        // Assert
        assertThat(strictA).isNotPresent();
        assertThat(strictB).get().usingRecursiveComparison().isEqualTo(new ResourceAmount<>(DUMMY_B, 15));
        assertThat(strictC).get().usingRecursiveComparison().isEqualTo(new ResourceAmount<>(DUMMY_C, 20));
        assertThat(strictD).get().usingRecursiveComparison().isEqualTo(new ResourceAmount<>(DUMMY_D, 25));
        assertThat(strictE).isNotPresent();

        assertThat(fuzzyA).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>(DUMMY_B, 15),
            new ResourceAmount<>(DUMMY_C, 20)
        );
        assertThat(fuzzyB).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>(DUMMY_B, 15),
            new ResourceAmount<>(DUMMY_C, 20)
        );
        assertThat(fuzzyC).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>(DUMMY_B, 15),
            new ResourceAmount<>(DUMMY_C, 20)
        );
        assertThat(fuzzyD).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>(DUMMY_D, 25)
        );
        assertThat(fuzzyE).isEmpty();
    }
}
