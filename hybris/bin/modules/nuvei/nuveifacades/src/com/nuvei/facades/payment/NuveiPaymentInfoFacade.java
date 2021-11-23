package com.nuvei.facades.payment;

import com.nuvei.facades.beans.NuveiSDKResponseData;

/**
 * Handles payment info facade logic
 */
public interface NuveiPaymentInfoFacade {

    /**
     * Adds the payment info to the current cart
     *
     * @param checkoutSDKResponse the checkoutSDK response data
     */
    void addPaymentInfoToCart(final NuveiSDKResponseData checkoutSDKResponse);

}
