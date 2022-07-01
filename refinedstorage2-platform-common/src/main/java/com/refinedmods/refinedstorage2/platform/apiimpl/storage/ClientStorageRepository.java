package com.refinedmods.refinedstorage2.platform.apiimpl.storage;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientStorageRepository implements PlatformStorageRepository {
    private static final Logger LOGGER = LogManager.getLogger();

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
    public <T> Optional<Storage<T>> get(final UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void set(final UUID id, final Storage<T> storage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<Storage<T>> disassemble(final UUID id) {
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
