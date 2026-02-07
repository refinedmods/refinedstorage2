package com.refinedmods.refinedstorage.neoforge.storage.portablegrid;

import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridBlockItem;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridBlockItemRenderInfo;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class PortableGridItemModel implements ItemModel {
    private static final Vector3f MOVE_TO_DISK_LOCATION = new Vector3f(0, -12 / 16F, 9 / 16F);

    private final List<BakedQuad> activeBaseQuads;
    private final List<BakedQuad> inactiveBaseQuads;
    private final Map<Item, List<BakedQuad>> diskQuadsByItem;
    private final Map<StorageState, List<BakedQuad>> ledQuadsByState;
    private final ModelRenderProperties modelRenderProperties;
    private final Map<ItemDisplayContext, ItemTransform> diskTransforms;

    public PortableGridItemModel(final List<BakedQuad> activeBaseQuads,
                                 final List<BakedQuad> inactiveBaseQuads,
                                 final Map<Item, List<BakedQuad>> diskQuadsByItem,
                                 final Map<StorageState, List<BakedQuad>> ledQuadsByState,
                                 final ModelRenderProperties modelRenderProperties) {
        this.activeBaseQuads = activeBaseQuads;
        this.inactiveBaseQuads = inactiveBaseQuads;
        this.diskQuadsByItem = diskQuadsByItem;
        this.ledQuadsByState = ledQuadsByState;
        this.modelRenderProperties = modelRenderProperties;
        this.diskTransforms = Arrays.stream(ItemDisplayContext.values()).collect(Collectors.toMap(
            context -> context,
            context -> {
                final ItemTransform baseTransform = modelRenderProperties.transforms().getTransform(context);
                final Matrix4f offsetTransform = getOffsetTransform(baseTransform);
                return getDiskTransform(baseTransform, offsetTransform);
            }
        ));
    }

    @Override
    public void update(final ItemStackRenderState renderState, final ItemStack stack,
                       final ItemModelResolver resolver, final ItemDisplayContext itemDisplayContext,
                       @Nullable final ClientLevel clientLevel, @Nullable final ItemOwner itemOwner, final int i) {
        renderState.appendModelIdentityElement(this);
        final ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        modelRenderProperties.applyToLayer(layer, itemDisplayContext);
        final PortableGridBlockItemRenderInfo renderInfo = PortableGridBlockItem.getRenderInfo(stack);
        renderState.appendModelIdentityElement(renderInfo);
        layer.prepareQuadList().addAll(renderInfo.active() ? activeBaseQuads : inactiveBaseQuads);
        addDisk(renderState, itemDisplayContext, renderInfo.disk());
    }

    private void addDisk(final ItemStackRenderState renderState, final ItemDisplayContext itemDisplayContext,
                         final Disk disk) {
        if (disk.state() == StorageState.NONE) {
            return;
        }
        final List<BakedQuad> diskQuads = diskQuadsByItem.get(disk.item());
        if (diskQuads == null) {
            return;
        }
        final ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        modelRenderProperties.applyToLayer(layer, itemDisplayContext);
        layer.setItemTransform(diskTransforms.get(itemDisplayContext));
        layer.prepareQuadList().addAll(diskQuads);
        layer.prepareQuadList().addAll(ledQuadsByState.get(disk.state()));
    }

    private static Matrix4f getOffsetTransform(final ItemTransform baseTransform) {
        final float toRad = (float) Math.PI / 180F;
        return new Matrix4f()
            .rotateXYZ(
                baseTransform.rotation().x() * toRad,
                baseTransform.rotation().y() * toRad,
                baseTransform.rotation().z() * toRad
            )
            .scale(baseTransform.scale().x(), baseTransform.scale().y(), baseTransform.scale().z())
            .rotateXYZ(
                baseTransform.rightRotation().x() * toRad,
                baseTransform.rightRotation().y() * toRad,
                baseTransform.rightRotation().z() * toRad
            );
    }

    private static ItemTransform getDiskTransform(final ItemTransform baseTransform,
                                                  final Matrix4f offsetTransform) {
        final Vector3f translation = offsetTransform
            .transformDirection(MOVE_TO_DISK_LOCATION, new Vector3f())
            .add(baseTransform.translation());
        return new ItemTransform(baseTransform.rotation(), translation, baseTransform.scale(),
            baseTransform.rightRotation());
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<ItemModel.Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public ItemModel bake(final BakingContext bakingContext, final Matrix4fc matrix4fc) {
            final ModelBaker baker = bakingContext.blockModelBaker();
            final ResolvedModel activeBaseModel = baker.getModel(PortableGridRenderingProperties.ACTIVE_MODEL);
            final List<BakedQuad> activeBaseQuads = activeBaseModel
                .bakeTopGeometry(activeBaseModel.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
            final ResolvedModel inactiveBaseModel = baker.getModel(PortableGridRenderingProperties.INACTIVE_MODEL);
            final List<BakedQuad> inactiveBaseQuads = inactiveBaseModel
                .bakeTopGeometry(inactiveBaseModel.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
            final Map<Item, List<BakedQuad>> diskQuads = RefinedStorageClientApi.INSTANCE.getDiskModelsByItem()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        final ResolvedModel diskModel = baker.getModel(entry.getValue());
                        return diskModel.bakeTopGeometry(diskModel.getTopTextureSlots(), baker,
                            ClientPlatformUtil.getRotation(OrientedDirection.WEST)).getAll();
                    }));
            final Map<StorageState, List<BakedQuad>> ledQuads = Arrays.stream(StorageState.values())
                .filter(s -> s != StorageState.NONE)
                .collect(Collectors.toMap(
                    state -> state,
                    state -> {
                        final ResolvedModel ledModel = baker.getModel(getLedModel(state));
                        return ledModel.bakeTopGeometry(ledModel.getTopTextureSlots(), baker,
                            ClientPlatformUtil.getRotation(OrientedDirection.WEST)).getAll();
                    }));
            return new PortableGridItemModel(activeBaseQuads, inactiveBaseQuads, diskQuads, ledQuads,
                ModelRenderProperties.fromResolvedModel(baker, activeBaseModel, activeBaseModel.getTopTextureSlots()));
        }

        private static Identifier getLedModel(final StorageState state) {
            return switch (state) {
                case NONE -> throw new IllegalArgumentException("Unexpected state: " + state);
                case INACTIVE -> PortableGridRenderingProperties.INACTIVE_LED_MODEL;
                case NORMAL -> PortableGridRenderingProperties.NORMAL_LED_MODEL;
                case NEAR_CAPACITY -> PortableGridRenderingProperties.NEAR_CAPACITY_LED_MODEL;
                case FULL -> PortableGridRenderingProperties.FULL_LED_MODEL;
            };
        }

        @Override
        public void resolveDependencies(final Resolver resolver) {
            resolver.markDependency(PortableGridRenderingProperties.ACTIVE_MODEL);
            resolver.markDependency(PortableGridRenderingProperties.INACTIVE_MODEL);
            resolver.markDependency(PortableGridRenderingProperties.INACTIVE_LED_MODEL);
            resolver.markDependency(PortableGridRenderingProperties.NORMAL_LED_MODEL);
            resolver.markDependency(PortableGridRenderingProperties.NEAR_CAPACITY_LED_MODEL);
            resolver.markDependency(PortableGridRenderingProperties.FULL_LED_MODEL);
            RefinedStorageClientApi.INSTANCE.getDiskModels().forEach(resolver::markDependency);
        }
    }
}
