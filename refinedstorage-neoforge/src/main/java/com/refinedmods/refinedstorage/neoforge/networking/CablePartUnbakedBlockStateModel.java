package com.refinedmods.refinedstorage.neoforge.networking;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class CablePartUnbakedBlockStateModel implements CustomUnbakedBlockStateModel {
    public static final MapCodec<CablePartUnbakedBlockStateModel> MODEL_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            DyeColor.CODEC.fieldOf("color").forGetter(model -> model.color),
            Identifier.CODEC.fieldOf("part_model").forGetter(model -> model.partModel)
        ).apply(instance, CablePartUnbakedBlockStateModel::new));

    private final DyeColor color;
    private final Identifier partModel;
    private final Identifier coreModel;
    private final Identifier extensionModel;

    public CablePartUnbakedBlockStateModel(final DyeColor color, final Identifier partModel) {
        this.color = color;
        this.partModel = partModel;
        this.coreModel = createIdentifier("block/cable/core/" + color.getName());
        this.extensionModel = createIdentifier("block/cable/extension/" + color.getName());
    }

    @Override
    public void resolveDependencies(final Resolver resolver) {
        resolver.markDependency(coreModel);
        resolver.markDependency(extensionModel);
        resolver.markDependency(partModel);
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return MODEL_CODEC;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockStateModel bake(final ModelBaker modelBaker) {
        final ModelDebugName debugName = getClass()::toString;
        final Material.Baked particleMaterial = modelBaker.materials().get(
            new Material(createIdentifier("block/cable/" + color.getName())),
            debugName
        );
        return new CableBlockStateModel(particleMaterial, modelBaker, color, new SimpleCablePart(partModel));
    }
}
