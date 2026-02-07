package com.refinedmods.refinedstorage.neoforge.storage.diskdrive;

import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;

import java.util.ArrayList;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.storage.AbstractDiskContainerBlockEntity.getDisks;
import static com.refinedmods.refinedstorage.neoforge.storage.diskdrive.DiskDriveRenderingProperties.DISKS;

public class DiskDriveItemModel implements ItemModel {
    private final List<BakedQuad> baseQuads;
    private final List<BakedQuad> ledQuads;
    private final Map<Item, List<BakedQuad>> diskQuadsByItem;
    private final Map<ItemDisplayContext, List<ItemTransform>> diskTransforms;
    private final ModelRenderProperties modelRenderProperties;

    public DiskDriveItemModel(final List<BakedQuad> baseQuads,
                              final Map<Item, List<BakedQuad>> diskQuadsByItem,
                              final List<BakedQuad> ledQuads,
                              final ModelRenderProperties modelRenderProperties) {
        this.baseQuads = baseQuads;
        this.diskQuadsByItem = diskQuadsByItem;
        this.ledQuads = ledQuads;
        this.modelRenderProperties = modelRenderProperties;
        this.diskTransforms = Arrays.stream(ItemDisplayContext.values()).collect(Collectors.toMap(
            context -> context,
            context -> {
                final ItemTransform baseTransform = modelRenderProperties.transforms().getTransform(context);
                final List<ItemTransform> transforms = new ArrayList<>(DISKS);
                for (int i = 0; i < DISKS; ++i) {
                    final Matrix4f offsetTransform = getOffsetTransform(baseTransform);
                    transforms.add(getDiskTransform(baseTransform, offsetTransform, i));
                }
                return transforms;
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
        layer.prepareQuadList().addAll(baseQuads);
        final TypedEntityData<BlockEntityType<?>> customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData == null) {
            return;
        }
        final List<@Nullable Item> disks = getDisks(customData.copyTagWithoutId(), DISKS);
        renderState.appendModelIdentityElement(disks);
        for (int x = 0; x < 2; ++x) {
            for (int y = 0; y < 4; ++y) {
                final int idx = x + (y * 2);
                final Item diskItem = disks.get(idx);
                if (diskItem != null) {
                    addDisk(renderState, itemDisplayContext, diskItem, idx);
                }
            }
        }
    }

    private void addDisk(final ItemStackRenderState renderState, final ItemDisplayContext itemDisplayContext,
                         final Item diskItem, final int idx) {
        final List<BakedQuad> diskQuads = diskQuadsByItem.get(diskItem);
        if (diskQuads == null) {
            return;
        }
        final ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        modelRenderProperties.applyToLayer(layer, itemDisplayContext);
        layer.setItemTransform(diskTransforms.get(itemDisplayContext).get(idx));
        layer.prepareQuadList().addAll(diskQuads);
        layer.prepareQuadList().addAll(ledQuads);
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
                                                  final Matrix4f offsetTransform,
                                                  final int idx) {
        final Vector3f translation = offsetTransform
            .transformDirection(new Vector3f(DiskDriveRenderingProperties.TRANSLATIONS[idx]), new Vector3f())
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
            final ResolvedModel baseModel = baker.getModel(DiskDriveRenderingProperties.BASE_MODEL);
            final List<BakedQuad> baseQuads = baseModel
                .bakeTopGeometry(baseModel.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
            final List<BakedQuad> ledQuads = baker.getModel(DiskDriveRenderingProperties.INACTIVE_LED_MODEL)
                .bakeTopGeometry(baseModel.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
            final Map<Item, List<BakedQuad>> diskQuads = RefinedStorageClientApi.INSTANCE.getDiskModelsByItem()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        final ResolvedModel diskModel = baker.getModel(entry.getValue());
                        return diskModel.bakeTopGeometry(diskModel.getTopTextureSlots(), baker,
                            BlockModelRotation.IDENTITY).getAll();
                    }));
            return new DiskDriveItemModel(baseQuads, diskQuads, ledQuads,
                ModelRenderProperties.fromResolvedModel(baker, baseModel, baseModel.getTopTextureSlots()));
        }

        @Override
        public void resolveDependencies(final Resolver resolver) {
            resolver.markDependency(DiskDriveRenderingProperties.BASE_MODEL);
            resolver.markDependency(DiskDriveRenderingProperties.INACTIVE_LED_MODEL);
            RefinedStorageClientApi.INSTANCE.getDiskModels().forEach(resolver::markDependency);
        }
    }
}
