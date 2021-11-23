package com.nuvei.services.payloads;

import de.hybris.platform.core.model.order.AbstractOrderModel;

/**
 * Interface to handle operations with the API Calls payloads
 */
public interface NuveiPayloadsService {

    /**
     * Sets a request payload into the given order
     *
     * @param request       The request to add
     * @param abstractOrder The abstractOrder that will contain the request
     */
    void setPaymentRequestPayload(String request, AbstractOrderModel abstractOrder);

    /**
     * @param response The response to add
     * @param abstractOrder The abstractOrder that will contain the request
     */
    void setPaymentResponsePayload(String response, AbstractOrderModel abstractOrder);
}
