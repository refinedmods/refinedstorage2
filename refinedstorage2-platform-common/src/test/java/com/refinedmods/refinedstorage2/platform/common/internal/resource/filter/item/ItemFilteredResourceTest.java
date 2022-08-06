package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item;

import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

@SetupMinecraft
class ItemFilteredResourceTest {
    @Test
    void testEqualsHashcode() {
        EqualsVerifier.forClass(ItemFilteredResource.class)
            .withPrefabValues(Item.class, Items.DIRT, Items.GLASS)
            .verify();
    }
}
