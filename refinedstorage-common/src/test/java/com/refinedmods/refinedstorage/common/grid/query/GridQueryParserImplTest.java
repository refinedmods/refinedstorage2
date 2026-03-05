package com.refinedmods.refinedstorage.common.grid.query;

import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage.common.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResourceAttributeKey;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage.query.parser.ParserOperatorMappings;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GridQueryParserImplTest {
    private final GridQueryParser sut = new GridQueryParser(
        LexerTokenMappings.DEFAULT_MAPPINGS,
        ParserOperatorMappings.DEFAULT_MAPPINGS
    );

    private final ResourceRepository<GridResource> repository = new ResourceRepositoryImpl<>(
        resource -> {
            throw new UnsupportedOperationException();
        },
        MutableResourceListImpl.create(),
        new HashSet<>(),
        v -> Comparator.comparing(GridResource::getHoverName),
        v -> Comparator.comparingLong(resource -> resource.getAmount(v))
    );

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void testEmptyQuery(final String query) throws GridQueryParserException {
        // Act
        final var predicate = sut.parse(query);

        // Assert
        assertThat(predicate.test(repository, new R("Dirt"))).isTrue();
        assertThat(predicate.test(repository, new R("Glass"))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"dirt", "Dirt", "DiRt", "Di", "irt", "test", "TeSt"})
    void testNameQuery(final String query) throws GridQueryParserException {
        // Act
        final var predicate = sut.parse(query);

        // Assert
        assertThat(predicate.test(repository, new R(List.of("Dirt", "test")))).isTrue();
        assertThat(predicate.test(repository, new R("Glass"))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"@refined", "@\"Refined Storage\"", "@ReFiNe", "@Storage", "@rs", "@RS"})
    void testModQuery(final String query) throws GridQueryParserException {
        // Act
        final var predicate = sut.parse(query);

        // Assert
        assertThat(predicate.test(repository, new R("Sponge", 1, "rs", "Refined Storage", Set.of()))).isTrue();
        assertThat(predicate.test(repository, new R("Glass"))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"#underwater", "#UnDerWate", "#water", "#unrelated", "#UNREL", "#laTed"})
    void testTagQuery(final String query) throws GridQueryParserException {
        // Act
        final var predicate = sut.parse(query);

        // Assert
        assertThat(predicate.test(repository,
            new R("Sponge", 1, "mc", "Minecraft", Set.of("underwater", "unrelated")))).isTrue();
        assertThat(predicate.test(repository, new R("Dirt", 1, "mc", "Minecraft", Set.of("transparent")))).isFalse();
    }

    @Test
    void testAttributeQueryWithInvalidNode() {
        // Act
        final Executable action = () -> sut.parse("@!true");

        // Assert
        final GridQueryParserException e = assertThrows(GridQueryParserException.class, action);
        assertThat(e.getMessage()).isEqualTo("Expected a literal");
    }

    @Test
    void testImplicitAndQuery() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse("DirT di RT");

        // Assert
        assertThat(predicate.test(repository, new R("Dirt"))).isTrue();
        assertThat(predicate.test(repository, new R("Glass"))).isFalse();
    }

    @Test
    void testImplicitAndQueryInParenthesis() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse("(DirT di RT) || (sto stone)");

        // Assert
        assertThat(predicate.test(repository, new R("Dirt"))).isTrue();
        assertThat(predicate.test(repository, new R("Glass"))).isFalse();
        assertThat(predicate.test(repository, new R("Stone"))).isTrue();
    }

    @Test
    void testImplicitAndQueryWithUnaryOperator() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse("@minecraft >5");

        // Assert
        assertThat(predicate.test(repository, new R("Dirt", 6, "minecraft", "Minecraft", Set.of()))).isTrue();
        assertThat(predicate.test(repository, new R("Glass", 5, "minecraft", "Minecraft", Set.of()))).isFalse();
        assertThat(predicate.test(repository, new R("Sponge", 5, "rs", "Refined Storage", Set.of()))).isFalse();
        assertThat(predicate.test(repository, new R("Cobblestone", 6, "rs", "Refined Storage", Set.of()))).isFalse();
    }

    @Test
    void testAndQuery() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse("DirT && di && RT");

        // Assert
        assertThat(predicate.test(repository, new R("Dirt"))).isTrue();
        assertThat(predicate.test(repository, new R("Glass"))).isFalse();
    }

    @Test
    void testOrQuery() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse("dir || glass || StoNe");

        // Assert
        assertThat(predicate.test(repository, new R("Dirt"))).isTrue();
        assertThat(predicate.test(repository, new R("Glass"))).isTrue();
        assertThat(predicate.test(repository, new R("Stone"))).isTrue();
        assertThat(predicate.test(repository, new R("Cobblestone"))).isTrue();

        assertThat(predicate.test(repository, new R("Sponge"))).isFalse();
        assertThat(predicate.test(repository, new R("Furnace"))).isFalse();
    }

    @Test
    void testSimpleNotQuery() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse("!stone");

        // Assert
        assertThat(predicate.test(repository, new R("Dirt"))).isTrue();
        assertThat(predicate.test(repository, new R("Glass"))).isTrue();

        assertThat(predicate.test(repository, new R("Stone"))).isFalse();
        assertThat(predicate.test(repository, new R("Cobblestone"))).isFalse();
    }

    @Test
    void testNotQueryWithMultipleOrParts() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse("!(stone || dirt)");

        // Assert
        assertThat(predicate.test(repository, new R("Sponge"))).isTrue();
        assertThat(predicate.test(repository, new R("Glass"))).isTrue();

        assertThat(predicate.test(repository, new R("Stone"))).isFalse();
        assertThat(predicate.test(repository, new R("Dirt"))).isFalse();
    }

    @Test
    void testComplexModQuery() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse(
            "((spo || buck) && @refined) || (glass && @mine)"
        );

        // Assert
        assertThat(predicate.test(repository, new R("Sponge", 1, "rs", "Refined Storage", Set.of()))).isTrue();
        assertThat(predicate.test(repository, new R("Bucket", 1, "rs", "Refined Storage", Set.of()))).isTrue();
        assertThat(predicate.test(repository, new R("Saddle", 1, "rs", "Refined Storage", Set.of()))).isFalse();

        assertThat(predicate.test(repository, new R("Glass", 1, "mc", "Minecraft", Set.of()))).isTrue();
        assertThat(predicate.test(repository, new R("Furnace", 1, "mc", "Minecraft", Set.of()))).isFalse();
    }

    @Test
    void testLessThanUnaryCountQuery() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse("<5");

        // Assert
        assertThat(predicate.test(repository, new R("Glass", 5))).isFalse();
        assertThat(predicate.test(repository, new R("Glass", 4))).isTrue();
    }

    @Test
    void testLessThanEqualsUnaryCountQuery() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse("<=5");

        // Assert
        assertThat(predicate.test(repository, new R("Glass", 6))).isFalse();
        assertThat(predicate.test(repository, new R("Glass", 5))).isTrue();
        assertThat(predicate.test(repository, new R("Glass", 4))).isTrue();
    }

    @Test
    void testGreaterThanUnaryCountQuery() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse(">5");

        // Assert
        assertThat(predicate.test(repository, new R("Glass", 5))).isFalse();
        assertThat(predicate.test(repository, new R("Glass", 6))).isTrue();
    }

    @Test
    void testGreaterThanEqualsUnaryCountQuery() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse(">=5");

        // Assert
        assertThat(predicate.test(repository, new R("Glass", 4))).isFalse();
        assertThat(predicate.test(repository, new R("Glass", 5))).isTrue();
        assertThat(predicate.test(repository, new R("Glass", 6))).isTrue();
    }

    @Test
    void testEqualsUnaryCountQuery() throws GridQueryParserException {
        // Act
        final var predicate = sut.parse("=5");

        // Assert
        assertThat(predicate.test(repository, new R("Glass", 4))).isFalse();
        assertThat(predicate.test(repository, new R("Glass", 5))).isTrue();
        assertThat(predicate.test(repository, new R("Glass", 6))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {">", ">=", "<", "<=", "="})
    void testInvalidNodeInUnaryCountQuery(final String operator) {
        // Act
        final GridQueryParserException e =
            assertThrows(GridQueryParserException.class, () -> sut.parse(operator + "(1 && 1)"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Count filtering expects a literal");
    }

    @ParameterizedTest
    @ValueSource(strings = {">", ">=", "<", "<=", "="})
    void testInvalidTokenInUnaryCountQuery(final String operator) {
        // Act
        final GridQueryParserException e =
            assertThrows(GridQueryParserException.class, () -> sut.parse(operator + "hello"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Count filtering expects an integer number");
    }

    private static class R implements GridResource {
        private final List<String> names;
        private final long amount;
        private final Map<GridResourceAttributeKey, Set<String>> attributes;

        R(final List<String> names) {
            this(names, 1);
        }

        R(final List<String> names, final long amount) {
            this.names = names;
            this.amount = amount;
            this.attributes = Map.of();
        }

        R(
            final List<String> names,
            final long amount,
            final String modId,
            final String modName,
            final Set<String> tags
        ) {
            this.names = names;
            this.amount = amount;
            this.attributes = Map.of(
                GridResourceAttributeKeys.MOD_ID, Set.of(modId),
                GridResourceAttributeKeys.MOD_NAME, Set.of(modName),
                GridResourceAttributeKeys.TAGS, tags
            );
        }

        R(final String name) {
            this(List.of(name), 1);
        }

        R(final String name, final long amount) {
            this(List.of(name), amount);
        }

        R(
            final String name,
            final long amount,
            final String modId,
            final String modName,
            final Set<String> tags
        ) {
            this(List.of(name), amount, modId, modName, tags);
        }

        @Override
        @Nullable
        public TrackedResource getTrackedResource(
            final Function<ResourceKey, @Nullable TrackedResource> trackedResourceProvider
        ) {
            return null;
        }

        @Override
        public long getAmount(final ResourceRepository<GridResource> repository) {
            return amount;
        }

        @Override
        public List<String> getSearchableNames() {
            return names;
        }

        @Override
        public String getHoverName() {
            return getSearchableNames().getLast();
        }

        @Override
        public Set<String> getAttribute(final GridResourceAttributeKey key) {
            return attributes.getOrDefault(key, Set.of());
        }

        @Override
        public boolean isAutocraftable(final ResourceRepository<GridResource> repository) {
            return false;
        }

        @Override
        public boolean canExtract(final ItemStack carriedStack, final ResourceRepository<GridResource> repository) {
            return false;
        }

        @Override
        public void onExtract(final GridExtractMode extractMode,
                              final boolean cursor,
                              final GridExtractionStrategy extractionStrategy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onScroll(final GridScrollMode scrollMode, final GridScrollingStrategy scrollingStrategy) {

            throw new UnsupportedOperationException();
        }

        @Override
        public void render(final GuiGraphicsExtractor graphics, final int x, final int y) {

            throw new UnsupportedOperationException();
        }

        @Override
        public String getDisplayedAmount(final ResourceRepository<GridResource> repository) {
            return "";
        }

        @Override
        public String getAmountInTooltip(final ResourceRepository<GridResource> repository) {
            return "";
        }

        @Override
        public boolean belongsToResourceType(final ResourceType resourceType) {
            return false;
        }

        @Override
        public List<Component> getTooltip() {
            return List.of();
        }

        @Override
        public Optional<TooltipComponent> getTooltipImage() {
            return Optional.empty();
        }

        @Override
        public int getRegistryId() {
            return 0;
        }

        @Override
        public List<ClientTooltipComponent> getExtractionHints(final ItemStack carriedStack,
                                                               final ResourceRepository<GridResource> repository) {
            return List.of();
        }

        @Override
        @Nullable
        public ResourceAmount getAutocraftingRequest() {
            throw new UnsupportedOperationException();
        }

        @Override
        @Nullable
        public PlatformResourceKey getResourceForRecipeMods() {
            throw new UnsupportedOperationException();
        }
    }
}
