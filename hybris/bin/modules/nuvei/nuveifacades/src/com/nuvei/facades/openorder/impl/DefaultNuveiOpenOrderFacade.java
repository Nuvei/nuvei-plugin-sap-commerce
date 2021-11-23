package com.nuvei.facades.openorder.impl;

import com.nuvei.facades.openorder.NuveiOpenOrderFacade;
import com.nuvei.services.openorder.NuveiOpenOrderService;
import com.safecharge.exception.SafechargeException;

public class DefaultNuveiOpenOrderFacade implements NuveiOpenOrderFacade {

    protected final NuveiOpenOrderService nuveiOpenOrderService;

    /**
     * Default constructor for {@link DefaultNuveiOpenOrderFacade}
     *
     * @param nuveiOpenOrderService injected
     */
    public DefaultNuveiOpenOrderFacade(final NuveiOpenOrderService nuveiOpenOrderService) {
        this.nuveiOpenOrderService = nuveiOpenOrderService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String requestOpenOrder() throws SafechargeException {
        return nuveiOpenOrderService.requestOpenOrder();
    }
}
