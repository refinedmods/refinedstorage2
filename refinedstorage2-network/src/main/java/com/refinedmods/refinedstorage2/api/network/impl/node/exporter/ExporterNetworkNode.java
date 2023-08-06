package com.refinedmods.refinedstorage2.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.task.Task;
import com.refinedmods.refinedstorage2.api.network.node.task.TaskExecutor;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class ExporterNetworkNode extends AbstractNetworkNode {
    private long energyUsage;
    private final Actor actor = new NetworkNodeActor(this);
    private final List<TaskImpl> tasks = new ArrayList<>();
    @Nullable
    private ExporterTransferStrategy transferStrategy;
    @Nullable
    private TaskExecutor<TaskContext> taskExecutor;

    public ExporterNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setTransferStrategy(@Nullable final ExporterTransferStrategy transferStrategy) {
        this.transferStrategy = transferStrategy;
    }

    public void setTaskExecutor(@Nullable final TaskExecutor<TaskContext> taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive() || taskExecutor == null) {
            return;
        }
        final TaskContext context = new TaskContext(network, actor);
        taskExecutor.execute(tasks, context);
    }

    public void setFilterTemplates(final List<Object> templates) {
        tasks.clear();
        tasks.addAll(templates.stream().map(TaskImpl::new).toList());
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public record TaskContext(Network network, Actor actor) {
    }

    class TaskImpl implements Task<TaskContext> {
        private final Object template;

        TaskImpl(final Object template) {
            this.template = template;
        }

        @Override
        public boolean run(final TaskContext context) {
            if (transferStrategy == null) {
                return false;
            }
            return transferStrategy.transfer(template, context.actor, context.network);
        }
    }
}
