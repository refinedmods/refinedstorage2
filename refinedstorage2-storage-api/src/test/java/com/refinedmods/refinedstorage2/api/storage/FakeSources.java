package com.refinedmods.refinedstorage2.api.storage;

public final class FakeSources {
    private FakeSources() {
    }

    public static final class FakeSource1 implements Source {
        public static final Source INSTANCE = new FakeSource1();

        @Override
        public String getName() {
            return "Source1";
        }
    }

    public static final class FakeSource2 implements Source {
        public static final Source INSTANCE = new FakeSource2();

        @Override
        public String getName() {
            return "Source2";
        }
    }
}
