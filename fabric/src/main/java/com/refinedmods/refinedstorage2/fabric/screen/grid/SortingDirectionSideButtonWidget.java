package com.refinedmods.refinedstorage2.fabric.screen.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.refinedmods.refinedstorage2.core.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.core.grid.GridView;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridChangeSettingPacket;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class SortingDirectionSideButtonWidget extends SideButtonWidget {
    private final GridView<ItemStack> itemView;
    private final TooltipRenderer tooltipRenderer;

    public SortingDirectionSideButtonWidget(GridView<ItemStack> itemView, TooltipRenderer tooltipRenderer) {
        super(createPressAction(itemView));
        this.itemView = itemView;
        this.tooltipRenderer = tooltipRenderer;
    }

    private static PressAction createPressAction(GridView<ItemStack> itemView) {
        return btn -> {
            GridSortingDirection sortingDirection = itemView.getSortingDirection().toggle();
            itemView.setSortingDirection(sortingDirection);
            itemView.sort();

            PacketUtil.sendToServer(GridChangeSettingPacket.ID, buf -> GridChangeSettingPacket.writeSortingDirection(buf, sortingDirection));
        };
    }

    @Override
    protected int getXTexture() {
        return itemView.getSortingDirection() == GridSortingDirection.ASCENDING ? 0 : 16;
    }

    @Override
    protected int getYTexture() {
        return 16;
    }

    @Override
    public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int mouseX, int mouseY) {
        List<Text> lines = new ArrayList<>();
        lines.add(new TranslatableText("gui.refinedstorage2.grid.sorting.direction"));
        lines.add(new TranslatableText("gui.refinedstorage2.grid.sorting.direction." + itemView.getSortingDirection().toString().toLowerCase(Locale.ROOT)).formatted(Formatting.GRAY));
        tooltipRenderer.render(matrixStack, lines, mouseX, mouseY);
    }
}
