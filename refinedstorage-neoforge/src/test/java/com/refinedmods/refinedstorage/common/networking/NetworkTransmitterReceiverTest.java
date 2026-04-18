package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_ITEMS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.checkBlockEntityActiveness;
import static com.refinedmods.refinedstorage.common.GameTestUtil.extract;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.networking.NetworkTransmitterReceiverTestPlots.checkNetworkTransmitterState;
import static com.refinedmods.refinedstorage.common.networking.NetworkTransmitterReceiverTestPlots.preparePlot;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.STONE;
import static net.minecraft.world.level.material.Fluids.WATER;

public final class NetworkTransmitterReceiverTest {
    private NetworkTransmitterReceiverTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldTransmitAndReceiveNetworkInSameDimension(final GameTestHelper helper) {
        preparePlot(helper, (transmitter, pos, receiverPos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount());
            }));

            final ItemStack networkCard = new ItemStack(MOD_ITEMS.getNetworkCard());
            networkCard.set(DataComponents.INSTANCE.getNetworkLocation(),
                GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(receiverPos)));

            // Act
            transmitter.getNetworkCards().setItem(0, networkCard);

            // Assert
            sequence
                .thenWaitUntil(() -> checkNetworkTransmitterState(helper, pos, NetworkTransmitterState.ACTIVE))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, receiverPos, true, NetworkReceiverBlock.ACTIVE))
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
                ))
                .thenExecute(storageContainsExactly(
                    helper,
                    receiverPos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, DIRT, 10);
                    extract(helper, network, STONE, 5);
                }))
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 10),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
                ))
                .thenExecute(storageContainsExactly(
                    helper,
                    receiverPos,
                    new ResourceAmount(asResource(STONE), 10),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
                ))
                .thenExecute(networkIsAvailable(helper, receiverPos, network ->
                    extract(helper, network, WATER, Platform.INSTANCE.getBucketAmount())))
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 10)
                ))
                .thenExecute(storageContainsExactly(
                    helper,
                    receiverPos,
                    new ResourceAmount(asResource(STONE), 10)
                ))
                .thenSucceed();
        });
    }
}
