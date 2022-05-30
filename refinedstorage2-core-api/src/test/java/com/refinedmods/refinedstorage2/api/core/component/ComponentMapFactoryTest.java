package com.refinedmods.refinedstorage2.api.core.component;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class ComponentMapFactoryTest {
    @Test
    void Test_should_register_factory_and_build_map_correctly() {
        // Arrange
        ComponentMapFactory<TestComponent, String> sut = new ComponentMapFactory<>();

        sut.addFactory(TestComponent1.class, TestComponent1::new);
        sut.addFactory(TestComponent3.class, TestComponent3::new);

        // Act
        ComponentMap<TestComponent> map = sut.buildComponentMap("TEST");

        // Assert
        assertThat(map.getComponent(TestComponent1.class).getGivenContext()).isEqualTo("C1 TEST");
        assertThat(map.getComponent(TestComponent2.class)).isNull();
        assertThat(map.getComponent(TestComponent3.class).getGivenContext()).isEqualTo("C3 TEST");
        assertThat(map.getComponents()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new TestComponent1("TEST"),
                new TestComponent3("TEST")
        );
    }

    @Test
    void Test_should_copy_correctly() {
        // Arrange
        ComponentMapFactory<TestComponent, String> original = new ComponentMapFactory<>();
        original.addFactory(TestComponent1.class, TestComponent1::new);
        original.addFactory(TestComponent2.class, TestComponent2::new);

        // Act
        ComponentMapFactory<TestComponent, String> copied = original.copy();
        copied.addFactory(TestComponent3.class, TestComponent3::new);

        // Assert
        assertThat(original.buildComponentMap("original").getComponents()).hasSize(2);
        assertThat(copied.buildComponentMap("copied").getComponents()).hasSize(3);
    }

    private interface TestComponent {
        String getGivenContext();
    }

    private abstract static class TestComponentImpl implements TestComponent {
        private final String componentId;
        private final String givenContext;

        public TestComponentImpl(String componentId, String givenContext) {
            this.componentId = componentId;
            this.givenContext = givenContext;
        }

        @Override
        public String getGivenContext() {
            return componentId + " " + givenContext;
        }
    }

    private static class TestComponent1 extends TestComponentImpl {
        public TestComponent1(String givenContext) {
            super("C1", givenContext);
        }
    }

    private static class TestComponent2 extends TestComponentImpl {
        public TestComponent2(String givenContext) {
            super("C2", givenContext);
        }
    }

    private static class TestComponent3 extends TestComponentImpl {
        public TestComponent3(String givenContext) {
            super("C3", givenContext);
        }
    }
}
