package com.refinedmods.refinedstorage2.api.network.extension;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.FakeNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistryImpl;

import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class NetworkTestExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
    private final FakeNetworkFactory fakeNetworkFactory = new FakeNetworkFactory();
    private final StorageChannelTypeRegistry storageChannelTypeRegistry = new StorageChannelTypeRegistryImpl();
    private final Map<String, Network> networkMap = new HashMap<>();

    public NetworkTestExtension() {
        storageChannelTypeRegistry.addType(StorageChannelTypes.FAKE);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        extensionContext.getTestInstances().ifPresent(testInstances -> testInstances.getAllInstances().forEach(this::processTestInstance));
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        networkMap.clear();
    }

    private void processTestInstance(Object testInstance) {
        setupNetworks(testInstance);
        injectNetworks(testInstance);
        addNetworkNodes(testInstance);
    }

    private void setupNetworks(Object testInstance) {
        SetupNetwork[] annotations = testInstance.getClass().getAnnotationsByType(SetupNetwork.class);
        for (SetupNetwork annotation : annotations) {
            Network network = fakeNetworkFactory.create();
            setupNetworkEnergy(annotation.energyCapacity(), annotation.energyStored(), network);
            networkMap.put(annotation.id(), network);
        }
    }

    private void setupNetworkEnergy(long capacity, long stored, Network network) {
        EnergyNetworkComponent component = network.getComponent(EnergyNetworkComponent.class);
        EnergyStorage storage = new EnergyStorageImpl(capacity);
        storage.receive(stored, Action.EXECUTE);
        ControllerNetworkNode controller = new ControllerNetworkNode();
        controller.setEnergyStorage(storage);
        component.onContainerAdded(() -> controller);
    }

    private void injectNetworks(Object testInstance) {
        Field[] fields = testInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            InjectNetwork annotation = field.getAnnotation(InjectNetwork.class);
            if (annotation != null) {
                Network network = networkMap.get(annotation.value());
                setField(testInstance, field, network);
            }
        }
    }

    private void addNetworkNodes(Object testInstance) {
        Field[] fields = testInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            tryAddSimpleNetworkNode(testInstance, field);
            tryAddDiskDrive(testInstance, field);
        }
    }

    private void tryAddDiskDrive(Object testInstance, Field field) {
        AddDiskDrive annotation = field.getAnnotation(AddDiskDrive.class);
        if (annotation != null) {
            NetworkNode resolvedNode = new DiskDriveNetworkNode(annotation.baseEnergyUsage(), annotation.energyUsagePerDisk(), storageChannelTypeRegistry);
            Network network = networkMap.get(annotation.networkId());
            registerNetworkNode(testInstance, field, resolvedNode, network);
        }
    }

    private void tryAddSimpleNetworkNode(Object testInstance, Field field) {
        AddNetworkNode annotation = field.getAnnotation(AddNetworkNode.class);
        if (annotation != null) {
            NetworkNode resolvedNode = resolveSimpleNetworkNode(field.getType(), annotation.energyUsage());
            Network network = networkMap.get(annotation.networkId());
            registerNetworkNode(testInstance, field, resolvedNode, network);
        }
    }

    private NetworkNode resolveSimpleNetworkNode(Class<?> type, long energyUsage) {
        if (type == StorageNetworkNode.class) {
            return new StorageNetworkNode<>(energyUsage, StorageChannelTypes.FAKE);
        } else if (type == FakeNetworkNode.class) {
            return new FakeNetworkNode(energyUsage);
        } else if (type == GridNetworkNode.class) {
            return new GridNetworkNode<>(energyUsage, StorageChannelTypes.FAKE);
        }
        throw new RuntimeException(type.getName());
    }

    private void registerNetworkNode(Object testInstance, Field field, NetworkNode networkNode, Network network) {
        networkNode.setNetwork(network);
        network.addContainer(() -> networkNode);
        setField(testInstance, field, networkNode);
    }

    private void setField(Object instance, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(InjectNetworkStorageChannel.class) ||
                parameterContext.isAnnotated(InjectNetworkEnergyComponent.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext
                .findAnnotation(InjectNetworkStorageChannel.class)
                .map(annotation -> (Object) getNetworkStorageChannel(annotation.networkId()))
                .or(() -> parameterContext.findAnnotation(InjectNetworkEnergyComponent.class).map(annotation -> (Object) getNetworkEnergy(annotation.networkId())))
                .orElseThrow();
    }

    private StorageChannel<String> getNetworkStorageChannel(String networkId) {
        return networkMap
                .get(networkId)
                .getComponent(StorageNetworkComponent.class)
                .getStorageChannel(StorageChannelTypes.FAKE);
    }

    private EnergyNetworkComponent getNetworkEnergy(String networkId) {
        return networkMap
                .get(networkId)
                .getComponent(EnergyNetworkComponent.class);
    }
}
