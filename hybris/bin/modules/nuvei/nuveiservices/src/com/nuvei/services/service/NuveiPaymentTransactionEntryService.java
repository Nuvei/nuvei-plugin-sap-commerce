package com.nuvei.services.service;

import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

/**
 * Service to handle logic related of {@link PaymentTransactionEntryModel}
 */
public interface NuveiPaymentTransactionEntryService {
    /**
     * Find {@link PaymentTransactionEntryModel} matches by type and requestId
     * @param requestId
     * @return {@link PaymentTransactionEntryModel} by requestId and type
     */
    PaymentTransactionEntryModel findPaymentEntryByRequestIdAndType(String requestId, final PaymentTransactionType type);

    /**
     * Find {@link PaymentTransactionEntryModel} matches by type and requestId
     * @param clientRequestId
     * @return {@link PaymentTransactionEntryModel} by requestId and type
     */
    PaymentTransactionEntryModel findPaymentEntryByClientRequestIDAndType(final String clientRequestId, final PaymentTransactionType type);
}
