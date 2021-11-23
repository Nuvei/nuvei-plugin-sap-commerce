package com.nuvei.services.enpoint;

import java.net.URI;

/**
 * Exposes methods to retrieve Nuvei endpoints depending on the environment configured: LIVE vs TEST
 */
public interface NuveiEndpointService {

    /**
     * Returns the URI corresponding to the purchase endpoint relative to the configured environment.
     * <p>
     * When the environment is configured for LIVE returns https://secure.safecharge.com/ppp/purchase.do
     * When the environment is configured for TEST returns https://ppp-test.safecharge.com/ppp/purchase.do
     *
     * @return URI to redirect the customer to on the Checkout Page integration
     */
    URI getRedirectEndpoint();
}
