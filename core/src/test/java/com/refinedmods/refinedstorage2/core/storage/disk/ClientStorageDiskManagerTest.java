package com.refinedmods.refinedstorage2.core.storage.disk;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RefinedStorage2Test
class ClientStorageDiskManagerTest {
    @Test
    void Test_getting_storage_disk_info_with_a_simple_request_callback() {
        // Arrange
        UUID id = UUID.randomUUID();
        Consumer<UUID> requestCallback = mock(Consumer.class);

        ClientStorageDiskManager storageDiskManager = new ClientStorageDiskManager(requestCallback);

        // Act
        StorageDiskInfo returnedInfo = storageDiskManager.getInfo(id);

        // Arrange
        assertThat(returnedInfo).isSameAs(StorageDiskInfo.UNKNOWN);
        verify(requestCallback).accept(id);
    }
}
