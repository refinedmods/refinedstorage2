package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternOutputRenderingScreen;
import com.refinedmods.refinedstorage.common.autocrafting.PatternSlot;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.AbstractFilterScreen;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.widget.History;
import com.refinedmods.refinedstorage.common.support.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslationAsHeading;
import static java.util.Objects.requireNonNull;

public class AutocrafterScreen extends AbstractBaseScreen<AutocrafterContainerMenu>
    implements AutocrafterContainerMenu.Listener, PatternOutputRenderingScreen {
    private static final Component EMPTY_PATTERN_SLOT = createTranslationAsHeading(
        "gui", "autocrafter.empty_pattern_slot"
    );

    private static final Component CHAINED = createTranslation("gui", "autocrafter.chained");
    private static final Component CHAINED_HELP = createTranslation("gui", "autocrafter.chained.help");
    private static final Component CHAINED_HEAD_HELP = createTranslation("gui", "autocrafter.chained.head_help");
    private static final Component NOT_CHAINED = createTranslation("gui", "autocrafter.not_chained");
    private static final Component NOT_CHAINED_HELP = createTranslation("gui", "autocrafter.not_chained.help");
    private static final Component EDIT = createTranslation("gui", "autocrafter.edit_name");
    private static final Component CURRENTLY_LOCKED = createTranslation("gui", "autocrafter.currently_locked");

    private static final ResourceLocation NAME_BACKGROUND = createIdentifier("widget/autocrafter_name");
    private static final List<String> CRAFTER_NAME_HISTORY = new ArrayList<>();

    private final Inventory playerInventory;

    @Nullable
    private LockModeSideButtonWidget lockModeSideButtonWidget;

    @Nullable
    private EditBox nameField;
    @Nullable
    private Button editButton;
    private boolean editName;

    public AutocrafterScreen(final AutocrafterContainerMenu menu,
                             final Inventory playerInventory,
                             final Component title) {
        super(menu, playerInventory, new TextMarquee(title, getTitleMaxWidth(menu)));
        this.inventoryLabelY = 42;
        this.imageWidth = 210;
        this.imageHeight = 137;
        this.playerInventory = playerInventory;
    }

    private static int getTitleMaxWidth(final AutocrafterContainerMenu menu) {
        final int chainingTitleWidth = Minecraft.getInstance().font.width(getChainingTitle(menu));
        final int editButtonWidth = getEditButtonWidth();
        return TITLE_MAX_WIDTH - chainingTitleWidth - editButtonWidth - 10;
    }

    private int getEditButtonX() {
        return leftPos + titleLabelX + titleMarquee.getEffectiveWidth(font) + 2;
    }

    private static int getEditButtonWidth() {
        return Minecraft.getInstance().font.width(EDIT) + 8;
    }

    private static Component getChainingTitle(final AutocrafterContainerMenu menu) {
        return (menu.isPartOfChain() || menu.isHeadOfChain()) ? CHAINED : NOT_CHAINED;
    }

    private Component getChainingTooltip() {
        if (!getMenu().isPartOfChain() && !getMenu().isHeadOfChain()) {
            return NOT_CHAINED_HELP;
        }
        return getMenu().isHeadOfChain() ? CHAINED_HEAD_HELP : CHAINED_HELP;
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        super.renderBg(graphics, delta, mouseX, mouseY);
        if (editName) {
            graphics.blitSprite(NAME_BACKGROUND, leftPos + 7, topPos + 5, 162, 12);
        }
    }

    @Override
    protected void init() {
        super.init();
        getMenu().setListener(this);

        tryAddLockModeSideButton();
        addSideButton(new AutocrafterPrioritySideButtonWidget(
            getMenu().getProperty(AutocrafterPropertyTypes.PRIORITY),
            playerInventory,
            this
        ));
        addSideButton(new VisibleToTheAutocrafterManagerSideButtonWidget(
            getMenu().getProperty(AutocrafterPropertyTypes.VISIBLE_TO_THE_AUTOCRAFTER_MANAGER)
        ));

        nameField = new SearchFieldWidget(
            font,
            leftPos + 8 + 1,
            topPos + 6 + 1,
            159 - 6,
            new History(CRAFTER_NAME_HISTORY)
        );
        nameField.setValue(title.getString());
        nameField.setBordered(false);
        nameField.setCanLoseFocus(false);
        addWidget(nameField);

        editButton = addRenderableWidget(Button.builder(EDIT, button -> setEditName(true))
            .pos(getEditButtonX(), topPos + titleLabelY - 3)
            .size(getEditButtonWidth(), 14)
            .build());
        editButton.active = getMenu().canChangeName();

        setEditName(false);
    }

    private void tryAddLockModeSideButton() {
        if (getMenu().isPartOfChain()) {
            return;
        }
        lockModeSideButtonWidget = new LockModeSideButtonWidget(
            getMenu().getProperty(AutocrafterPropertyTypes.LOCK_MODE)
        );
        lockedChanged(getMenu().isLocked());
        addSideButton(lockModeSideButtonWidget);
    }

    private void setEditName(final boolean editName) {
        this.editName = editName;
        if (nameField != null) {
            nameField.visible = editName;
            nameField.setFocused(editName);
            nameField.setCanLoseFocus(!editName);
            if (editName) {
                setFocused(nameField);
            } else {
                setFocused(null);
            }
        }
        if (editButton != null) {
            editButton.visible = !editName;
        }
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (nameField != null && editName) {
            nameField.render(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        if (editName) {
            renderPlayerInventoryTitle(graphics);
            return;
        }
        super.renderLabels(graphics, mouseX, mouseY);
        final Component title = getChainingTitle(menu);
        graphics.drawString(font, title, getChainingTitleX(title), titleLabelY, 4210752, false);
    }

    private int getChainingTitleX(final Component title) {
        return imageWidth - 41 - font.width(title);
    }

    @Override
    public boolean charTyped(final char unknown1, final int unknown2) {
        return (nameField != null && editName && nameField.charTyped(unknown1, unknown2))
            || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (nameField != null && editName) {
            if (nameField.keyPressed(key, scanCode, modifiers)) {
                return true;
            }
            if (nameField.isFocused() && saveOrCancel(key)) {
                return true;
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    private boolean saveOrCancel(final int key) {
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) {
            getMenu().changeName(requireNonNull(nameField).getValue());
            setEditName(false);
            return true;
        } else if (key == GLFW.GLFW_KEY_ESCAPE) {
            setEditName(false);
            requireNonNull(nameField).setValue(titleMarquee.getText().getString());
            return true;
        }
        return false;
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (hoveredSlot instanceof PatternSlot patternSlot
            && !patternSlot.hasItem()
            && getMenu().getCarried().isEmpty()) {
            graphics.renderTooltip(font, EMPTY_PATTERN_SLOT, x, y);
            return;
        }
        final Component chainingTitle = getChainingTitle(getMenu());
        final int chainingTitleX = getChainingTitleX(chainingTitle);
        if (isHovering(chainingTitleX, titleLabelY, font.width(chainingTitle), font.lineHeight, x, y)) {
            final Component chainingTooltip = getChainingTooltip();
            Platform.INSTANCE.renderTooltip(
                graphics,
                List.of(HelpClientTooltipComponent.createAlwaysDisplayed(chainingTooltip)),
                x,
                y
            );
            return;
        }
        super.renderTooltip(graphics, x, y);
    }

    @Override
    protected ResourceLocation getTexture() {
        return AbstractFilterScreen.TEXTURE;
    }

    @Override
    public void nameChanged(final Component name) {
        titleMarquee.setText(name);
        if (nameField != null) {
            nameField.setValue(name.getString());
        }
        if (editButton != null) {
            editButton.setX(getEditButtonX());
        }
    }

    @Override
    public void lockedChanged(final boolean locked) {
        if (lockModeSideButtonWidget == null) {
            return;
        }
        if (locked) {
            lockModeSideButtonWidget.setWarning(CURRENTLY_LOCKED);
            return;
        }
        lockModeSideButtonWidget.setWarning(null);
    }

    @Override
    public boolean canDisplayOutput(final ItemStack stack) {
        return getMenu().containsPattern(stack);
    }
}
