package com.nuvei.facades.payloads.impl;


import com.nuvei.facades.payloads.NuveiPayloadsFacade;
import com.nuvei.services.payloads.NuveiPayloadsService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;

/**
 * {@inheritDoc}
 */
public class DefaultNuveiPayloadsFacade implements NuveiPayloadsFacade {

    protected final NuveiPayloadsService nuveiPayloadsService;
    protected final CartService cartService;

    /**
     * Default constructor for {@link DefaultNuveiPayloadsFacade}
     *
     * @param nuveiPayloadsService injected
     * @param cartService          injected
     */
    public DefaultNuveiPayloadsFacade(final NuveiPayloadsService nuveiPayloadsService,
                                      final CartService cartService) {
        this.nuveiPayloadsService = nuveiPayloadsService;
        this.cartService = cartService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPaymentRequestPayload(final String request) {
        if (cartService.hasSessionCart()) {
            final CartModel sessionCart = cartService.getSessionCart();
            nuveiPayloadsService.setPaymentRequestPayload(request, sessionCart);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPaymentResponsePayload(final String response) {
        if (cartService.hasSessionCart()) {
            final CartModel sessionCart = cartService.getSessionCart();
            nuveiPayloadsService.setPaymentResponsePayload(response, sessionCart);
        }
    }
}
