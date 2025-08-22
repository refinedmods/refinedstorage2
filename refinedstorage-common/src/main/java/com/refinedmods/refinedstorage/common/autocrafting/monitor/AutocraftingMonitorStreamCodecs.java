package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.InWorldExternalPatternSinkKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import java.util.ArrayList;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public final class AutocraftingMonitorStreamCodecs {
    public static final StreamCodec<RegistryFriendlyByteBuf, TaskId> TASK_ID_STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, TaskId::id,
        TaskId::new
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, TaskStatus.Item> STATUS_ITEM_STREAM_CODEC =
        new StatusItemStreamCodec();
    private static final StreamCodec<RegistryFriendlyByteBuf, TaskStatus.TaskInfo> INFO_STREAM_CODEC =
        StreamCodec.composite(
            TASK_ID_STREAM_CODEC, TaskStatus.TaskInfo::id,
            ResourceCodecs.STREAM_CODEC, s -> (PlatformResourceKey) s.resource(),
            ByteBufCodecs.VAR_LONG, TaskStatus.TaskInfo::amount,
            ByteBufCodecs.VAR_LONG, TaskStatus.TaskInfo::startTime,
            TaskStatus.TaskInfo::new
        );
    public static final StreamCodec<RegistryFriendlyByteBuf, TaskStatus> STATUS_STREAM_CODEC =
        StreamCodec.composite(
            INFO_STREAM_CODEC, TaskStatus::info,
            PlatformUtil.enumStreamCodec(TaskState.values()), TaskStatus::state,
            ByteBufCodecs.DOUBLE, TaskStatus::percentageCompleted,
            ByteBufCodecs.collection(ArrayList::new, STATUS_ITEM_STREAM_CODEC), TaskStatus::items,
            TaskStatus::new
        );

    private AutocraftingMonitorStreamCodecs() {
    }

    private static class StatusItemStreamCodec implements StreamCodec<RegistryFriendlyByteBuf, TaskStatus.Item> {
        private static final StreamCodec<ByteBuf, TaskStatus.ItemType> TYPE_STREAM_CODEC = PlatformUtil.enumStreamCodec(
            TaskStatus.ItemType.values()
        );

        @Override
        public TaskStatus.Item decode(final RegistryFriendlyByteBuf buf) {
            return new TaskStatus.Item(
                ResourceCodecs.STREAM_CODEC.decode(buf),
                TYPE_STREAM_CODEC.decode(buf),
                decodeSinkKey(buf),
                buf.readLong(),
                buf.readLong(),
                buf.readLong(),
                buf.readLong()
            );
        }

        @Nullable
        private ExternalPatternSinkKey decodeSinkKey(final RegistryFriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                return new InWorldExternalPatternSinkKey(buf.readUtf(), ItemStack.STREAM_CODEC.decode(buf));
            }
            return null;
        }

        @Override
        public void encode(final RegistryFriendlyByteBuf buf, final TaskStatus.Item item) {
            ResourceCodecs.STREAM_CODEC.encode(buf, (PlatformResourceKey) item.resource());
            TYPE_STREAM_CODEC.encode(buf, item.type());
            encodeSinkKey(buf, item.sinkKey());
            buf.writeLong(item.stored());
            buf.writeLong(item.processing());
            buf.writeLong(item.scheduled());
            buf.writeLong(item.crafting());
        }

        private void encodeSinkKey(final RegistryFriendlyByteBuf buf,
                                   @Nullable final ExternalPatternSinkKey sinkKey) {
            if (sinkKey instanceof InWorldExternalPatternSinkKey(String name, ItemStack stack)) {
                buf.writeBoolean(true);
                buf.writeUtf(name);
                ItemStack.STREAM_CODEC.encode(buf, stack);
            } else {
                buf.writeBoolean(false);
            }
        }
    }
}
