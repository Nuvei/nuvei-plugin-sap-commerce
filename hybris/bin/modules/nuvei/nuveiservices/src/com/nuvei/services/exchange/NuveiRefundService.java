package com.nuvei.services.exchange;

import com.safecharge.exception.SafechargeException;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.math.BigDecimal;

/**
 * Handle request settle transactions
 */
public interface NuveiRefundService extends NuveiExchangeService {

    /**
     * Create and execute Refund transaction that is associated to the {@link ReturnRequestModel}
     *
     * @param returnRequestModel the order returned
     * @param totalAmount        amount of the refund
     * @throws {@link SafechargeException} the exception throws
     */
    PaymentTransactionEntryModel requestSafechargeTransaction(final ReturnRequestModel returnRequestModel, BigDecimal totalAmount);

}
