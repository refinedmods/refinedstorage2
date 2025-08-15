package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageInfo;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientStorageRepository implements StorageRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStorageRepository.class);

    private final Map<UUID, ClientStorageInfo> info = new HashMap<>();
    private final Consumer<UUID> storageInfoRequestAcceptor;

    public ClientStorageRepository(final Consumer<UUID> storageInfoRequestAcceptor) {
        this.storageInfoRequestAcceptor = CoreValidations.validateNotNull(
            storageInfoRequestAcceptor,
            "Storage info request acceptor cannot be null"
        );
    }

    @Override
    public Optional<SerializableStorage> get(final UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(final UUID id, final SerializableStorage storage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SerializableStorage> removeIfEmpty(final UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(final UUID id) {
        throw new UnsupportedOperationException();
    }

    private ClientStorageInfo getOrCreateClientInfo(final UUID id) {
        return info.computeIfAbsent(id, k -> new ClientStorageInfo());
    }

    public void updateInfo(final UUID id, final long stored, final long capacity) {
        getOrCreateClientInfo(id).info = new StorageInfo(stored, capacity);
    }

    @Override
    public StorageInfo getInfo(final UUID id) {
        final ClientStorageInfo clientInfo = getOrCreateClientInfo(id);
        trySendRequestPacket(id, clientInfo.rateLimiter);
        return clientInfo.info;
    }

    private void trySendRequestPacket(final UUID id, final RateLimiter rateLimiter) {
        if (!rateLimiter.tryAcquire()) {
            return;
        }
        LOGGER.debug("Sending storage info request packet for {}", id);
        storageInfoRequestAcceptor.accept(id);
    }

    @Override
    public void markAsChanged() {
        throw new UnsupportedOperationException();
    }

    private static class ClientStorageInfo {
        private final RateLimiter rateLimiter = RateLimiter.create(1);
        private StorageInfo info = StorageInfo.UNKNOWN;
    }
}
