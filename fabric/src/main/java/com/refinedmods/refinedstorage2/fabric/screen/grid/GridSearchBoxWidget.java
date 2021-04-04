package com.refinedmods.refinedstorage2.fabric.screen.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.refinedmods.refinedstorage2.core.util.History;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SearchFieldWidget;
import net.minecraft.client.font.TextRenderer;

public class GridSearchBoxWidget extends SearchFieldWidget implements GridSearchBox {
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();

    public GridSearchBoxWidget(TextRenderer textRenderer, int x, int y, int width) {
        super(textRenderer, x, y, width, new History(SEARCH_FIELD_HISTORY));
    }

    @Override
    public void setAutoSelected(boolean autoSelected) {
        setFocused(autoSelected);
        setFocusUnlocked(!autoSelected);
    }

    @Override
    public void setListener(Consumer<String> listener) {
        setChangedListener(listener);
    }
}
