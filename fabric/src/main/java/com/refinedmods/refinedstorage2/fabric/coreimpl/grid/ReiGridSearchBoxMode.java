package com.refinedmods.refinedstorage2.fabric.coreimpl.grid;

import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeDisplayProperties;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.core.grid.GridView;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

// TODO - Investigate hard dep on REI
public class ReiGridSearchBoxMode extends GridSearchBoxModeImpl {
    private final boolean twoWay;

    private ReiGridSearchBoxMode(GridQueryParser queryParser, boolean autoSelect, boolean twoWay, GridSearchBoxModeDisplayProperties displayProperties) {
        super(queryParser, autoSelect, displayProperties);
        this.twoWay = twoWay;
    }

    @Override
    public void onTextChanged(GridView<?> view, String text) {
        super.onTextChanged(view, text);
        TextFieldWidget textField = REIHelper.getInstance().getSearchTextField();
        if (textField != null) {
            textField.setText(text);
        }
    }

    @Override
    public String getSearchBoxValue() {
        if (twoWay) {
            TextFieldWidget textField = REIHelper.getInstance().getSearchTextField();
            if (textField != null) {
                return textField.getText();
            }
        }
        return null;
    }

    public static ReiGridSearchBoxMode create(GridQueryParser queryParser, boolean autoSelected, boolean twoWay) {
        return new ReiGridSearchBoxMode(queryParser, autoSelected, twoWay, new GridSearchBoxModeDisplayProperties(
            RefinedStorage2Mod.createIdentifier("textures/icons.png"),
            autoSelected ? 16 : 0,
            96,
            createText(autoSelected, twoWay)
        ));
    }

    private static Text createText(boolean autoSelected, boolean twoWay) {
        String twoWayText = twoWay ? "_two_way" : "";
        String autoSelectedText = autoSelected ? "_autoselected" : "";
        return new TranslatableText("gui.refinedstorage2.grid.search_box_mode.rei" + twoWayText + autoSelectedText).formatted(Formatting.GRAY);
    }
}
