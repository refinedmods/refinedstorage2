package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_HEIGHT;
import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_PADDING;
import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_WIDTH;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

class SmithingTablePatternGridRenderer implements PatternGridRenderer {
    private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM =
        Identifier.withDefaultNamespace("container/slot/smithing_template_armor_trim");
    private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE =
        Identifier.withDefaultNamespace("container/slot/smithing_template_netherite_upgrade");
    private static final List<Identifier> EMPTY_SLOT_SMITHING_TEMPLATES =
        List.of(EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM, EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE);
    private static final Component MISSING_SMITHING_TEMPLATE_TOOLTIP =
        Component.translatable("container.upgrade.missing_template_tooltip");
    private static final Vector3f ARMOR_STAND_TRANSLATION = new Vector3f();
    private static final Quaternionf ARMOR_STAND_ANGLE =
        (new Quaternionf()).rotationXYZ(0.43633232F, 0.0F, 3.1415927F);
    private static final Identifier SPRITE = createIdentifier("pattern_grid/smithing_table");

    private final PatternGridContainerMenu menu;
    private final int leftPos;
    private final int topPos;
    private final int x;
    private final int y;
    private final CyclingSlotBackground templateIcon;
    private final CyclingSlotBackground baseIcon;
    private final CyclingSlotBackground additionalIcon;
    private final ArmorStandRenderState armorStandPreview = new ArmorStandRenderState();

    private ItemStack result = ItemStack.EMPTY;

    SmithingTablePatternGridRenderer(final PatternGridContainerMenu menu,
                                     final int leftPos,
                                     final int topPos,
                                     final int x,
                                     final int y) {
        this.menu = menu;
        this.leftPos = leftPos;
        this.topPos = topPos;
        this.x = x;
        this.y = y;
        this.templateIcon = new CyclingSlotBackground(menu.getFirstSmithingTableSlotIndex());
        this.baseIcon = new CyclingSlotBackground(menu.getFirstSmithingTableSlotIndex() + 1);
        this.additionalIcon = new CyclingSlotBackground(menu.getFirstSmithingTableSlotIndex() + 2);
    }

    @Override
    public void addWidgets(final Consumer<AbstractWidget> widgets,
                           final Consumer<AbstractWidget> renderables) {
        final Level level = ClientPlatformUtil.getClientLevel();
        if (level == null) {
            return;
        }
        this.armorStandPreview.entityType = EntityType.ARMOR_STAND;
        this.armorStandPreview.showBasePlate = false;
        this.armorStandPreview.showArms = true;
        this.armorStandPreview.xRot = 25.0F;
        this.armorStandPreview.bodyRot = 210.0F;
        result = menu.getSmithingTableResult().copy();
        updatePreview();
    }

    @Override
    public void tick() {
        final ItemStack currentResult = menu.getSmithingTableResult();
        if (!ItemStack.isSameItemSameComponents(currentResult, result)) {
            result = currentResult.copy();
            updatePreview();
        }
        final Optional<SmithingTemplateItem> templateItem = menu.getSmithingTableTemplateItem();
        templateIcon.tick(EMPTY_SLOT_SMITHING_TEMPLATES);
        baseIcon.tick(templateItem.map(SmithingTemplateItem::getBaseSlotEmptyIcons).orElse(List.of()));
        additionalIcon.tick(templateItem.map(SmithingTemplateItem::getAdditionalSlotEmptyIcons).orElse(List.of()));
    }

    @Override
    public int getClearButtonX() {
        return leftPos + 112;
    }

    @Override
    public int getClearButtonY() {
        return y + 26;
    }

    @Override
    public void renderBackground(final GuiGraphicsExtractor graphics,
                                 final float partialTicks,
                                 final int mouseX,
                                 final int mouseY) {
        graphics.enableScissor(x, y, x + INSET_WIDTH, y + INSET_HEIGHT);
        graphics.blitSprite(GUI_TEXTURED, SPRITE, x + INSET_PADDING, y + 26, 98, 18);
        renderIcons(graphics, partialTicks);
        final int x0 = x + 106;
        final int y0 = y + 15;
        final int x1 = x + 106 + 40;
        final int y1 = y + 40 + 60;
        graphics.entity(this.armorStandPreview, 20.0F, ARMOR_STAND_TRANSLATION, ARMOR_STAND_ANGLE,
            null, x0, y0, x1, y1);
        graphics.disableScissor();
    }

    private void renderIcons(final GuiGraphicsExtractor graphics, final float partialTicks) {
        templateIcon.extractRenderState(menu, graphics, partialTicks, leftPos, topPos);
        baseIcon.extractRenderState(menu, graphics, partialTicks, leftPos, topPos);
        additionalIcon.extractRenderState(menu, graphics, partialTicks, leftPos, topPos);
    }

    @Override
    public void renderTooltip(final Font font,
                              @Nullable final Slot hoveredSlot,
                              final GuiGraphicsExtractor graphics,
                              final int mouseX,
                              final int mouseY) {
        if (hoveredSlot == null || hoveredSlot.hasItem()) {
            return;
        }
        final int firstSlotIndex = menu.getFirstSmithingTableSlotIndex();
        menu.getSmithingTableTemplateItem().ifPresentOrElse(template -> {
            if (hoveredSlot.index == firstSlotIndex + 1) {
                graphics.tooltip(font, split(font, template.getBaseSlotDescription()), mouseX, mouseY,
                    DefaultTooltipPositioner.INSTANCE, null);
            } else if (hoveredSlot.index == firstSlotIndex + 2) {
                graphics.tooltip(font, split(font, template.getAdditionSlotDescription()), mouseX, mouseY,
                    DefaultTooltipPositioner.INSTANCE, null);
            }
        }, () -> {
            if (hoveredSlot.index == firstSlotIndex) {
                graphics.tooltip(font, split(font, MISSING_SMITHING_TEMPLATE_TOOLTIP), mouseX, mouseY,
                    DefaultTooltipPositioner.INSTANCE, null);
            }
        });
    }

    private static List<ClientTooltipComponent> split(final Font font, final Component template) {
        return font.split(template, 115)
            .stream()
            .map(ClientTooltipComponent::create)
            .toList();
    }

    private void updatePreview() {
        this.armorStandPreview.leftHandItemStack = ItemStack.EMPTY;
        this.armorStandPreview.leftHandItemState.clear();
        this.armorStandPreview.headEquipment = ItemStack.EMPTY;
        this.armorStandPreview.headItem.clear();
        this.armorStandPreview.chestEquipment = ItemStack.EMPTY;
        this.armorStandPreview.legsEquipment = ItemStack.EMPTY;
        this.armorStandPreview.feetEquipment = ItemStack.EMPTY;
        if (result.isEmpty()) {
            return;
        }
        final Equippable equippable = result.get(DataComponents.EQUIPPABLE);
        final ItemModelResolver itemModelResolver = Minecraft.getInstance().getItemModelResolver();
        switch (equippable != null ? equippable.slot() : null) {
            case HEAD:
                if (HumanoidArmorLayer.shouldRender(result, EquipmentSlot.HEAD)) {
                    this.armorStandPreview.headEquipment = result.copy();
                } else {
                    itemModelResolver.updateForTopItem(this.armorStandPreview.headItem, result, ItemDisplayContext.HEAD,
                        null, null, 0);
                }
                break;
            case CHEST:
                this.armorStandPreview.chestEquipment = result.copy();
                break;
            case LEGS:
                this.armorStandPreview.legsEquipment = result.copy();
                break;
            case FEET:
                this.armorStandPreview.feetEquipment = result.copy();
                break;
            case null:
            default:
                this.armorStandPreview.leftHandItemStack = result.copy();
                itemModelResolver.updateForTopItem(this.armorStandPreview.leftHandItemState, result,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND, null, null, 0);
        }
    }
}
