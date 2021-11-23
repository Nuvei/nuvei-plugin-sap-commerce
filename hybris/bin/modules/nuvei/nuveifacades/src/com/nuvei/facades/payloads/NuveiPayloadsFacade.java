package com.nuvei.facades.payloads;

/**
 * Facade to handle operations with the API Calls payloads
 */
public interface NuveiPayloadsFacade {

    /**
     * Sets a request payload into the given session cart
     *
     * @param request The request to add
     */
    void setPaymentRequestPayload(String request);

    /**
     * Sets a response payload into the session cart
     *
     * @param response The response to add
     */
    void setPaymentResponsePayload(String response);

}
