package com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage;

import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.impl.storage.AbstractStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;

import java.util.function.LongSupplier;
import javax.annotation.Nullable;

public class ExternalStorageNetworkNode extends AbstractStorageNetworkNode implements StorageProvider {
    private final long energyUsage;
    private final ExposedExternalStorage storage;
    @Nullable
    private ExternalStorage externalStorage;

    public ExternalStorageNetworkNode(final long energyUsage, final LongSupplier clock) {
        this.energyUsage = energyUsage;
        this.storage = new ExposedExternalStorage(this, clock);
    }

    public void setTrackingRepository(final TrackedStorageRepository trackingRepository) {
        storage.setTrackingRepository(trackingRepository);
    }

    public void initialize(final ExternalStorageProviderFactory factory) {
        storage.tryClearDelegate();
        factory.create().ifPresent(provider -> {
            this.externalStorage = new ExternalStorage(provider, storage);
            if (isActive()) {
                setVisible(true);
            }
        });
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        setVisible(newActive);
    }

    public boolean detectChanges() {
        return storage.detectChanges();
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    private void setVisible(final boolean visible) {
        if (visible) {
            if (externalStorage == null) {
                return;
            }
            storage.setDelegate(externalStorage);
        } else {
            storage.tryClearDelegate();
        }
    }
}
