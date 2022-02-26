package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import java.util.function.Consumer;

public interface GridSearchBox {
    void setAutoSelected(boolean autoSelected);

    void setValue(String value);

    void setListener(Consumer<String> listener);
}
