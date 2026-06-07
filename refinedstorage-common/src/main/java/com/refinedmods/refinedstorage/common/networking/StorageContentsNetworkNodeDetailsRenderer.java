package com.refinedmods.refinedstorage.common.networking;

public class StorageContentsNetworkNodeDetailsRenderer /*implements NetworkNodeDetailsRenderer*/ {
    /*
    private static final int COLUMNS = 9;

    @Override
    public List<ClientTooltipComponent> render(final NetworkMonitorDetails details, final GuiGraphicsExtractor graphics,
                                               final int x, final int y, final int mouseX, final int mouseY) {
        if (!(details instanceof StorageContentsNetworkMonitorDetails storageContentsNetworkMonitorDetails)) {
            return Collections.emptyList();
        }
        final ResourceRepository<GridResource> repository = storageContentsNetworkMonitorDetails
            .getResourceRepository();
        final List<GridResource> gridResources = repository.getViewList();
        GridResource hovering = null;
        for (int i = 0; i < gridResources.size(); i++) {
            final GridResource resource = gridResources.get(i);
            final int slotX = x + (i % COLUMNS) * 18;
            final int slotY = y + (i / COLUMNS) * 18;
            graphics.blitSprite(GUI_TEXTURED, SLOT, slotX, slotY, 18, 18);
            final boolean interact = mouseX >= slotX
                && mouseY >= slotY
                && mouseX <= slotX + 16
                && mouseY <= slotY + 16;
            renderSlotContents(graphics, slotX + 1, slotY + 1, resource, repository, interact);
            if (interact) {
                hovering = resource;
            }
        }
        return hovering != null ? getTooltip(hovering, mouseX, mouseY, repository, graphics) : Collections.emptyList();
    }

    private static void renderSlotContents(final GuiGraphicsExtractor graphics, final int x, final int y,
                                           final GridResource resource,
                                           final ResourceRepository<GridResource> repository, final boolean interact) {
        if (interact) {
            ClientPlatformUtil.renderSlotHighlightBack(graphics, x, y);
        }
        resource.render(graphics, x, y);
        final boolean large = Minecraft.getInstance().isEnforceUnicode()
            || Platform.INSTANCE.getConfig().getGrid().isLargeFont();
        final String text = resource.getDisplayedAmount(repository);
        ResourceSlotRendering.renderAmount(graphics, x, y, text, 0xFFFFFFFF, large);
        if (interact) {
            ClientPlatformUtil.renderSlotHighlightFront(graphics, x, y);
        }
    }

    private static List<ClientTooltipComponent> getTooltip(final GridResource resource,
                                                           final int mouseX, final int mouseY,
                                                           final ResourceRepository<GridResource> repository,
                                                           final GuiGraphicsExtractor graphics) {
        final ItemStack stackContext = resource instanceof ItemGridResource itemResource
            ? itemResource.getItemStack()
            : ItemStack.EMPTY;
        final List<Component> lines = resource.getTooltip();
        final List<ClientTooltipComponent> processedLines = Platform.INSTANCE.processTooltipComponents(
            stackContext,
            graphics,
            mouseX,
            resource.getTooltipImage(),
            lines
        );
        final String amountInTooltip = resource.getAmountInTooltip(repository);
        processedLines.add(new SmallTextClientTooltipComponent(
            createTranslation("misc", "total", amountInTooltip).withStyle(ChatFormatting.GRAY)
        ));
        return processedLines;
    }

    @Override
    public int getRows(final NetworkMonitorDetails details) {
        if (details instanceof StorageContentsNetworkMonitorDetails storageContentsNetworkMonitorDetails) {
            final int size = storageContentsNetworkMonitorDetails.getResourceRepository().getViewList().size();
            return (int) Math.ceil((float) size / (float) COLUMNS);
        }
        return 0;
    }*/
}
