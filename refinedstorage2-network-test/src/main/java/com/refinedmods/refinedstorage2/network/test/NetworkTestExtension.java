package com.refinedmods.refinedstorage2.network.test;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.NetworkImpl;
import com.refinedmods.refinedstorage2.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.nodefactory.NetworkNodeFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class NetworkTestExtension implements BeforeEachCallback, ParameterResolver {
    private final Map<String, Network> networkMap = new HashMap<>();
    private final Map<Class<? extends NetworkNode>, NetworkNodeFactory<?>> networkNodeFactories = new HashMap<>();

    @Override
    public void beforeEach(final ExtensionContext extensionContext) {
        extensionContext
            .getTestInstances()
            .ifPresent(testInstances -> testInstances.getAllInstances().forEach(this::processTestInstance));
    }

    private void processTestInstance(final Object testInstance) {
        registerNetworkNodes(testInstance);
        setupNetworks(testInstance);
        injectNetworks(testInstance);
        addNetworkNodes(testInstance);
    }

    private void registerNetworkNodes(final Object testInstance) {
        for (final RegisterNetworkNode annotation : getAnnotations(testInstance, RegisterNetworkNode.class)) {
            try {
                final NetworkNodeFactory<?> factory = annotation.value().getDeclaredConstructor().newInstance();
                networkNodeFactories.put(annotation.clazz(), factory);
            } catch (InstantiationException
                     | IllegalAccessException
                     | InvocationTargetException
                     | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setupNetworks(final Object testInstance) {
        for (final SetupNetwork annotation : getAnnotations(testInstance, SetupNetwork.class)) {
            final Network network = new NetworkImpl(NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY);
            setupNetworkEnergy(annotation.energyCapacity(), annotation.energyStored(), network);
            networkMap.put(annotation.id(), network);
        }
    }

    private <A extends Annotation> List<A> getAnnotations(final Object testInstance, final Class<A> annotationType) {
        final List<A> annotations = new ArrayList<>();
        collectAnnotations(annotations, testInstance.getClass(), annotationType);
        return annotations;
    }

    private <A extends Annotation> void collectAnnotations(final List<A> annotations,
                                                           final Class<?> clazz,
                                                           final Class<A> annotationType) {
        // collect annotations in other annotations (for @NetworkTest)
        for (final Annotation annotation : clazz.getAnnotations()) {
            annotations.addAll(List.of(annotation.annotationType().getAnnotationsByType(annotationType)));
        }
        // collect annotations on class
        annotations.addAll(List.of(clazz.getAnnotationsByType(annotationType)));
        if (clazz.getSuperclass() != null) {
            collectAnnotations(annotations, clazz.getSuperclass(), annotationType);
        }
    }

    private void setupNetworkEnergy(final long capacity, final long stored, final Network network) {
        final EnergyNetworkComponent component = network.getComponent(EnergyNetworkComponent.class);
        final EnergyStorage storage = new EnergyStorageImpl(capacity);
        storage.receive(stored, Action.EXECUTE);
        final ControllerNetworkNode controller = new ControllerNetworkNode();
        controller.setEnergyStorage(storage);
        controller.setActive(true);
        component.onContainerAdded(() -> controller);
    }

    private void injectNetworks(final Object testInstance) {
        for (final Field field : getFields(testInstance)) {
            final InjectNetwork annotation = field.getAnnotation(InjectNetwork.class);
            if (annotation != null) {
                final Network network = networkMap.get(annotation.value());
                setField(testInstance, field, network);
            }
        }
    }

    private void addNetworkNodes(final Object testInstance) {
        for (final Field field : getFields(testInstance)) {
            addNetworkNode(testInstance, field);
        }
    }

    private List<Field> getFields(final Object testInstance) {
        final List<Field> fields = new ArrayList<>();
        collectFields(fields, testInstance.getClass());
        return fields;
    }

    private void collectFields(final List<Field> fields, final Class<?> clazz) {
        fields.addAll(List.of(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null) {
            collectFields(fields, clazz.getSuperclass());
        }
    }

    private void addNetworkNode(final Object testInstance, final Field field) {
        final AddNetworkNode annotation = field.getAnnotation(AddNetworkNode.class);
        if (annotation != null) {
            final Class<?> type = field.getType();
            final Map<String, Object> properties = getProperties(annotation.properties());
            final NetworkNode resolvedNode = networkNodeFactories.get(type).create(
                annotation,
                properties
            );
            final Network network = networkMap.get(annotation.networkId());
            registerNetworkNode(testInstance, field, resolvedNode, network);
        }
    }

    private Map<String, Object> getProperties(final AddNetworkNode.Property[] properties) {
        final Map<String, Object> result = new HashMap<>();
        for (final AddNetworkNode.Property property : properties) {
            result.put(property.key(), property.longValue() == -1
                ? property.boolValue()
                : property.longValue());
        }
        return result;
    }

    private void registerNetworkNode(final Object testInstance,
                                     final Field field,
                                     final NetworkNode networkNode,
                                     @Nullable final Network network) {
        networkNode.setNetwork(network);
        if (network != null) {
            network.addContainer(() -> networkNode);
        }
        setField(testInstance, field, networkNode);
    }

    private void setField(final Object instance, final Field field, final Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(InjectNetworkStorageChannel.class)
            || parameterContext.isAnnotated(InjectNetworkEnergyComponent.class)
            || parameterContext.isAnnotated(InjectNetwork.class);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext,
                                   final ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext
            .findAnnotation(InjectNetworkStorageChannel.class)
            .map(annotation -> (Object) getNetworkStorageChannel(annotation.networkId()))
            .or(() -> parameterContext
                .findAnnotation(InjectNetworkEnergyComponent.class)
                .map(annotation -> (Object) getNetworkEnergy(annotation.networkId())))
            .or(() -> parameterContext
                .findAnnotation(InjectNetwork.class)
                .map(annotation -> networkMap.get(annotation.value())))
            .orElseThrow();
    }

    private StorageChannel getNetworkStorageChannel(final String networkId) {
        return networkMap
            .get(networkId)
            .getComponent(StorageNetworkComponent.class)
            .getStorageChannel(NetworkTestFixtures.STORAGE_CHANNEL_TYPE);
    }

    private EnergyNetworkComponent getNetworkEnergy(final String networkId) {
        return networkMap
            .get(networkId)
            .getComponent(EnergyNetworkComponent.class);
    }
}
