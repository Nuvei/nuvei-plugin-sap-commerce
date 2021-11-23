package com.nuvei.services.exchange;

import com.safecharge.exception.SafechargeException;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

/**
 * Handle request settle transactions
 */
public interface NuveiExchangeService {

    /**
     * Create and execute Settle transaction that is associated to the {@link OrderModel}
     *
     * @param orderModel
     * @throws SafechargeException
     */
    PaymentTransactionEntryModel requestSafechargeTransaction(OrderModel orderModel);

}
