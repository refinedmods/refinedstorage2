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
    private final List<ExporterTask> tasks = new ArrayList<>();
    @Nullable
    private ExporterTransferStrategy transferStrategy;
    @Nullable
    private TaskExecutor<ExporterTaskContext> taskExecutor;

    public ExporterNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setTransferStrategy(@Nullable final ExporterTransferStrategy transferStrategy) {
        this.transferStrategy = transferStrategy;
    }

    public void setTaskExecutor(@Nullable final TaskExecutor<ExporterTaskContext> taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive() || taskExecutor == null) {
            return;
        }
        final ExporterTaskContext context = new ExporterTaskContext(network, actor);
        taskExecutor.execute(tasks, context);
    }

    public void setFilterTemplates(final List<Object> templates) {
        tasks.clear();
        tasks.addAll(templates.stream().map(ExporterTask::new).toList());
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public record ExporterTaskContext(Network network, Actor actor) {
    }

    class ExporterTask implements Task<ExporterTaskContext> {
        private final Object template;

        ExporterTask(final Object template) {
            this.template = template;
        }

        @Override
        public boolean run(final ExporterTaskContext context) {
            if (transferStrategy == null) {
                return false;
            }
            return transferStrategy.transfer(template, context.actor, context.network);
        }
    }
}
