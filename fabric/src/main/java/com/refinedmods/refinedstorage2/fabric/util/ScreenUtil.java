package com.refinedmods.refinedstorage2.fabric.util;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

public class ScreenUtil {
    private static final List<String> VERSION_INFO_LINES = new ArrayList<>();

    private ScreenUtil() {
    }

    public static void drawVersionInformation(MatrixStack matrixStack, TextRenderer textRenderer, float delta) {
        if (VERSION_INFO_LINES.isEmpty()) {
            loadVersionInformationLines();
        }

        int x = 5;
        int y = 5;

        for (String line : VERSION_INFO_LINES) {
            textRenderer.drawWithShadow(matrixStack, line, x, y, Formatting.WHITE.getColorValue());
            y += 9;
        }
    }

    private static void loadVersionInformationLines() {
        VERSION_INFO_LINES.add("Refined Storage for Fabric");

        FabricLoader.getInstance().getModContainer("refinedstorage2").ifPresent(platform -> VERSION_INFO_LINES.add("Platform " + getVersion(platform)));
        FabricLoader.getInstance().getModContainer("refinedstorage2-core").ifPresent(core -> VERSION_INFO_LINES.add("Core " + getVersion(core)));
    }

    private static String getVersion(ModContainer platform) {
        String friendlyString = platform.getMetadata().getVersion().getFriendlyString();
        if ("${version}".equals(friendlyString)) {
            friendlyString = "unknown";
        }
        return friendlyString;
    }
}
