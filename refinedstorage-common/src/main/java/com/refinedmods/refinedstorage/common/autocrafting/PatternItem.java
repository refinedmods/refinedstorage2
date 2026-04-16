package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderItem;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslationKey;

public class PatternItem extends Item implements PatternProviderItem {
    private static final Map<UUID, PatternResolver.ResolvedCraftingPattern> CRAFTING_PATTERN_CACHE = new HashMap<>();
    private static final Map<UUID, PatternResolver.ResolvedProcessingPattern> PROCESSING_PATTERN_CACHE =
        new HashMap<>();
    private static final Map<UUID, PatternResolver.ResolvedSmithingTablePattern> SMITHING_TABLE_PATTERN_CACHE =
        new HashMap<>();
    private static final Map<UUID, PatternResolver.ResolvedStonecutterPattern> STONE_CUTTER_PATTERN_CACHE =
        new HashMap<>();

    private static final Component HELP = createTranslation("item", "pattern.help");
    private static final MutableComponent FUZZY_MODE = createTranslation("item", "pattern.fuzzy_mode")
        .withStyle(ChatFormatting.YELLOW);

    private final PatternResolver resolver = new PatternResolver();

    public PatternItem() {
        super(new Item.Properties());
    }

    @Override
    public String getDescriptionId(final ItemStack stack) {
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state != null) {
            return createTranslationKey("misc", "pattern." + state.type().getSerializedName());
        }
        return super.getDescriptionId(stack);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, lines, tooltipFlag);
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state == null) {
            return;
        }
        final CraftingPatternState craftingState = stack.get(DataComponents.INSTANCE.getCraftingPatternState());
        if (craftingState != null && craftingState.fuzzyMode()) {
            lines.add(FUZZY_MODE);
        }
    }

    public boolean hasMapping(final ItemStack stack) {
        return stack.has(DataComponents.INSTANCE.getPatternState());
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state == null) {
            return Optional.of(new HelpTooltipComponent(HELP));
        }
        final Level level = ClientPlatformUtil.getClientLevel();
        if (level == null) {
            return Optional.empty();
        }
        return switch (state.type()) {
            case CRAFTING -> {
                final CraftingPatternState craftingState = stack.get(DataComponents.INSTANCE.getCraftingPatternState());
                if (craftingState == null) {
                    yield Optional.empty();
                }
                yield getCachedCraftingPattern(state, stack, level).map(pattern -> new CraftingPatternTooltipComponent(
                    state.id(),
                    pattern,
                    craftingState.input().input().width(),
                    craftingState.input().input().height()
                ));
            }
            case PROCESSING -> {
                final ProcessingPatternState processingState = stack.get(
                    DataComponents.INSTANCE.getProcessingPatternState()
                );
                if (processingState == null) {
                    yield Optional.empty();
                }
                yield Optional.of(new ProcessingPatternTooltipComponent(state.id(), processingState));
            }
            case STONECUTTER -> getCachedStonecutterPattern(state, stack, level).map(
                pattern -> new StonecutterPatternTooltipComponent(
                    state.id(),
                    pattern
                ));
            case SMITHING_TABLE -> getCachedSmithingTablePattern(state, stack, level).map(
                pattern -> new SmithingTablePatternTooltipComponent(
                    state.id(),
                    pattern
                ));
        };
    }

    @Nullable
    @Override
    public UUID getId(final ItemStack stack) {
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state == null) {
            return null;
        }
        return state.id();
    }

    @Override
    public Optional<Pattern> getPattern(final ItemStack stack, final Level level) {
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state == null) {
            return Optional.empty();
        }
        return switch (state.type()) {
            case CRAFTING -> resolver.getCraftingPattern(stack, level, state)
                .map(PatternResolver.ResolvedCraftingPattern::pattern);
            case PROCESSING -> resolver.getProcessingPattern(state, stack)
                .map(PatternResolver.ResolvedProcessingPattern::pattern);
            case STONECUTTER -> resolver.getStonecutterPattern(stack, level, state)
                .map(PatternResolver.ResolvedStonecutterPattern::pattern);
            case SMITHING_TABLE -> resolver.getSmithingTablePattern(state, stack, level)
                .map(PatternResolver.ResolvedSmithingTablePattern::pattern);
        };
    }

    @Override
    public Optional<ItemStack> getOutput(final ItemStack stack, final Level level) {
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state == null) {
            return Optional.empty();
        }
        return switch (state.type()) {
            case CRAFTING -> getCachedCraftingPattern(state, stack, level)
                .map(PatternResolver.ResolvedCraftingPattern::output)
                .map(ResourceAmount::resource)
                .filter(ItemResource.class::isInstance)
                .map(ItemResource.class::cast)
                .map(ItemResource::toItemStack);
            case PROCESSING -> getCachedProcessingPattern(state, stack)
                .map(PatternResolver.ResolvedProcessingPattern::pattern)
                .filter(pattern -> pattern.layout().outputs().size() == 1)
                .map(pattern -> pattern.layout().outputs().getFirst().resource())
                .filter(ItemResource.class::isInstance)
                .map(ItemResource.class::cast)
                .map(ItemResource::toItemStack);
            case STONECUTTER -> getCachedStonecutterPattern(state, stack, level)
                .map(PatternResolver.ResolvedStonecutterPattern::output)
                .map(ResourceAmount::resource)
                .filter(ItemResource.class::isInstance)
                .map(ItemResource.class::cast)
                .map(ItemResource::toItemStack);
            case SMITHING_TABLE -> getCachedSmithingTablePattern(state, stack, level)
                .map(PatternResolver.ResolvedSmithingTablePattern::output)
                .map(ItemResource::toItemStack);
        };
    }

    private Optional<PatternResolver.ResolvedCraftingPattern> getCachedCraftingPattern(final PatternState state,
                                                                                       final ItemStack stack,
                                                                                       final Level level) {
        final PatternResolver.ResolvedCraftingPattern pattern = CRAFTING_PATTERN_CACHE.get(state.id());
        if (pattern == null) {
            return resolver.getCraftingPattern(stack, level, state).map(resolved -> {
                CRAFTING_PATTERN_CACHE.put(state.id(), resolved);
                return resolved;
            });
        }
        return Optional.of(pattern);
    }

    private Optional<PatternResolver.ResolvedSmithingTablePattern> getCachedSmithingTablePattern(
        final PatternState state,
        final ItemStack stack,
        final Level level
    ) {
        final PatternResolver.ResolvedSmithingTablePattern pattern = SMITHING_TABLE_PATTERN_CACHE.get(state.id());
        if (pattern == null) {
            return resolver.getSmithingTablePattern(state, stack, level).map(resolved -> {
                SMITHING_TABLE_PATTERN_CACHE.put(state.id(), resolved);
                return resolved;
            });
        }
        return Optional.of(pattern);
    }

    private Optional<PatternResolver.ResolvedStonecutterPattern> getCachedStonecutterPattern(
        final PatternState state,
        final ItemStack stack,
        final Level level
    ) {
        final PatternResolver.ResolvedStonecutterPattern pattern = STONE_CUTTER_PATTERN_CACHE.get(state.id());
        if (pattern == null) {
            return resolver.getStonecutterPattern(stack, level, state).map(resolved -> {
                STONE_CUTTER_PATTERN_CACHE.put(state.id(), resolved);
                return resolved;
            });
        }
        return Optional.of(pattern);
    }

    private Optional<PatternResolver.ResolvedProcessingPattern> getCachedProcessingPattern(
        final PatternState state,
        final ItemStack stack
    ) {
        final PatternResolver.ResolvedProcessingPattern pattern = PROCESSING_PATTERN_CACHE.get(state.id());
        if (pattern == null) {
            return resolver.getProcessingPattern(state, stack).map(resolved -> {
                PROCESSING_PATTERN_CACHE.put(state.id(), resolved);
                return resolved;
            });
        }
        return Optional.of(pattern);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player.isCrouching()) {
            return new InteractionResultHolder<>(
                InteractionResult.CONSUME,
                new ItemStack(Items.INSTANCE.getPattern(), stack.getCount())
            );
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    public record CraftingPatternTooltipComponent(UUID id,
                                                  PatternResolver.ResolvedCraftingPattern pattern,
                                                  int width,
                                                  int height)
        implements TooltipComponent {
    }

    public record ProcessingPatternTooltipComponent(UUID id, ProcessingPatternState state)
        implements TooltipComponent {
    }

    public record StonecutterPatternTooltipComponent(UUID id, PatternResolver.ResolvedStonecutterPattern pattern)
        implements TooltipComponent {
    }

    public record SmithingTablePatternTooltipComponent(UUID id, PatternResolver.ResolvedSmithingTablePattern pattern)
        implements TooltipComponent {
    }
}
