package com.refinedmods.refinedstorage2.platform.common.block.direction;

import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BiDirectionType implements DirectionType<BiDirection> {
    public static final DirectionType<BiDirection> INSTANCE = new BiDirectionType();

    private static final EnumProperty<BiDirection> PROPERTY = EnumProperty.create("direction", BiDirection.class);

    private BiDirectionType() {
    }

    @Override
    public EnumProperty<BiDirection> getProperty() {
        return PROPERTY;
    }

    @Override
    public BiDirection getDefault() {
        return BiDirection.NORTH;
    }

    @Override
    public BiDirection getDirection(final Direction clickedFace,
                                    final Direction playerFacing,
                                    final float playerPitch) {
        if (playerPitch > 65) {
            return BiDirection.forUp(playerFacing);
        } else if (playerPitch < -65) {
            return BiDirection.forDown(playerFacing.getOpposite());
        } else {
            return BiDirection.forHorizontal(playerFacing.getOpposite());
        }
    }

    @Override
    public BiDirection rotate(final BiDirection direction) {
        return direction.rotate();
    }
}
