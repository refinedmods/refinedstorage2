package com.refinedmods.refinedstorage.common.grid.view.pin;

import java.util.List;

public interface PinRepository {
    void saveAll(List<Pin> pins);

    List<Pin> loadAll();
}
