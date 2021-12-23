package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import java.util.function.Consumer;

public interface GridSearchBox {
    void setAutoSelected(boolean autoSelected);

    void setValue(String value);

    void setListener(Consumer<String> listener);

    void setInvalid(boolean invalid);
}
