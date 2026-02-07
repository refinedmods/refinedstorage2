package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.ParentContainer;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyProvider;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.network.security.SecurityActor;
import com.refinedmods.refinedstorage.api.network.security.SecurityDecision;
import com.refinedmods.refinedstorage.api.network.security.SecurityDecisionProvider;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageProvider;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.api.storage.Storage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

public class RelayOutputNetworkNode extends AbstractNetworkNode
    implements EnergyProvider, SecurityDecisionProvider, StorageProvider, PatternProvider {
    private final long energyUsage;
    private final RelayOutputStorage storage = new RelayOutputStorage();
    private final RelayOutputPatternProvider patternProvider = new RelayOutputPatternProvider(this);

    @Nullable
    private EnergyNetworkComponent energyDelegate;
    @Nullable
    private SecurityNetworkComponent securityDelegate;

    public RelayOutputNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    void setEnergyDelegate(@Nullable final EnergyNetworkComponent energyDelegate) {
        this.energyDelegate = energyDelegate;
    }

    void setSecurityDelegate(@Nullable final SecurityNetworkComponent securityDelegate) {
        this.securityDelegate = securityDelegate;
    }

    void setStorageDelegate(@Nullable final StorageNetworkComponent storageDelegate) {
        this.storage.setDelegate(storageDelegate);
    }

    void setAutocraftingDelegate(@Nullable final AutocraftingNetworkComponent autocraftingDelegate) {
        this.patternProvider.setDelegate(autocraftingDelegate);
    }

    void setAccessMode(final AccessMode accessMode) {
        this.storage.setAccessMode(accessMode);
    }

    void setInsertPriority(final int insertPriority) {
        this.storage.setInsertPriority(insertPriority);
        if (network != null) {
            network.getComponent(StorageNetworkComponent.class).sortSources();
        }
    }

    void setExtractPriority(final int extractPriority) {
        this.storage.setExtractPriority(extractPriority);
        if (network != null) {
            network.getComponent(StorageNetworkComponent.class).sortSources();
        }
    }

    void setFilters(final Set<ResourceKey> filters) {
        this.storage.setFilters(filters);
        this.patternProvider.setFilters(filters);
    }

    void setFilterMode(final FilterMode filterMode) {
        this.storage.setFilterMode(filterMode);
        this.patternProvider.setFilterMode(filterMode);
    }

    void setFilterNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        this.storage.setFilterNormalizer(normalizer);
        this.patternProvider.setFilterNormalizer(normalizer);
    }

    @Override
    public long getEnergyUsage() {
        if (energyDelegate != null
            || securityDelegate != null
            || storage.hasDelegate()
            || patternProvider.hasDelegate()) {
            return energyUsage;
        }
        return 0;
    }

    @Override
    public void doWork() {
        super.doWork();
        patternProvider.doWork();
    }

    public void setStepBehavior(final StepBehavior stepBehavior) {
        patternProvider.setStepBehavior(stepBehavior);
    }

    @Override
    public long getStored() {
        return energyDelegate == null || energyDelegate.contains(energyDelegate) ? 0 : energyDelegate.getStored();
    }

    @Override
    public long getCapacity() {
        return energyDelegate == null || energyDelegate.contains(energyDelegate) ? 0 : energyDelegate.getCapacity();
    }

    @Override
    public long extract(final long amount) {
        return energyDelegate == null || energyDelegate.contains(energyDelegate) ? 0 : energyDelegate.extract(amount);
    }

    @Override
    public boolean contains(final EnergyProvider energyProvider) {
        return energyProvider == energyDelegate
            || (energyDelegate != null && energyDelegate.contains(energyProvider));
    }

    @Override
    public boolean contains(final SecurityNetworkComponent securityComponent) {
        return securityComponent == securityDelegate
            || (securityDelegate != null && securityDelegate.contains(securityComponent));
    }

    @Override
    public boolean contains(final AutocraftingNetworkComponent component) {
        return patternProvider.contains(component);
    }

    public List<Task> getTasks() {
        return patternProvider.getTasks();
    }

    @Override
    public void addTask(final Task task) {
        patternProvider.addTask(task);
    }

    @Override
    public void cancelTask(final TaskId taskId) {
        patternProvider.cancelTask(taskId);
    }

    @Override
    public List<TaskStatus> getTaskStatuses() {
        return patternProvider.getTaskStatuses();
    }

    @Override
    public long getAmount(final ResourceKey resource) {
        return patternProvider.getAmount(resource);
    }

    @Override
    public void receivedExternalIteration() {
        patternProvider.receivedExternalIteration();
    }

    @Override
    public SecurityDecision isAllowed(final Permission permission, final SecurityActor actor) {
        if (securityDelegate == null || securityDelegate.contains(securityDelegate)) {
            return SecurityDecision.PASS;
        }
        return securityDelegate.isAllowed(permission, actor) ? SecurityDecision.ALLOW : SecurityDecision.DENY;
    }

    @Override
    public boolean isProviderActive() {
        return isActive() && securityDelegate != null;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public void onAddedIntoContainer(final ParentContainer parentContainer) {
        patternProvider.onAddedIntoContainer(parentContainer);
    }

    @Override
    public void onRemovedFromContainer(final ParentContainer parentContainer) {
        patternProvider.onRemovedFromContainer(parentContainer);
    }

    @Override
    public Result accept(final Pattern pattern, final Collection<ResourceAmount> resources, final Action action) {
        return patternProvider.accept(pattern, resources, action);
    }

    @Override
    public void setNetwork(@Nullable final Network network) {
        if (this.network != null) {
            patternProvider.detachAll(this.network);
        }
        super.setNetwork(network);
        if (network != null) {
            patternProvider.attachAll(network);
        }
    }
}
