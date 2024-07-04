package com.refinedmods.refinedstorage.platform.api.security;

import com.refinedmods.refinedstorage.api.network.security.SecurityActor;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

/**
 * Represents an item that can contain a {@link SecurityPolicy}. Typically, a Security Card.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.5")
public interface SecurityPolicyContainerItem {
    boolean isValid(ItemStack stack);

    Optional<SecurityActor> getActor(ItemStack stack);

    /**
     * Returns a {@link SecurityPolicy} containing all the permissions that this {@link SecurityPolicyContainerItem}
     * permits.
     * It is important to have all the relevant permissions in the {@link SecurityPolicy},
     * even the ones that are "allowed by default" via {@link PlatformPermission#isAllowedByDefault()}.
     * If not, even a permission that is allowed by default will not be allowed.
     *
     * @param stack the stack
     * @return the policy, if present
     */
    Optional<SecurityPolicy> getPolicy(ItemStack stack);

    long getEnergyUsage();
}
