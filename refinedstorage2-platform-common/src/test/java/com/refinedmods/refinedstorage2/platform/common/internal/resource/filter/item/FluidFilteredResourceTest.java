package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item;

import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.fluid.FluidFilteredResource;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

@SetupMinecraft
class FluidFilteredResourceTest {
    @Test
    void testEqualsHashcode() {
        EqualsVerifier.forClass(FluidFilteredResource.class)
            .withPrefabValues(Fluid.class, Fluids.LAVA, Fluids.WATER)
            .verify();
    }
}
