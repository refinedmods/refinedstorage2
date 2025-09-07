package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.COBBLESTONE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PatternTest {
    @Test
    void shouldCreateInternalPattern() {
        // Act
        final Pattern sut = new Pattern(UUID.randomUUID(), PatternLayout.internal(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            ),
            List.of(
                new ResourceAmount(COBBLESTONE, 1)
            )
        ));

        // Assert
        assertThat(sut.layout().ingredients()).hasSize(2);
        final Ingredient firstIngredient = sut.layout().ingredients().getFirst();
        assertThat(firstIngredient.amount()).isEqualTo(1);
        assertThat(firstIngredient.inputs()).containsExactly(A, B);
        final Ingredient secondIngredient = sut.layout().ingredients().get(1);
        assertThat(secondIngredient.amount()).isEqualTo(2);
        assertThat(secondIngredient.inputs()).containsExactly(C);
        assertThat(sut.layout().outputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_LOG, 3),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        assertThat(sut.layout().byproducts()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(COBBLESTONE, 1));
        assertThat(sut.layout().type()).isEqualTo(PatternType.INTERNAL);
    }

    @Test
    void shouldCreateExternalPattern() {
        // Act
        final Pattern sut = new Pattern(UUID.randomUUID(), PatternLayout.external(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            )
        ));

        // Assert
        assertThat(sut.layout().ingredients()).hasSize(2);
        final Ingredient firstIngredient = sut.layout().ingredients().getFirst();
        assertThat(firstIngredient.amount()).isEqualTo(1);
        assertThat(firstIngredient.inputs()).containsExactly(A, B);
        final Ingredient secondIngredient = sut.layout().ingredients().get(1);
        assertThat(secondIngredient.amount()).isEqualTo(2);
        assertThat(secondIngredient.inputs()).containsExactly(C);
        assertThat(sut.layout().outputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_LOG, 3),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        assertThat(sut.layout().byproducts()).isEmpty();
        assertThat(sut.layout().type()).isEqualTo(PatternType.EXTERNAL);
    }

    @ParameterizedTest
    @EnumSource(PatternType.class)
    void shouldNotCreatePatternWithoutIngredients(final PatternType type) {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(UUID.randomUUID(), new PatternLayout(
            List.of(),
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            ),
            List.of(),
            type
        ));

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Ingredients cannot be empty");
    }

    @ParameterizedTest
    @EnumSource(PatternType.class)
    void shouldNotCreatePatternWithoutOutputs(final PatternType type) {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(UUID.randomUUID(), new PatternLayout(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            List.of(),
            List.of(),
            type
        ));

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Outputs cannot be empty");
    }

    @Test
    void shouldCopyIngredientsAndOutputsAndByproducts() {
        // Arrange
        final List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient(1, List.of(A, B)));
        final List<ResourceAmount> outputs = new ArrayList<>();
        outputs.add(new ResourceAmount(OAK_LOG, 3));
        final List<ResourceAmount> byproducts = new ArrayList<>();
        byproducts.add(new ResourceAmount(COBBLESTONE, 1));
        final Pattern sut = new Pattern(UUID.randomUUID(), PatternLayout.internal(ingredients, outputs, byproducts));

        // Act
        ingredients.add(new Ingredient(2, List.of(C)));
        outputs.add(new ResourceAmount(OAK_PLANKS, 4));
        byproducts.add(new ResourceAmount(OAK_LOG, 2));

        // Assert
        assertThat(sut.layout().ingredients()).hasSize(1);
        assertThat(sut.layout().outputs()).hasSize(1);
        assertThat(sut.layout().byproducts()).hasSize(1);
    }

    @Test
    void shouldNotBeAbleToModifyIngredientsAndOutputsAndByproducts() {
        // Arrange
        final Pattern sut = new Pattern(UUID.randomUUID(), PatternLayout.internal(
            List.of(new Ingredient(1, List.of(A))),
            List.of(new ResourceAmount(OAK_LOG, 3)),
            List.of(new ResourceAmount(OAK_PLANKS, 4))
        ));
        final List<Ingredient> ingredients = sut.layout().ingredients();
        final List<ResourceAmount> outputs = sut.layout().outputs();
        final List<ResourceAmount> byproducts = sut.layout().byproducts();

        final Ingredient newIngredient = new Ingredient(2, List.of(B));
        final ResourceAmount newOutput = new ResourceAmount(OAK_PLANKS, 4);
        final ResourceAmount newByproduct = new ResourceAmount(OAK_LOG, 2);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> ingredients.add(newIngredient);
        final ThrowableAssert.ThrowingCallable action2 = () -> outputs.add(newOutput);
        final ThrowableAssert.ThrowingCallable action3 = () -> byproducts.add(newByproduct);

        // Assert
        assertThatThrownBy(action).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(action2).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(action3).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldNotCreateExternalPatternWithByproducts() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(UUID.randomUUID(), new PatternLayout(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            ),
            List.of(
                new ResourceAmount(COBBLESTONE, 1)
            ),
            PatternType.EXTERNAL
        ));

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("External patterns cannot have byproducts");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotCreatePatternWithNullType() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(UUID.randomUUID(), new PatternLayout(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            ),
            List.of(),
            null
        ));

        // Assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(PatternType.class)
    @SuppressWarnings("ConstantConditions")
    void shouldNotCreatePatternWithNullIngredients(final PatternType type) {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(UUID.randomUUID(), new PatternLayout(
            null,
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            ),
            List.of(),
            type
        ));

        // Assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(PatternType.class)
    @SuppressWarnings("ConstantConditions")
    void shouldNotCreatePatternWithNullOutputs(final PatternType type) {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(UUID.randomUUID(), new PatternLayout(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            null,
            List.of(),
            type
        ));

        // Assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotCreatePatternWithNullByproducts() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(UUID.randomUUID(), PatternLayout.internal(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            ),
            null
        ));

        // Assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotCreateWithNullId() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(null, PatternLayout.internal(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            ),
            List.of()
        ));

        // Assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }
}
