package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage.platform.api.storage.StorageRepository;

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

    private final Map<UUID, StorageInfo> info = new HashMap<>();
    private final RateLimiter rateLimiter = RateLimiter.create(2);
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

    public void setInfo(final UUID id, final long stored, final long capacity) {
        info.put(id, new StorageInfo(stored, capacity));
    }

    @Override
    public StorageInfo getInfo(final UUID id) {
        trySendRequestPacket(id);
        return info.getOrDefault(id, StorageInfo.UNKNOWN);
    }

    private void trySendRequestPacket(final UUID id) {
        if (!rateLimiter.tryAcquire()) {
            return;
        }
        LOGGER.debug("Sending request info packet for {}", id);
        storageInfoRequestAcceptor.accept(id);
    }

    @Override
    public void markAsChanged() {
        throw new UnsupportedOperationException();
    }
}
