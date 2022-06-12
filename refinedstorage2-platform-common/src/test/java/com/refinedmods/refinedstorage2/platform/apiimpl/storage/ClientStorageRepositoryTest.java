package com.refinedmods.refinedstorage2.platform.apiimpl.storage;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class ClientStorageRepositoryTest {

    List<UUID> uuidsRequested;
    ClientStorageRepository sut;

    @BeforeEach
    void setup() {
        uuidsRequested = new ArrayList<>();
        sut = new ClientStorageRepository(uuidsRequested::add);
    }

    @Test
    void Test_sending_request() {
        // Arrange
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        // Act
        StorageInfo aInfo = sut.getInfo(a);
        StorageInfo bInfo = sut.getInfo(b);

        // Assert
        assertThat(uuidsRequested).containsExactly(a);
        assertThat(aInfo).usingRecursiveComparison().isEqualTo(new StorageInfo(0, 0));
        assertThat(bInfo).usingRecursiveComparison().isEqualTo(new StorageInfo(0, 0));
    }

    @Test
    void Test_retrieving_storage_info() {
        // Arrange
        UUID a = UUID.randomUUID();
        sut.setInfo(a, 10, 100);

        UUID b = UUID.randomUUID();

        // Act
        StorageInfo aInfo = sut.getInfo(a);
        StorageInfo bInfo = sut.getInfo(b);

        // Assert
        assertThat(uuidsRequested).containsExactly(a);
        assertThat(aInfo).usingRecursiveComparison().isEqualTo(new StorageInfo(10, 100));
        assertThat(bInfo).usingRecursiveComparison().isEqualTo(new StorageInfo(0, 0));
    }
}
