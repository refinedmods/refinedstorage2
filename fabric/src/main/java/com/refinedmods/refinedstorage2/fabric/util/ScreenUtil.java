package com.refinedmods.refinedstorage2.fabric.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

public class ScreenUtil {
    private static final List<String> VERSION_INFO_LINES = new ArrayList<>();

    private ScreenUtil() {
    }

    public static void drawVersionInformation(MatrixStack matrixStack, TextRenderer textRenderer) {
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

        FabricLoader
                .getInstance()
                .getModContainer("refinedstorage2")
                .flatMap(ScreenUtil::getVersion)
                .ifPresent(version -> VERSION_INFO_LINES.add("v" + version));
    }

    private static Optional<String> getVersion(ModContainer platform) {
        String friendlyString = platform.getMetadata().getVersion().getFriendlyString();
        if ("${version}".equals(friendlyString)) {
            return Optional.empty();
        }
        return Optional.of(friendlyString);
    }
}
