package com.refinedmods.refinedstorage2.api.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComponentMapFactoryTest {
    @Test
    void shouldRegisterFactoryAndBuildComponentMap() {
        // Arrange
        final ComponentMapFactory<TestComponent, String> sut = new ComponentMapFactory<>();
        sut.addFactory(TestComponent1.class, TestComponent1::new);
        sut.addFactory(TestComponent3.class, TestComponent3::new);

        // Act
        final ComponentMap<TestComponent> map = sut.buildComponentMap("TEST");

        // Assert
        assertThat(map.getComponent(TestComponent1.class).toString()).isEqualTo("C1 TEST");
        assertThat(map.getComponent(TestComponent3.class).toString()).isEqualTo("C3 TEST");
        assertThat(map.getComponents()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new TestComponent1("TEST"),
            new TestComponent3("TEST")
        );
    }

    @Test
    void shouldNotBeAbleToRetrieveUnregisteredComponent() {
        // Arrange
        final ComponentMapFactory<TestComponent, String> sut = new ComponentMapFactory<>();
        sut.addFactory(TestComponent1.class, TestComponent1::new);
        sut.addFactory(TestComponent3.class, TestComponent3::new);

        // Act
        final ComponentMap<TestComponent> map = sut.buildComponentMap("TEST");

        // Assert
        assertThrows(IllegalArgumentException.class, () -> map.getComponent(TestComponent2.class));
    }

    @Test
    void testCopying() {
        // Arrange
        final ComponentMapFactory<TestComponent, String> original = new ComponentMapFactory<>();
        original.addFactory(TestComponent1.class, TestComponent1::new);
        original.addFactory(TestComponent2.class, TestComponent2::new);

        // Act
        final ComponentMapFactory<TestComponent, String> copied = original.copy();
        copied.addFactory(TestComponent3.class, TestComponent3::new);

        // Assert
        assertThat(original.buildComponentMap("original").getComponents())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new TestComponent1("original"),
                new TestComponent2("original")
            );

        assertThat(copied.buildComponentMap("copied").getComponents())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new TestComponent1("copied"),
                new TestComponent2("copied"),
                new TestComponent3("copied")
            );
    }

    private interface TestComponent {
    }

    private abstract static class AbstractTestComponent implements TestComponent {
        private final String componentId;
        private final String givenContext;

        AbstractTestComponent(final String componentId, final String givenContext) {
            this.componentId = componentId;
            this.givenContext = givenContext;
        }

        @Override
        public String toString() {
            return componentId + " " + givenContext;
        }
    }

    private static class TestComponent1 extends AbstractTestComponent {
        TestComponent1(final String givenContext) {
            super("C1", givenContext);
        }
    }

    private static class TestComponent2 extends AbstractTestComponent {
        TestComponent2(final String givenContext) {
            super("C2", givenContext);
        }
    }

    private static class TestComponent3 extends AbstractTestComponent {
        TestComponent3(final String givenContext) {
            super("C3", givenContext);
        }
    }
}
