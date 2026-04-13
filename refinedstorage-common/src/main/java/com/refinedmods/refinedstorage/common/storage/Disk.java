package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.storage.StorageState;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public record Disk(@Nullable Item item, StorageState state) {
    public static final Codec<Disk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item")
            .forGetter(disk -> Optional.ofNullable(disk.item)),
        StringRepresentable.fromValues(SerializableStorageState::values).fieldOf("state")
            .forGetter(disk -> SerializableStorageState.toSerializedState(disk.state))
    ).apply(instance, (item, state) -> new Disk(item.orElse(null), state.toState())));
    public static final Codec<List<Disk>> LIST_CODEC = Codec.list(CODEC);

    private enum SerializableStorageState implements StringRepresentable {
        NONE("none"),
        INACTIVE("inactive"),
        NORMAL("normal"),
        NEAR_CAPACITY("near_capacity"),
        FULL("full");

        private final String name;

        SerializableStorageState(final String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public StorageState toState() {
            return switch (this) {
                case NONE -> StorageState.NONE;
                case INACTIVE -> StorageState.INACTIVE;
                case NORMAL -> StorageState.NORMAL;
                case NEAR_CAPACITY -> StorageState.NEAR_CAPACITY;
                case FULL -> StorageState.FULL;
            };
        }

        public static SerializableStorageState toSerializedState(final StorageState state) {
            return switch (state) {
                case NONE -> NONE;
                case INACTIVE -> INACTIVE;
                case NORMAL -> NORMAL;
                case NEAR_CAPACITY -> NEAR_CAPACITY;
                case FULL -> FULL;
            };
        }
    }
}
