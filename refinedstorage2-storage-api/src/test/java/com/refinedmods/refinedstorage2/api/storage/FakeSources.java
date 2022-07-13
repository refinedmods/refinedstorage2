package com.refinedmods.refinedstorage2.api.storage;

public final class FakeSources {
    private FakeSources() {
    }

    public static final class FakeActor1 implements Actor {
        public static final Actor INSTANCE = new FakeActor1();

        @Override
        public String getName() {
            return "Source1";
        }
    }

    public static final class FakeActor2 implements Actor {
        public static final Actor INSTANCE = new FakeActor2();

        @Override
        public String getName() {
            return "Source2";
        }
    }
}
