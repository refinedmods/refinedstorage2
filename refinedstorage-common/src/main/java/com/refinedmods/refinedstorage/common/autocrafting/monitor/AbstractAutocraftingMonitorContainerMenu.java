package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class AbstractAutocraftingMonitorContainerMenu extends AbstractBaseContainerMenu
    implements TaskStatusListener, AutocraftingMonitorWatcher, AutocraftingTaskButton.StateProvider {
    private final Map<TaskId, TaskStatus> statusByTaskId;
    private final List<TaskStatus.TaskInfo> tasks;
    private final List<TaskStatus.TaskInfo> tasksView;
    @Nullable
    private final AutocraftingMonitor autocraftingMonitor;
    private final Player player;

    @Nullable
    private AutocraftingMonitorListener listener;
    @Nullable
    private TaskId currentTaskId;
    private boolean active;

    protected AbstractAutocraftingMonitorContainerMenu(final MenuType<?> menuType,
                                                       final int syncId,
                                                       final Inventory playerInventory,
                                                       final AutocraftingMonitorData data) {
        super(menuType, syncId);
        this.statusByTaskId = data.statuses().stream().collect(Collectors.toMap(
            s -> s.info().id(),
            s -> s
        ));
        this.tasks = data.statuses().stream().map(TaskStatus::info).collect(Collectors.toList());
        this.tasksView = Collections.unmodifiableList(tasks);
        this.currentTaskId = data.statuses().isEmpty() ? null : data.statuses().getFirst().info().id();
        this.autocraftingMonitor = null;
        this.active = data.active();
        this.player = playerInventory.player;
    }

    AbstractAutocraftingMonitorContainerMenu(final MenuType<?> menuType,
                                             final int syncId,
                                             final Player player,
                                             final AutocraftingMonitor autocraftingMonitor) {
        super(menuType, syncId);
        this.statusByTaskId = Collections.emptyMap();
        this.tasks = Collections.emptyList();
        this.tasksView = Collections.emptyList();
        this.currentTaskId = null;
        this.autocraftingMonitor = autocraftingMonitor;
        this.player = player;
        this.autocraftingMonitor.addListener(this);
        this.autocraftingMonitor.addWatcher(this);
    }

    @Override
    public void removed(final Player removedPlayer) {
        super.removed(removedPlayer);
        if (autocraftingMonitor != null) {
            autocraftingMonitor.removeListener(this);
            autocraftingMonitor.removeWatcher(this);
        }
    }

    void setListener(@Nullable final AutocraftingMonitorListener listener) {
        this.listener = listener;
    }

    List<TaskStatus.Item> getCurrentItems() {
        final TaskStatus status = statusByTaskId.get(currentTaskId);
        if (status == null) {
            return Collections.emptyList();
        }
        return status.items();
    }

    List<TaskStatus.TaskInfo> getTasksView() {
        return tasksView;
    }

    @Override
    public double getPercentageCompleted(final TaskId taskId) {
        final TaskStatus status = statusByTaskId.get(taskId);
        return status == null ? 0 : status.percentageCompleted();
    }

    @Override
    @Nullable
    public TaskState getState(final TaskId taskId) {
        final TaskStatus status = statusByTaskId.get(taskId);
        return status == null ? null : status.state();
    }

    void setCurrentTaskId(@Nullable final TaskId taskId) {
        this.currentTaskId = taskId;
        loadCurrentTask();
    }

    void loadCurrentTask() {
        if (listener != null) {
            listener.currentTaskChanged(currentTaskId == null ? null : statusByTaskId.get(currentTaskId));
        }
    }

    @Override
    public void taskStatusChanged(final TaskStatus status) {
        if (autocraftingMonitor != null && player instanceof ServerPlayer serverPlayer) {
            S2CPackets.sendAutocraftingMonitorTaskStatusChanged(serverPlayer, status);
            return;
        }
        statusByTaskId.put(status.info().id(), status);
    }

    @Override
    public void taskRemoved(final TaskId id) {
        if (autocraftingMonitor != null && player instanceof ServerPlayer serverPlayer) {
            S2CPackets.sendAutocraftingMonitorTaskRemoved(serverPlayer, id);
            return;
        }
        statusByTaskId.remove(id);
        tasks.removeIf(task -> task.id().equals(id));
        if (listener != null) {
            listener.taskRemoved(id);
        }
        if (id.equals(currentTaskId)) {
            this.currentTaskId = tasks.isEmpty() ? null : tasks.getFirst().id();
            loadCurrentTask();
        }
    }

    @Override
    public void taskAdded(final TaskStatus status) {
        if (autocraftingMonitor != null && player instanceof ServerPlayer serverPlayer) {
            S2CPackets.sendAutocraftingMonitorTaskAdded(serverPlayer, status);
            return;
        }
        statusByTaskId.put(status.info().id(), status);
        tasks.add(status.info());
        if (listener != null) {
            listener.taskAdded(status);
        }
        if (currentTaskId == null) {
            this.currentTaskId = status.info().id();
            loadCurrentTask();
        }
    }

    public void cancelTask(final TaskId taskId) {
        if (autocraftingMonitor != null) {
            autocraftingMonitor.cancel(taskId);
        }
    }

    void cancelCurrentTask() {
        if (currentTaskId != null) {
            C2SPackets.sendAutocraftingMonitorCancel(currentTaskId);
        }
    }

    public void cancelAllTasks() {
        if (autocraftingMonitor != null) {
            autocraftingMonitor.cancelAll();
        } else {
            C2SPackets.sendAutocraftingMonitorCancelAll();
        }
    }

    @Override
    public void activeChanged(final boolean newActive) {
        if (player instanceof ServerPlayer serverPlayer) {
            S2CPackets.sendAutocraftingMonitorActive(serverPlayer, newActive);
        } else {
            this.active = newActive;
            loadCurrentTask();
        }
    }

    boolean isActive() {
        return active;
    }
}
