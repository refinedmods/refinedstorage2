package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternType;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class PatternTypeItemModelProperty implements SelectItemModelProperty<PatternType> {
    public static final Identifier NAME = createIdentifier("pattern_type");

    private static final Codec<PatternType> CODEC = StringRepresentable.fromValues(PatternType::values);
    public static final Type<? extends SelectItemModelProperty<PatternType>, PatternType> PROPERTY_TYPE
        = Type.create(MapCodec.unit(new PatternTypeItemModelProperty()), CODEC);

    @Override
    @Nullable
    public PatternType get(final ItemStack stack, @Nullable final ClientLevel clientLevel,
                           @Nullable final LivingEntity livingEntity, final int i,
                           final ItemDisplayContext itemDisplayContext) {
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state == null) {
            return null;
        }
        return state.type();
    }

    @Override
    public Codec<PatternType> valueCodec() {
        return CODEC;
    }

    @Override
    public Type<? extends SelectItemModelProperty<PatternType>, PatternType> type() {
        return PROPERTY_TYPE;
    }
}
