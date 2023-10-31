package com.refinedmods.refinedstorage2.platform.common.grid.screen;

import java.util.function.Consumer;

public interface GridSearchBox {
    void setAutoSelected(boolean autoSelected);

    void setValue(String value);

    void setValid(boolean valid);

    void addListener(Consumer<String> listener);
}
