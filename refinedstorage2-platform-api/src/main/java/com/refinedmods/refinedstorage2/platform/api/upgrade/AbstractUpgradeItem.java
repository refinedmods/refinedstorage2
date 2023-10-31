package com.refinedmods.refinedstorage2.platform.api.upgrade;

import java.util.Optional;
import java.util.Set;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.2")
public abstract class AbstractUpgradeItem extends Item implements UpgradeItem {
    private final UpgradeRegistry registry;

    protected AbstractUpgradeItem(final Properties properties, final UpgradeRegistry registry) {
        super(properties);
        this.registry = registry;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        final Set<UpgradeMapping> destinations = registry.getByUpgradeItem(this);
        if (destinations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new UpgradeDestinationTooltipComponent(destinations));
    }

    public record UpgradeDestinationTooltipComponent(Set<UpgradeMapping> destinations)
        implements TooltipComponent {
    }
}

