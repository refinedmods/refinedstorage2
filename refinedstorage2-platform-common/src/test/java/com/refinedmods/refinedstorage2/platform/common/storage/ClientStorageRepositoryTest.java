package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientStorageRepositoryTest {
    private List<UUID> uuidsRequested;
    private ClientStorageRepository sut;

    @BeforeEach
    void setup() {
        uuidsRequested = new ArrayList<>();
        sut = new ClientStorageRepository(uuidsRequested::add);
    }

    @Test
    void shouldSendRequestWhenRetrievingInfo() {
        // Arrange
        final UUID a = UUID.randomUUID();
        final UUID b = UUID.randomUUID();

        // Act
        final StorageInfo aInfo = sut.getInfo(a);
        final StorageInfo bInfo = sut.getInfo(b);

        // Assert
        assertThat(uuidsRequested).containsExactly(a);
        assertThat(aInfo).usingRecursiveComparison().isEqualTo(StorageInfo.UNKNOWN);
        assertThat(bInfo).usingRecursiveComparison().isEqualTo(StorageInfo.UNKNOWN);
    }

    @Test
    void shouldRetrieveInfo() {
        // Arrange
        final UUID a = UUID.randomUUID();
        sut.setInfo(a, 10, 100);

        final UUID b = UUID.randomUUID();

        // Act
        final StorageInfo aInfo = sut.getInfo(a);
        final StorageInfo bInfo = sut.getInfo(b);

        // Assert
        assertThat(uuidsRequested).containsExactly(a);
        assertThat(aInfo).usingRecursiveComparison().isEqualTo(new StorageInfo(10, 100));
        assertThat(bInfo).usingRecursiveComparison().isEqualTo(StorageInfo.UNKNOWN);
    }
}
