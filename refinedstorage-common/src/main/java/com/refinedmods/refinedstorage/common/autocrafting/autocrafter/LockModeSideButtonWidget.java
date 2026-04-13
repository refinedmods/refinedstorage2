package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class LockModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final String PREFIX = "autocrafter.lock_mode";

    private static final MutableComponent TITLE = createTranslation("gui", PREFIX);
    private static final Identifier NEVER = createIdentifier("widget/side_button/autocrafter/lock_mode/never");
    private static final Identifier LOCK_UNTIL_REDSTONE_PULSE_RECEIVED = createIdentifier(
        "widget/side_button/autocrafter/lock_mode/lock_until_redstone_pulse_is_received"
    );
    private static final Identifier LOCK_UNTIL_CONNECTED_MACHINE_IS_EMPTY = createIdentifier(
        "widget/side_button/autocrafter/lock_mode/lock_until_connected_machine_is_empty"
    );
    private static final Identifier LOCK_UNTIL_ALL_OUTPUTS_ARE_RECEIVED = createIdentifier(
        "widget/side_button/autocrafter/lock_mode/lock_until_all_outputs_are_received"
    );
    private static final Identifier LOCK_UNTIL_HIGH_REDSTONE_SIGNAL = createIdentifier(
        "widget/side_button/autocrafter/lock_mode/lock_until_high_redstone_signal"
    );
    private static final Identifier LOCK_UNTIL_LOW_REDSTONE_SIGNAL = createIdentifier(
        "widget/side_button/autocrafter/lock_mode/lock_until_low_redstone_signal"
    );

    private static final List<MutableComponent> NEVER_TITLE = List.of(
        createTranslation("gui", PREFIX + ".never").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> LOCK_UNTIL_REDSTONE_PULSE_RECEIVED_TITLE = List.of(createTranslation(
        "gui", PREFIX + ".lock_until_redstone_pulse_received"
    ).withStyle(ChatFormatting.GRAY));
    private static final List<MutableComponent> LOCK_UNTIL_CONNECTED_MACHINE_IS_EMPTY_TITLE = List.of(createTranslation(
        "gui", PREFIX + ".lock_until_connected_machine_is_empty"
    ).withStyle(ChatFormatting.GRAY));
    private static final List<MutableComponent> LOCK_UNTIL_ALL_OUTPUTS_ARE_RECEIVED_TITLE = List.of(createTranslation(
        "gui", PREFIX + ".lock_until_all_outputs_are_received"
    ).withStyle(ChatFormatting.GRAY));
    private static final List<MutableComponent> LOCK_UNTIL_HIGH_REDSTONE_SIGNAL_TITLE = List.of(createTranslation(
        "gui", PREFIX + ".lock_until_high_redstone_signal"
    ).withStyle(ChatFormatting.GRAY));
    private static final List<MutableComponent> LOCK_UNTIL_LOW_REDSTONE_SIGNAL_TITLE = List.of(createTranslation(
        "gui", PREFIX + ".lock_until_low_redstone_signal"
    ).withStyle(ChatFormatting.GRAY));

    private final ClientProperty<LockMode> property;

    LockModeSideButtonWidget(final ClientProperty<LockMode> property) {
        super(createPressAction(property));
        this.property = property;
    }

    private static OnPress createPressAction(final ClientProperty<LockMode> property) {
        return btn -> property.setValue(property.getValue().toggle());
    }

    @Override
    protected Identifier getSprite() {
        return switch (property.getValue()) {
            case NEVER -> NEVER;
            case LOCK_UNTIL_REDSTONE_PULSE_RECEIVED -> LOCK_UNTIL_REDSTONE_PULSE_RECEIVED;
            case LOCK_UNTIL_CONNECTED_MACHINE_IS_EMPTY -> LOCK_UNTIL_CONNECTED_MACHINE_IS_EMPTY;
            case LOCK_UNTIL_ALL_OUTPUTS_ARE_RECEIVED -> LOCK_UNTIL_ALL_OUTPUTS_ARE_RECEIVED;
            case LOCK_UNTIL_HIGH_REDSTONE_SIGNAL -> LOCK_UNTIL_HIGH_REDSTONE_SIGNAL;
            case LOCK_UNTIL_LOW_REDSTONE_SIGNAL -> LOCK_UNTIL_LOW_REDSTONE_SIGNAL;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (property.getValue()) {
            case NEVER -> NEVER_TITLE;
            case LOCK_UNTIL_REDSTONE_PULSE_RECEIVED -> LOCK_UNTIL_REDSTONE_PULSE_RECEIVED_TITLE;
            case LOCK_UNTIL_CONNECTED_MACHINE_IS_EMPTY -> LOCK_UNTIL_CONNECTED_MACHINE_IS_EMPTY_TITLE;
            case LOCK_UNTIL_ALL_OUTPUTS_ARE_RECEIVED -> LOCK_UNTIL_ALL_OUTPUTS_ARE_RECEIVED_TITLE;
            case LOCK_UNTIL_HIGH_REDSTONE_SIGNAL -> LOCK_UNTIL_HIGH_REDSTONE_SIGNAL_TITLE;
            case LOCK_UNTIL_LOW_REDSTONE_SIGNAL -> LOCK_UNTIL_LOW_REDSTONE_SIGNAL_TITLE;
        };
    }
}
