package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewItem;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewType;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.amount.AbstractAmountScreen;
import com.refinedmods.refinedstorage.common.support.amount.AmountScreenConfiguration;
import com.refinedmods.refinedstorage.common.support.amount.DoubleAmountOperations;
import com.refinedmods.refinedstorage.common.support.amount.IconButton;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.CheckboxWidget;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class AutocraftingPreviewScreen extends AbstractAmountScreen<AutocraftingPreviewContainerMenu, Double>
    implements AutocraftingPreviewListener {
    static final int REQUEST_BUTTON_HEIGHT = 96 / 4;
    static final int REQUEST_BUTTON_WIDTH = 64;

    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/autocrafting_preview.png");
    private static final MutableComponent TITLE = createTranslation("gui", "autocrafting_preview.title");
    private static final MutableComponent START = createTranslation("gui", "autocrafting_preview.start");
    private static final MutableComponent PENDING = createTranslation("gui", "autocrafting_preview.pending");
    private static final MutableComponent CANCELLING = createTranslation("gui", "autocrafting_preview.cancelling");
    private static final MutableComponent CANCELLING_FORCE_CLOSE = createTranslation("gui",
        "autocrafting_preview.cancelling.force_close");
    private static final MutableComponent MAX = createTranslation("gui", "autocrafting_preview.max");
    private static final MutableComponent MAX_HELP = createTranslation("gui", "autocrafting_preview.max.help");
    private static final MutableComponent NOTIFY = createTranslation("gui", "autocrafting_preview.notify");
    private static final MutableComponent NOTIFY_HELP = createTranslation("gui", "autocrafting_preview.notify.help");
    private static final MutableComponent MISSING_RESOURCES = createTranslation(
        "gui",
        "autocrafting_preview.start.missing_resources"
    );
    private static final MutableComponent CYCLE_DETECTED = createTranslation(
        "gui",
        "autocrafting_preview.cycle_detected"
    ).withStyle(Style.EMPTY.withBold(true));
    private static final MutableComponent CYCLE_OUTPUTS = createTranslation(
        "gui",
        "autocrafting_preview.cycle_detected.outputs"
    );
    private static final MutableComponent BREAK_THE_CYCLE_AND_TRY_AGAIN = createTranslation(
        "gui",
        "autocrafting_preview.cycle_detected.break_the_cycle_and_try_again"
    );
    private static final MutableComponent REQUEST_TOO_LARGE_TO_HANDLE = createTranslation(
        "gui",
        "autocrafting_preview.request_too_large_to_handle"
    ).withStyle(Style.EMPTY.withBold(true));
    private static final MutableComponent TRY_SMALLER_AMOUNT = createTranslation(
        "gui",
        "autocrafting_preview.request_too_large_to_handle.try_smaller_amount"
    );
    private static final MutableComponent REQUEST_CANCELLED = createTranslation(
        "gui",
        "autocrafting_preview.request_cancelled"
    ).withStyle(Style.EMPTY.withBold(true));
    private static final MutableComponent CRAFT_SUBCOMPONENTS = createTranslation(
        "gui",
        "autocrafting_preview.request_cancelled.craft_subcomponents"
    );
    private static final MutableComponent NOT_AVAILABLE = createTranslation(
        "gui",
        "autocrafting_preview.not_available"
    );
    private static final ResourceLocation ROW = createIdentifier("autocrafting_preview/row");
    private static final ResourceLocation CRAFTING_REQUESTS = createIdentifier("autocrafting_preview/requests");

    private static final int ROWS_VISIBLE = 4;
    private static final int COLUMNS = 3;
    private static final int PREVIEW_AREA_HEIGHT = 119;

    private static final int ROW_HEIGHT = 30;
    private static final int ROW_WIDTH = 221;

    private static final int REQUESTS_WIDTH = 91;
    private static final int REQUESTS_HEIGHT = 111;
    private static final int REQUESTS_INNER_WIDTH = 64;
    private static final int REQUESTS_INNER_HEIGHT = 96;
    private static final int REQUESTS_VISIBLE = 4;

    @Nullable
    private ScrollbarWidget previewItemsScrollbar;
    @Nullable
    private ScrollbarWidget requestButtonsScrollbar;
    @Nullable
    private Button maxButton;
    @Nullable
    private CheckboxWidget notifyCheckbox;

    private final List<AutocraftingRequestButton> requestButtons = new ArrayList<>();
    private final boolean requestsButtonsVisible;

    private final RateLimiter requestRateLimiter = RateLimiter.create(1);
    private final RateLimiter maxAmountRequestRateLimiter = RateLimiter.create(1 / 5D);

    @Nullable
    private Double changedAmount;
    private boolean mayEnableMaxAmountRequestButtonAgain;
    private boolean requestedCancellation;

    public AutocraftingPreviewScreen(final Screen parent,
                                     final Inventory playerInventory,
                                     final List<AutocraftingRequest> requests) {
        this(new AutocraftingPreviewContainerMenu(requests), parent, playerInventory);
    }

    public AutocraftingPreviewScreen(final AutocraftingPreviewContainerMenu menu, final Inventory playerInventory) {
        this(menu, null, playerInventory);
    }

    public AutocraftingPreviewScreen(final AutocraftingPreviewContainerMenu menu,
                                     @Nullable final Screen parent,
                                     final Inventory playerInventory) {
        super(
            menu,
            parent,
            playerInventory,
            TITLE,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Double>create()
                .withInitialAmount(1D)
                .withIncrementsTop(1, 10, 64)
                .withIncrementsTopStartPosition(new Vector3f(80, 20, 0))
                .withIncrementsBottom(-1, -10, -64)
                .withIncrementsBottomStartPosition(new Vector3f(80, 71, 0))
                .withAmountFieldPosition(new Vector3f(77, 51, 0))
                .withActionButtonsStartPosition(new Vector3f(7, 222, 0))
                .withHorizontalActionButtons(true)
                .withMinAmount(menu::getMinAmount)
                .withResetAmount(1D)
                .withConfirmButtonText(START)
                .build(),
            DoubleAmountOperations.INSTANCE
        );
        this.imageWidth = 254;
        this.imageHeight = 249;
        this.requestsButtonsVisible = getMenu().getRequests().size() > 1;
        getMenu().setListener(this);
    }

    @Override
    protected void init() {
        final boolean wasAlreadyInitialized = amountField != null;
        super.init();
        previewItemsScrollbar = new ScrollbarWidget(
            leftPos + 235,
            topPos + 98,
            ScrollbarWidget.Type.NORMAL,
            PREVIEW_AREA_HEIGHT
        );
        previewItemsScrollbar.setEnabled(false);

        if (requestsButtonsVisible) {
            initRequestButtons();
        }

        if (confirmButton != null) {
            disableStartButton();
        }

        if (!wasAlreadyInitialized) {
            getMenu().loadCurrentRequest();
        }

        final boolean wasActive = maxButton == null || maxButton.active;
        maxButton = Button.builder(MAX, this::requestMaxAmount)
            .size(22, 15)
            .pos(leftPos + 185 - 1, topPos + 49 - 1)
            .build();
        maxButton.active = wasActive;
        addRenderableWidget(maxButton);

        final boolean selected = notifyCheckbox == null ? menu.isNotify() : notifyCheckbox.isSelected();
        notifyCheckbox = new CheckboxWidget(
            leftPos + imageWidth - font.width(NOTIFY) - 4 - 9 - 6,
            topPos + 222 + 6,
            NOTIFY,
            Minecraft.getInstance().font,
            selected,
            CheckboxWidget.Size.SMALL
        );
        notifyCheckbox.setHelpTooltip(NOTIFY_HELP);
        notifyCheckbox.setOnPressed((checkbox, notify) -> menu.setNotify(notify));
        addRenderableWidget(notifyCheckbox);

        getExclusionZones().add(new Rect2i(
            leftPos - REQUESTS_WIDTH + 4,
            topPos,
            REQUESTS_WIDTH,
            REQUESTS_HEIGHT
        ));
    }

    @Nullable
    @Override
    protected IconButton.Icon getConfirmButtonIcon() {
        return null;
    }

    private void requestMaxAmount(final Button button) {
        button.active = false;
        getMenu().requestMaxAmount();
    }

    private void initRequestButtons() {
        requestButtons.clear();
        requestButtonsScrollbar = new ScrollbarWidget(
            leftPos - 17 + 4,
            getRequestButtonsInnerY(),
            ScrollbarWidget.Type.NORMAL,
            96
        );
        requestButtonsScrollbar.setListener(value -> {
            final int scrollOffset = requestButtonsScrollbar.isSmoothScrolling()
                ? (int) requestButtonsScrollbar.getOffset()
                : (int) requestButtonsScrollbar.getOffset() * REQUEST_BUTTON_HEIGHT;
            for (int i = 0; i < requestButtons.size(); i++) {
                final AutocraftingRequestButton requestButton = requestButtons.get(i);
                final int y = getCraftingRequestButtonY(i) - scrollOffset;
                requestButton.setY(y);
                requestButton.visible = isCraftingRequestButtonVisible(y);
            }
        });
        updateRequestsScrollbar();
        for (int i = 0; i < getMenu().getRequests().size(); ++i) {
            final AutocraftingRequest request = getMenu().getRequests().get(i);
            final int buttonY = getCraftingRequestButtonY(i);
            final AutocraftingRequestButton button = new AutocraftingRequestButton(
                getRequestButtonsInnerX(),
                buttonY,
                request,
                this::changeCurrentRequest
            );
            button.visible = isCraftingRequestButtonVisible(buttonY);
            requestButtons.add(addWidget(button));
        }
    }

    private boolean isCraftingRequestButtonVisible(final int y) {
        final int innerY = getRequestButtonsInnerY();
        return y >= innerY - REQUEST_BUTTON_HEIGHT && y <= innerY + REQUESTS_INNER_HEIGHT;
    }

    private int getCraftingRequestButtonY(final int i) {
        return getRequestButtonsInnerY() + (i * REQUEST_BUTTON_HEIGHT);
    }

    private void changeCurrentRequest(final AutocraftingRequest request) {
        getMenu().setCurrentRequest(request);
    }

    private void setRequest(final AutocraftingRequest request) {
        for (final AutocraftingRequestButton requestButton : requestButtons) {
            requestButton.active = requestButton.getRequest() != request;
        }
        updateAmount(request.getAmount());
        setPreview(request.getPreview());
    }

    private void setPreview(@Nullable final Preview preview) {
        if (previewItemsScrollbar == null || confirmButton == null) {
            return;
        }
        if (preview == null) {
            previewItemsScrollbar.setEnabled(false);
            previewItemsScrollbar.setMaxOffset(0);
            disableStartButton();
            return;
        }
        final int items = preview.items().size();
        final int rows = Math.ceilDiv(items, COLUMNS) - ROWS_VISIBLE;
        previewItemsScrollbar.setMaxOffset(previewItemsScrollbar.isSmoothScrolling() ? rows * ROW_HEIGHT : rows);
        previewItemsScrollbar.setEnabled(rows > 0);
        confirmButton.setMessage(START);
        confirmButton.active = preview.type() == PreviewType.SUCCESS;
        confirmButton.setIcon(preview.type() == PreviewType.SUCCESS
            ? IconButton.Icon.START
            : IconButton.Icon.ERROR);
        confirmButton.setTooltip(preview.type() == PreviewType.MISSING_RESOURCES
            ? Tooltip.create(MISSING_RESOURCES)
            : null);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (previewItemsScrollbar != null) {
            previewItemsScrollbar.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (requestButtonsScrollbar != null) {
            requestButtonsScrollbar.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (requestsButtonsVisible) {
            final int requestsInnerX = getRequestButtonsInnerX();
            final int requestsInnerY = getRequestButtonsInnerY();
            graphics.enableScissor(
                requestsInnerX,
                requestsInnerY,
                requestsInnerX + REQUESTS_INNER_WIDTH,
                requestsInnerY + REQUESTS_INNER_HEIGHT
            );
            for (final AutocraftingRequestButton requestButton : requestButtons) {
                requestButton.render(graphics, mouseX, mouseY, partialTicks);
            }
            graphics.disableScissor();
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        super.renderBg(graphics, delta, mouseX, mouseY);
        if (requestsButtonsVisible) {
            graphics.blitSprite(CRAFTING_REQUESTS, leftPos - REQUESTS_WIDTH + 4, topPos, REQUESTS_WIDTH,
                REQUESTS_HEIGHT);
        }
        final AutocraftingRequest request = getMenu().getCurrentRequest();
        final Preview preview = request.getPreview();
        if (preview == null || previewItemsScrollbar == null) {
            return;
        }
        final int x = leftPos + 8;
        final int y = topPos + 98;
        graphics.enableScissor(x, y, x + 221, y + PREVIEW_AREA_HEIGHT);
        if (preview.type() == PreviewType.CYCLE_DETECTED) {
            renderCycleDetected(graphics, y, x, preview);
        } else if (preview.type() == PreviewType.OVERFLOW) {
            renderRequestTooLargeToHandle(graphics, x, y);
        } else if (preview.type() == PreviewType.CANCELLED) {
            renderCancelled(graphics, x, y);
        } else if (preview.type() == PreviewType.NOT_AVAILABLE) {
            renderNotAvailable(graphics, x, y);
        } else {
            renderPreviewRows(graphics, mouseX, mouseY, preview, y, x);
        }
        graphics.disableScissor();
    }

    private void renderCycleDetected(final GuiGraphics graphics, final int y, final int x, final Preview preview) {
        int yy = y + 4;
        SmallText.render(
            graphics,
            font,
            CYCLE_DETECTED.getVisualOrderText(),
            x + 4,
            yy,
            0xFF5555,
            false,
            SmallText.DEFAULT_SCALE
        );
        yy += 10;
        SmallText.render(
            graphics,
            font,
            CYCLE_OUTPUTS.getVisualOrderText(),
            x + 4,
            yy,
            0x404040,
            false,
            SmallText.DEFAULT_SCALE
        );
        yy += 10;
        for (final ResourceAmount output : preview.outputsOfPatternWithCycle()) {
            final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
                output.resource().getClass()
            );
            rendering.render(output.resource(), graphics, x + 4, yy);
            SmallText.render(
                graphics,
                font,
                Component.literal(output.amount() + "x ").append(rendering.getDisplayName(output.resource()))
                    .getVisualOrderText(),
                x + 4 + 16 + 3,
                yy + 5,
                0x404040,
                false,
                SmallText.DEFAULT_SCALE
            );
            yy += 18;
        }
        yy += 2;
        SmallText.render(
            graphics,
            font,
            BREAK_THE_CYCLE_AND_TRY_AGAIN.getVisualOrderText(),
            x + 4,
            yy,
            0x404040,
            false,
            SmallText.DEFAULT_SCALE
        );
    }

    private void renderRequestTooLargeToHandle(final GuiGraphics graphics, final int x, final int y) {
        SmallText.render(
            graphics,
            font,
            REQUEST_TOO_LARGE_TO_HANDLE.getVisualOrderText(),
            x + 4,
            y + 4,
            0xFF5555,
            false,
            SmallText.DEFAULT_SCALE
        );
        SmallText.render(
            graphics,
            font,
            TRY_SMALLER_AMOUNT.getVisualOrderText(),
            x + 4,
            y + 4 + 10,
            0x404040,
            false,
            SmallText.DEFAULT_SCALE
        );
    }

    private void renderCancelled(final GuiGraphics graphics, final int x, final int y) {
        SmallText.render(
            graphics,
            font,
            REQUEST_CANCELLED.getVisualOrderText(),
            x + 4,
            y + 4,
            0xFF5555,
            false,
            SmallText.DEFAULT_SCALE
        );
        SmallText.render(
            graphics,
            font,
            CRAFT_SUBCOMPONENTS.getVisualOrderText(),
            x + 4,
            y + 4 + 10,
            0x404040,
            false,
            SmallText.DEFAULT_SCALE
        );
    }

    private void renderNotAvailable(final GuiGraphics graphics, final int x, final int y) {
        SmallText.render(
            graphics,
            font,
            NOT_AVAILABLE.getVisualOrderText(),
            x + 4,
            y + 4,
            0xFF5555,
            false,
            SmallText.DEFAULT_SCALE
        );
    }

    private void renderPreviewRows(final GuiGraphics graphics,
                                   final int mouseX,
                                   final int mouseY,
                                   final Preview preview,
                                   final int y,
                                   final int x) {
        final List<PreviewItem> items = preview.items();
        final int rows = Math.ceilDiv(items.size(), COLUMNS);
        for (int i = 0; i < rows; ++i) {
            final int scrollOffset = getScrollOffset();
            final int yy = y + (i * ROW_HEIGHT) - scrollOffset;
            renderRow(graphics, x, yy, i, items, mouseX, mouseY);
        }
    }

    private int getScrollOffset() {
        if (previewItemsScrollbar == null) {
            return 0;
        }
        return (previewItemsScrollbar.isSmoothScrolling()
            ? (int) previewItemsScrollbar.getOffset()
            : (int) previewItemsScrollbar.getOffset() * ROW_HEIGHT);
    }

    private void renderRow(final GuiGraphics graphics,
                           final int x,
                           final int y,
                           final int i,
                           final List<PreviewItem> items,
                           final double mouseX,
                           final double mouseY) {
        if (y <= topPos + 98 - ROW_HEIGHT || y > topPos + 98 + PREVIEW_AREA_HEIGHT) {
            return;
        }
        graphics.blitSprite(ROW, x, y, ROW_WIDTH, ROW_HEIGHT);
        for (int column = i * COLUMNS; column < Math.min(i * COLUMNS + COLUMNS, items.size()); ++column) {
            final PreviewItem item = items.get(column);
            final int xx = x + (column % COLUMNS) * 74;
            renderCell(graphics, xx, y, item, mouseX, mouseY);
        }
    }

    private void renderCell(final GuiGraphics graphics,
                            final int x,
                            final int y,
                            final PreviewItem item,
                            final double mouseX,
                            final double mouseY) {
        if (item.missing() > 0) {
            graphics.fill(x, y, x + 73, y + 29, 0xFFF2DEDE);
        }
        int xx = x + 2;
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
            item.resource().getClass()
        );
        int yy = y + 7;
        rendering.render(item.resource(), graphics, xx, yy);
        if (isHovering(xx - leftPos, yy - topPos, 16, 16, mouseX, mouseY)
            && isHoveringOverPreviewArea(mouseX, mouseY)) {
            setTooltipForNextRenderPass(rendering.getTooltip(item.resource()).stream()
                .map(Component::getVisualOrderText)
                .toList());
        }
        if (!SmallText.isSmall()) {
            yy -= 2;
        }
        xx += 16 + 3;
        if (item.missing() > 0) {
            renderCellText(graphics, "missing", rendering, xx, yy, item.missing());
            yy += 7;
        }
        if (item.available() > 0) {
            renderCellText(graphics, "available", rendering, xx, yy, item.available());
            yy += 7;
        }
        if (item.toCraft() > 0) {
            renderCellText(graphics, "to_craft", rendering, xx, yy, item.toCraft());
        }
    }

    private void renderCellText(final GuiGraphics graphics,
                                final String type,
                                final ResourceRendering rendering,
                                final int x,
                                final int y,
                                final long amount) {
        SmallText.render(
            graphics,
            font,
            createTranslation("gui", "autocrafting_preview." + type, rendering.formatAmount(amount, true))
                .getVisualOrderText(),
            x,
            y,
            0x404040,
            false,
            SmallText.DEFAULT_SCALE
        );
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        super.renderTooltip(graphics, x, y);
        if (maxButton != null && maxButton.isHovered()) {
            Platform.INSTANCE.renderTooltip(
                graphics,
                List.of(HelpClientTooltipComponent.createAlwaysDisplayed(MAX_HELP)),
                x,
                y
            );
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (previewItemsScrollbar != null && previewItemsScrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        if (requestButtonsScrollbar != null && requestButtonsScrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public void mouseMoved(final double mx, final double my) {
        if (previewItemsScrollbar != null) {
            previewItemsScrollbar.mouseMoved(mx, my);
        }
        if (requestButtonsScrollbar != null) {
            requestButtonsScrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final double mx, final double my, final int button) {
        if (previewItemsScrollbar != null && previewItemsScrollbar.mouseReleased(mx, my, button)) {
            return true;
        }
        if (requestButtonsScrollbar != null && requestButtonsScrollbar.mouseReleased(mx, my, button)) {
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double z, final double delta) {
        final boolean didPreviewItemsScrollbar = previewItemsScrollbar != null
            && isHoveringOverPreviewArea(x, y)
            && previewItemsScrollbar.mouseScrolled(x, y, z, delta);
        final boolean didRequestButtonsScrollbar = !didPreviewItemsScrollbar
            && requestButtonsScrollbar != null
            && isHoveringOverRequestButtons(x, y)
            && requestButtonsScrollbar.mouseScrolled(x, y, z, delta);
        return didPreviewItemsScrollbar || didRequestButtonsScrollbar || super.mouseScrolled(x, y, z, delta);
    }

    private boolean isHoveringOverPreviewArea(final double x, final double y) {
        return isHovering(7, 97, 241, 121, x, y);
    }

    private boolean isHoveringOverRequestButtons(final double x, final double y) {
        final int requestsInnerX = getRequestButtonsInnerX() - 1;
        final int requestsInnerY = getRequestButtonsInnerY() - 1;
        return isHovering(requestsInnerX - leftPos, requestsInnerY - topPos, 80, 98, x, y);
    }

    private int getRequestButtonsInnerY() {
        return topPos + 8;
    }

    private int getRequestButtonsInnerX() {
        return leftPos - 83 + 4;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected void onAmountFieldChanged() {
        if (amountField == null || confirmButton == null) {
            return;
        }
        disableStartButton();
        getAndValidateAmount().ifPresentOrElse(amount -> {
            confirmButton.setMessage(PENDING);
            changedAmount = amount;
            amountField.setTextColor(0xFFFFFF);
        }, () -> {
            confirmButton.setMessage(START);
            amountField.setTextColor(0xFF5555);
        });
    }

    private void disableStartButton() {
        if (confirmButton == null) {
            return;
        }
        confirmButton.active = false;
        confirmButton.setIcon(null);
        confirmButton.setTooltip(null);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (changedAmount != null && requestRateLimiter.tryAcquire()) {
            getMenu().amountChanged(changedAmount);
            changedAmount = null;
        }
        if (mayEnableMaxAmountRequestButtonAgain && maxButton != null && maxAmountRequestRateLimiter.tryAcquire()) {
            maxButton.active = true;
            mayEnableMaxAmountRequestButtonAgain = false;
        }
    }

    @Override
    protected boolean beforeClose() {
        if (requestedCancellation) {
            return true;
        }
        requestedCancellation = true;
        if (cancelButton != null) {
            cancelButton.active = false;
            cancelButton.setMessage(CANCELLING);
            cancelButton.setTooltip(Tooltip.create(CANCELLING_FORCE_CLOSE));
        }
        getMenu().sendCancelRequest();
        return false;
    }

    public void cancelResponseReceived() {
        // If we get the cancellation response late, and have force closed the screen,
        // and meanwhile have a new screen open already, we do not want to close right now.
        if (!requestedCancellation) {
            return;
        }
        close();
    }

    @Override
    protected void reset() {
        updateAmount(getMenu().getCurrentRequest().getAmount());
    }

    @Override
    protected boolean confirm(final Double amount) {
        disableStartButton();
        if (confirmButton != null) {
            confirmButton.setMessage(PENDING);
        }
        getMenu().sendRequest(amount, notifyCheckbox == null ? menu.isNotify() : notifyCheckbox.isSelected());
        return false;
    }

    @Override
    public void requestChanged(final AutocraftingRequest request) {
        setRequest(request);
    }

    @Override
    public void previewChanged(@Nullable final Preview preview) {
        setPreview(preview);
    }

    @Override
    public void requestRemoved(final AutocraftingRequest request, final boolean last) {
        if (last) {
            close();
            return;
        }
        requestButtons.stream().filter(btn -> btn.getRequest() == request).findFirst().ifPresent(btn -> {
            removeWidget(btn);
            requestButtons.remove(btn);
        });
        updateRequestsScrollbar();
        for (int i = 0; i < requestButtons.size(); ++i) {
            final AutocraftingRequestButton requestButton = requestButtons.get(i);
            final int buttonY = getCraftingRequestButtonY(i);
            requestButton.setY(buttonY);
            requestButton.visible = isCraftingRequestButtonVisible(buttonY);
        }
    }

    @Override
    public void maxAmountReceived(final double maxAmount) {
        updateAmount(maxAmount);
        maxAmountRequestRateLimiter.tryAcquire();
        mayEnableMaxAmountRequestButtonAgain = true;
    }

    private void updateRequestsScrollbar() {
        if (requestButtonsScrollbar == null) {
            return;
        }
        final int totalRequestButtons = getMenu().getRequests().size() - REQUESTS_VISIBLE;
        final int maxOffset = requestButtonsScrollbar.isSmoothScrolling()
            ? totalRequestButtons * REQUEST_BUTTON_HEIGHT
            : totalRequestButtons;
        requestButtonsScrollbar.setEnabled(maxOffset > 0);
        requestButtonsScrollbar.setMaxOffset(maxOffset);
    }
}
