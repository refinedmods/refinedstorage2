package com.refinedmods.refinedstorage.api.network.impl.security;

import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.network.security.SecurityActor;
import com.refinedmods.refinedstorage.api.network.security.SecurityDecision;
import com.refinedmods.refinedstorage.api.network.security.SecurityDecisionProvider;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class SecurityDecisionProviderImpl implements SecurityDecisionProvider {
    private final Map<SecurityActor, SecurityPolicy> policyByActor = new HashMap<>();
    @Nullable
    private SecurityPolicy defaultPolicy;

    public SecurityDecisionProviderImpl setPolicy(final SecurityActor actor, final SecurityPolicy policy) {
        policyByActor.put(actor, policy);
        return this;
    }

    public SecurityDecisionProviderImpl setDefaultPolicy(@Nullable final SecurityPolicy policy) {
        this.defaultPolicy = policy;
        return this;
    }

    public void clearPolicies() {
        policyByActor.clear();
    }

    @Override
    public SecurityDecision isAllowed(final Permission permission, final SecurityActor actor) {
        final SecurityPolicy policy = policyByActor.get(actor);
        if (policy == null) {
            return SecurityDecision.PASS;
        }
        return allowOrDeny(policy.isAllowed(permission));
    }

    @Override
    public SecurityDecision isAllowed(final Permission permission) {
        if (defaultPolicy == null) {
            return SecurityDecision.PASS;
        }
        return allowOrDeny(defaultPolicy.isAllowed(permission));
    }

    private static SecurityDecision allowOrDeny(final boolean allowed) {
        return allowed ? SecurityDecision.ALLOW : SecurityDecision.DENY;
    }
}
