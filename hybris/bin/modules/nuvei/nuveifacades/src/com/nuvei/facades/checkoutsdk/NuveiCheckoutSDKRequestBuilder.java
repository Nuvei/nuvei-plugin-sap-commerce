package com.nuvei.facades.checkoutsdk;

import com.nuvei.facades.beans.NuveiCheckoutSDKRequestData;

/**
 * Manages the creation of the {@link NuveiCheckoutSDKRequestData} objects
 */
public interface NuveiCheckoutSDKRequestBuilder {

    /**
     * Creates a {@link NuveiCheckoutSDKRequestData} object for the CheckoutSDK() call
     *
     * @param sessionToken The session token for the request
     * @return The {@link NuveiCheckoutSDKRequestData}
     */
    NuveiCheckoutSDKRequestData getCheckoutSDKRequestData(final String sessionToken);
}
