package com.refinedmods.refinedstorage2.platform.common.support.direction;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public interface DirectionType<T extends Enum<T> & StringRepresentable> {
    EnumProperty<T> getProperty();

    T getDefault();

    Direction extractDirection(T direction);

    T getDirection(Direction clickedFace, Direction playerFacing, float playerPitch);

    T rotate(T direction);
}
