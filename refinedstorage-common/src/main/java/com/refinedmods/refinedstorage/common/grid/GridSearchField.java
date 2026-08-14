package com.refinedmods.refinedstorage.common.grid;

import java.util.function.Consumer;

public interface GridSearchField {
    void setValue(String value);

    void setValid(boolean valid);

    void addListener(Consumer<String> listener);
}
