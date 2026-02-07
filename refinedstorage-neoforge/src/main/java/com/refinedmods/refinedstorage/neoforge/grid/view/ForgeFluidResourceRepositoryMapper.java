package com.refinedmods.refinedstorage.neoforge.grid.view;

import com.refinedmods.refinedstorage.common.grid.view.AbstractFluidGridResourceRepositoryMapper;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForgeFluidResourceRepositoryMapper extends AbstractFluidGridResourceRepositoryMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForgeFluidResourceRepositoryMapper.class);

    private final Map<FluidResource, FluidStack> stackCache = new HashMap<>();

    @Override
    protected String getTooltip(final FluidResource resource) {
        try {
            return toStack(resource)
                .getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.ADVANCED)
                .stream()
                .map(Component::getString)
                .collect(Collectors.joining("\n"));
        } catch (final Throwable t) {
            LOGGER.warn("Failed to get tooltip for fluid {}", resource, t);
            return "";
        }
    }

    @Override
    protected String getModName(final String modId) {
        return ModList.get().getModContainerById(modId)
            .map(container -> container.getModInfo().getDisplayName())
            .orElse("");
    }

    @Override
    protected String getName(final FluidResource fluidResource) {
        return toStack(fluidResource).getHoverName().getString();
    }

    private FluidStack toStack(final FluidResource fluidResource) {
        return stackCache.computeIfAbsent(fluidResource, r -> new FluidStack(
            BuiltInRegistries.FLUID.wrapAsHolder(r.fluid()),
            FluidType.BUCKET_VOLUME,
            r.components()
        ));
    }
}
