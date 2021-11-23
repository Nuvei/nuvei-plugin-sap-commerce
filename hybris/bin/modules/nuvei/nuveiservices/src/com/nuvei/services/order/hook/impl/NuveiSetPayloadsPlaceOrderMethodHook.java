package com.nuvei.services.order.hook.impl;

import de.hybris.platform.commerceservices.order.hook.CommercePlaceOrderMethodHook;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commerceservices.service.data.CommerceOrderResult;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;


/**
 * Hook for updating the order with the payloads from the cart
 */
public class NuveiSetPayloadsPlaceOrderMethodHook implements CommercePlaceOrderMethodHook {

    protected final ModelService modelService;

    /**
     * Default constructor for {@link NuveiSetPayloadsPlaceOrderMethodHook}
     *
     * @param modelService
     */
    public NuveiSetPayloadsPlaceOrderMethodHook(final ModelService modelService) {
        this.modelService = modelService;
    }

    /**
     * Updates the order with the payloads from the cart
     *
     * @param parameter  CommerceCheckoutParameter that contains the cart
     * @param orderModel CommerceOrderResult that contains the order
     * @throws InvalidCartException
     */
    @Override
    public void afterPlaceOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult orderModel) throws InvalidCartException {
        final CartModel cart = parameter.getCart();
        final OrderModel order = orderModel.getOrder();

        ServicesUtil.validateParameterNotNullStandardMessage("order", order);
        ServicesUtil.validateParameterNotNullStandardMessage("cart", cart);

        order.setRequestsPayload(cart.getRequestsPayload());
        order.setResponsesPayload(cart.getResponsesPayload());

        modelService.save(order);
    }

    /**
     * Not implemented
     *
     * @param parameter The CommerceCheckoutParameter parameter
     * @throws InvalidCartException
     */
    @Override
    public void beforePlaceOrder(final CommerceCheckoutParameter parameter) throws InvalidCartException {
        // not implemented
    }

    /**
     * Not implemented
     *
     * @param parameter The CommerceCheckoutParameter parameter
     * @param result    The CommerceOrderResult result
     * @throws InvalidCartException
     */
    @Override
    public void beforeSubmitOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult result) throws InvalidCartException {
        // not implemented
    }
}
