package com.refinedmods.refinedstorage2.fabric.screen.grid;

import java.util.function.Consumer;

public interface GridSearchBox {
    void setAutoSelected(boolean autoSelected);

    void setText(String text);

    void setListener(Consumer<String> listener);

    void setError(boolean error);
}
