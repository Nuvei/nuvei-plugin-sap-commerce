package com.nuvei.services.service;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;

/**
 * Service to handle logic of custom nuvei's orders
 */
public interface NuveiOrderService {

    /**
     * Find the order related with clientUnique Id
     *
     * @param clientUniqueId
     * @return {@link AbstractOrderModel}
     */
    AbstractOrderModel findAbstractOrderModelByClientUniqueId(String clientUniqueId);

    /**
     * Create and send request cancel order
     *
     * @param orderModel
     * @return true if cancel is done
     */
    Boolean requestCancelOrder(final OrderModel orderModel);
}
