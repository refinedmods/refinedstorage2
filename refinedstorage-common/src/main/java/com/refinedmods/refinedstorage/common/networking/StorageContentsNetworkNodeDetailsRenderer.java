package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.StorageConfigurationDetails;
import com.refinedmods.refinedstorage.api.network.impl.node.StorageContentsNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage.common.grid.query.GridQueryParserException;
import com.refinedmods.refinedstorage.common.grid.screen.GridSearchFieldWidget;
import com.refinedmods.refinedstorage.common.grid.view.ItemGridResource;
import com.refinedmods.refinedstorage.common.storage.PlatformStorageContentsNetworkDetails;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallTextClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.widget.History;
import com.refinedmods.refinedstorage.common.support.widget.ProgressBarWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchIconWidget;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;
import com.refinedmods.refinedstorage.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage.query.lexer.SyntaxHighlighter;
import com.refinedmods.refinedstorage.query.lexer.SyntaxHighlighterColors;
import com.refinedmods.refinedstorage.query.parser.ParserOperatorMappings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.support.Sprites.SLOT;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createStoredWithCapacityTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class StorageContentsNetworkNodeDetailsRenderer extends AbstractNetworkNodeDetailsRenderer {
    private static final GridQueryParser QUERY_PARSER = new GridQueryParser(
        LexerTokenMappings.DEFAULT_MAPPINGS,
        ParserOperatorMappings.DEFAULT_MAPPINGS
    );
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();
    private static final Component SEARCH_HELP = createTranslation("gui", "grid.search_help")
        .append("\n")
        .append(createTranslation("gui", "grid.search_help.mod_search").withStyle(ChatFormatting.GRAY))
        .append("\n")
        .append(createTranslation("gui", "grid.search_help.tag_search").withStyle(ChatFormatting.GRAY))
        .append("\n")
        .append(createTranslation("gui", "grid.search_help.tooltip_search").withStyle(ChatFormatting.GRAY));

    private static final int COLUMNS = 9;
    private static final int FILTER_SLOTS = 9;
    private static final int SLOT_SIZE = 18;
    private static final int PROGRESS_BAR_HEIGHT = 16;
    private static final Identifier SEARCH_BACKGROUND = createIdentifier("network_monitor/storage_search");
    private static final int SEARCH_HEIGHT = 12;
    private static final int SEARCH_ICON_GAP = 2;
    private static final int SEARCH_BACKGROUND_OFFSET_X = PADDING + SearchIconWidget.SEARCH_SIZE + SEARCH_ICON_GAP;
    private static final int SEARCH_FIELD_OFFSET_Y = 2;
    private static final int SEARCH_FIELD_INSET_X = 2;
    private static final int SEARCH_FIELD_INSET_WIDTH = 6;

    @Nullable
    private GridSearchFieldWidget searchField;
    @Nullable
    private SearchIconWidget searchIcon;
    @Nullable
    private ResourceRepository<GridResource> repository;

    @Override
    public List<AbstractWidget> createWidgets(final int width) {
        if (searchField == null) {
            final int searchBackgroundWidth = width - PADDING - SEARCH_BACKGROUND_OFFSET_X;
            searchField = new GridSearchFieldWidget(
                Minecraft.getInstance().font,
                0,
                0,
                searchBackgroundWidth - SEARCH_FIELD_INSET_WIDTH,
                new SyntaxHighlighter(SyntaxHighlighterColors.DEFAULT_COLORS),
                new History(SEARCH_FIELD_HISTORY)
            );
            searchField.addListener(this::onSearchTextChanged);
        }
        if (searchIcon == null) {
            searchIcon = new SearchIconWidget(0, 0, () -> SEARCH_HELP, searchField);
        }
        return List.of(searchIcon, searchField);
    }

    @Override
    public void setRepository(@Nullable final ResourceRepository<GridResource> newRepository) {
        this.repository = newRepository;
        onSearchTextChanged(searchField == null ? "" : searchField.getValue());
    }

    @Override
    protected List<ClientTooltipComponent> renderAdditionalDetails(final NetworkNodeDetails details,
                                                                   final GuiGraphicsExtractor graphics,
                                                                   final int x,
                                                                   final int scrollY,
                                                                   final int baseY,
                                                                   final int width,
                                                                   final int height,
                                                                   final int mouseX,
                                                                   final int mouseY) {
        final StorageContentsNetworkNodeDetails storageDetails = PlatformStorageContentsNetworkDetails.unwrap(details);
        if (storageDetails == null || repository == null) {
            return Collections.emptyList();
        }
        final int storedHeight = getStoredHeight();
        if (scrollY + storedHeight > baseY && scrollY < baseY + height) {
            renderStored(graphics, x, scrollY, width, storageDetails);
        }
        final StorageConfigurationDetails configuration = storageDetails.getConfiguration();
        final int configurationY = scrollY + storedHeight;
        final List<Optional<ResourceKey>> filters = getFilters(details);
        final int configurationHeight = getConfigurationHeight(configuration, filters);
        List<ClientTooltipComponent> tooltip = Collections.emptyList();
        if (configurationY + configurationHeight > baseY && configurationY < baseY + height) {
            tooltip = renderConfiguration(graphics, x, configurationY, mouseX, mouseY, configuration, filters,
                isFuzzyMode(details));
        }
        final int searchY = configurationY + configurationHeight;
        if (updateSearchPosition(x, searchY, baseY, height)) {
            final int searchBackgroundWidth = width - PADDING - SEARCH_BACKGROUND_OFFSET_X;
            graphics.blitSprite(GUI_TEXTURED, SEARCH_BACKGROUND, x + SEARCH_BACKGROUND_OFFSET_X, searchY,
                searchBackgroundWidth, SEARCH_HEIGHT);
        }
        final List<ClientTooltipComponent> contentsTooltip = renderContents(graphics, x,
            searchY + getSearchHeight(), baseY, height, mouseX, mouseY, repository);
        return tooltip.isEmpty() ? contentsTooltip : tooltip;
    }

    private boolean updateSearchPosition(final int x, final int y, final int baseY, final int height) {
        if (searchField == null || searchIcon == null) {
            return false;
        }
        searchIcon.setX(x + PADDING);
        searchIcon.setY(y);
        searchField.setX(x + SEARCH_BACKGROUND_OFFSET_X + SEARCH_FIELD_INSET_X);
        searchField.setY(y + SEARCH_FIELD_OFFSET_Y);
        final boolean visible = y >= baseY - SEARCH_HEIGHT && y + SEARCH_HEIGHT <= baseY + height;
        searchIcon.visible = visible;
        searchField.visible = visible;
        return visible;
    }

    @Override
    protected int getAdditionalHeight(final NetworkNodeDetails details) {
        final StorageContentsNetworkNodeDetails storageDetails = PlatformStorageContentsNetworkDetails.unwrap(details);
        if (storageDetails != null && repository != null) {
            final int size = repository.getViewList().size();
            final int rows = (int) Math.ceil((float) size / (float) COLUMNS);
            return getStoredHeight()
                + getConfigurationHeight(storageDetails.getConfiguration(), getFilters(details))
                + getSearchHeight()
                + (rows * SLOT_SIZE);
        }
        return 0;
    }

    private void onSearchTextChanged(final String text) {
        if (repository == null) {
            return;
        }
        try {
            repository.setFilterAndSort(QUERY_PARSER.parse(text));
            setSearchFieldValid(true);
        } catch (final GridQueryParserException e) {
            repository.setFilterAndSort((view, resource) -> false);
            setSearchFieldValid(false);
        }
    }

    private void setSearchFieldValid(final boolean valid) {
        if (searchField != null) {
            searchField.setValid(valid);
        }
    }

    private static int getStoredHeight() {
        return getLineHeight() + PROGRESS_BAR_HEIGHT + PADDING;
    }

    private static int getSearchHeight() {
        return SEARCH_HEIGHT + PADDING;
    }

    private static boolean isFuzzyMode(final NetworkNodeDetails details) {
        return details instanceof PlatformStorageContentsNetworkDetails platformDetails
            && platformDetails.isFuzzyMode();
    }

    private static List<Optional<ResourceKey>> getFilters(final NetworkNodeDetails details) {
        if (details instanceof PlatformStorageContentsNetworkDetails platformDetails) {
            return platformDetails.getFilters();
        }
        return List.of();
    }

    private static boolean shouldShowFilters(final StorageConfigurationDetails configuration,
                                             final List<Optional<ResourceKey>> filters) {
        return configuration.filterMode() == FilterMode.ALLOW || filters.stream().anyMatch(Optional::isPresent);
    }

    private static int getConfigurationHeight(final StorageConfigurationDetails configuration,
                                              final List<Optional<ResourceKey>> filters) {
        int height = 0;
        if (shouldShowFilters(configuration, filters)) {
            height += getLineHeight() + SLOT_SIZE + PADDING;
        }
        height += getLineHeight() * 2;
        height += configuration.insertPriority() == configuration.extractPriority()
            ? getLineHeight()
            : getLineHeight() * 2;
        if (configuration.voidExcess()) {
            height += getLineHeight();
        }
        return height + PADDING;
    }

    private static List<ClientTooltipComponent> renderConfiguration(final GuiGraphicsExtractor graphics,
                                                                    final int x,
                                                                    final int y,
                                                                    final int mouseX,
                                                                    final int mouseY,
                                                                    final StorageConfigurationDetails configuration,
                                                                    final List<Optional<ResourceKey>> filters,
                                                                    final boolean fuzzyMode) {
        List<ClientTooltipComponent> tooltip = Collections.emptyList();
        int lineY = y;
        if (shouldShowFilters(configuration, filters)) {
            renderLine(graphics, x, lineY, createTranslation("gui", "network_monitor.details.filter_mode",
                getFilterModeText(configuration.filterMode())));
            lineY += getLineHeight();
            tooltip = renderFilters(graphics, x, lineY, mouseX, mouseY, filters);
            lineY += SLOT_SIZE + PADDING;
        }
        renderLine(graphics, x, lineY, createTranslation("gui", "network_monitor.details.fuzzy_mode",
            createTranslation("gui", fuzzyMode ? "fuzzy_mode.on" : "fuzzy_mode.off")));
        lineY += getLineHeight();
        renderLine(graphics, x, lineY, createTranslation("gui", "network_monitor.details.access_mode",
            getAccessModeText(configuration.accessMode())));
        lineY += getLineHeight();
        if (configuration.insertPriority() == configuration.extractPriority()) {
            renderLine(graphics, x, lineY, createTranslation("gui", "network_monitor.details.priority",
                configuration.insertPriority()));
            lineY += getLineHeight();
        } else {
            renderLine(graphics, x, lineY, createTranslation("gui", "network_monitor.details.insert_priority",
                configuration.insertPriority()));
            lineY += getLineHeight();
            renderLine(graphics, x, lineY, createTranslation("gui", "network_monitor.details.extract_priority",
                configuration.extractPriority()));
            lineY += getLineHeight();
        }
        if (configuration.voidExcess()) {
            renderLine(graphics, x, lineY, createTranslation("gui", "network_monitor.details.void_excess"));
        }
        return tooltip;
    }

    private static void renderLine(final GuiGraphicsExtractor graphics,
                                   final int x,
                                   final int y,
                                   final Component text) {
        SmallText.render(graphics, Minecraft.getInstance().font, text.getVisualOrderText(), x + PADDING, y,
            0xFF404040, false, SmallText.correctScale(SmallText.DEFAULT_SCALE));
    }

    private static Component getFilterModeText(final FilterMode filterMode) {
        return switch (filterMode) {
            case ALLOW -> createTranslation("gui", "filter_mode.allow");
            case BLOCK -> createTranslation("gui", "filter_mode.block");
        };
    }

    private static Component getAccessModeText(final AccessMode accessMode) {
        return switch (accessMode) {
            case INSERT_EXTRACT -> createTranslation("gui", "access_mode.insert_extract");
            case INSERT -> createTranslation("gui", "access_mode.insert");
            case EXTRACT -> createTranslation("gui", "access_mode.extract");
        };
    }

    private static List<ClientTooltipComponent> renderFilters(final GuiGraphicsExtractor graphics,
                                                              final int x,
                                                              final int y,
                                                              final int mouseX,
                                                              final int mouseY,
                                                              final List<Optional<ResourceKey>> filters) {
        ResourceKey hovering = null;
        for (int i = 0; i < FILTER_SLOTS; ++i) {
            final int slotX = x + (i * SLOT_SIZE);
            graphics.blitSprite(GUI_TEXTURED, SLOT, slotX, y, SLOT_SIZE, SLOT_SIZE);
            if (i >= filters.size() || filters.get(i).isEmpty()) {
                continue;
            }
            final ResourceKey filter = filters.get(i).orElseThrow();
            final boolean interact = mouseX >= slotX
                && mouseY >= y
                && mouseX <= slotX + 16
                && mouseY <= y + 16;
            if (interact) {
                ClientPlatformUtil.renderSlotHighlightBack(graphics, slotX + 1, y + 1);
                hovering = filter;
            }
            RefinedStorageClientApi.INSTANCE.getResourceRendering(filter.getClass())
                .render(filter, graphics, slotX + 1, y + 1);
            if (interact) {
                ClientPlatformUtil.renderSlotHighlightFront(graphics, slotX + 1, y + 1);
            }
        }
        return hovering == null ? Collections.emptyList() : getFilterTooltip(hovering, graphics, mouseX);
    }

    private static List<ClientTooltipComponent> getFilterTooltip(final ResourceKey filter,
                                                                 final GuiGraphicsExtractor graphics,
                                                                 final int mouseX) {
        final ItemStack stackContext = filter instanceof ItemResource itemResource
            ? itemResource.toItemStack()
            : ItemStack.EMPTY;
        return Platform.INSTANCE.processTooltipComponents(
            stackContext,
            graphics,
            mouseX,
            Optional.empty(),
            RefinedStorageClientApi.INSTANCE.getResourceRendering(filter.getClass()).getTooltip(filter)
        );
    }

    private static void renderStored(final GuiGraphicsExtractor graphics,
                                     final int x,
                                     final int y,
                                     final int width,
                                     final StorageContentsNetworkNodeDetails details) {
        final Font font = Minecraft.getInstance().font;
        final Component stored = getStoredText(details);
        final float scale = SmallText.correctScale(SmallText.DEFAULT_SCALE);
        SmallText.render(graphics, font, stored.getVisualOrderText(), x + PADDING, y, 0xFF404040, false,
            scale);
        ProgressBarWidget.renderHorizontal(graphics, x + PADDING, y + getLineHeight(),
            width - (PADDING * 2), PROGRESS_BAR_HEIGHT, details.getProgress());
    }

    private static MutableComponent getStoredText(final StorageContentsNetworkNodeDetails details) {
        if (details.hasCapacity()) {
            return createStoredWithCapacityTranslation(details.getStored(), details.getCapacity(),
                details.getProgress(), 0xFF404040).withColor(0xFF404040);
        }
        return createTranslation("misc", "stored", format(details.getStored())).withColor(0xFF404040);
    }

    private static List<ClientTooltipComponent> renderContents(final GuiGraphicsExtractor graphics,
                                                               final int x,
                                                               final int y,
                                                               final int baseY,
                                                               final int height,
                                                               final int mouseX,
                                                               final int mouseY,
                                                               final ResourceRepository<GridResource> repository) {
        final List<GridResource> resources = repository.getViewList();
        GridResource hovering = null;
        for (int i = getFirstVisibleIndex(y, baseY); i < resources.size(); ++i) {
            final int slotY = y + (i / COLUMNS) * SLOT_SIZE;
            if (slotY >= baseY + height) {
                break;
            }
            final GridResource resource = resources.get(i);
            final int slotX = x + (i % COLUMNS) * SLOT_SIZE;
            graphics.blitSprite(GUI_TEXTURED, SLOT, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            final boolean interact = mouseX >= slotX
                && mouseY >= slotY
                && mouseX <= slotX + 16
                && mouseY <= slotY + 16;
            renderSlotContents(graphics, slotX + 1, slotY + 1, resource, repository, interact);
            if (interact) {
                hovering = resource;
            }
        }
        return hovering != null ? getTooltip(hovering, repository, graphics, mouseX) : Collections.emptyList();
    }

    private static int getFirstVisibleIndex(final int y, final int visibleY) {
        final int hiddenRows = Math.max(0, Math.floorDiv(visibleY - y, SLOT_SIZE));
        return hiddenRows * COLUMNS;
    }

    private static void renderSlotContents(final GuiGraphicsExtractor graphics,
                                           final int x,
                                           final int y,
                                           final GridResource resource,
                                           final ResourceRepository<GridResource> repository,
                                           final boolean interact) {
        if (interact) {
            ClientPlatformUtil.renderSlotHighlightBack(graphics, x, y);
        }
        resource.render(graphics, x, y);
        final boolean large = Minecraft.getInstance().isEnforceUnicode()
            || Platform.INSTANCE.getConfig().getGrid().isLargeFont();
        ResourceSlotRendering.renderAmount(graphics, x, y, resource.getDisplayedAmount(repository), 0xFFFFFFFF, large);
        if (interact) {
            ClientPlatformUtil.renderSlotHighlightFront(graphics, x, y);
        }
    }

    private static List<ClientTooltipComponent> getTooltip(final GridResource resource,
                                                           final ResourceRepository<GridResource> repository,
                                                           final GuiGraphicsExtractor graphics,
                                                           final int mouseX) {
        final ItemStack stackContext = resource instanceof ItemGridResource itemResource
            ? itemResource.getItemStack()
            : ItemStack.EMPTY;
        final List<ClientTooltipComponent> lines = Platform.INSTANCE.processTooltipComponents(
            stackContext,
            graphics,
            mouseX,
            resource.getTooltipImage(),
            resource.getTooltip()
        );
        lines.add(new SmallTextClientTooltipComponent(
            createTranslation("misc", "total", resource.getAmountInTooltip(repository))
                .withStyle(ChatFormatting.GRAY)
        ));
        return lines;
    }
}
