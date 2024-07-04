package com.refinedmods.refinedstorage.api.network.security;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.5")
public enum SecurityDecision {
    /**
     * Allow the operation.
     */
    ALLOW,
    /**
     * Deny the operation.
     */
    DENY,
    /**
     * Pass the decision to the next {@link SecurityDecisionProvider}.
     */
    PASS
}
