package com.nuvei.services.service;

import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;

import java.util.Optional;

/**
 * Service to handle logic related of {@link PaymentTransactionModel}
 */
public interface NuveiPaymentTransactionService {

    /**
     * Creates a payment transaction entry and attaches it to the payment transaction
     *  @param paymentTransaction       the payment transaction
     * @param notificationModel        the nuvei direct merchant notification
     * @param paymentTransactionType   the payment transaction type
     * @return
     */
    PaymentTransactionEntryModel createPaymentTransactionEntry(PaymentTransactionModel paymentTransaction, NuveiDirectMerchantNotificationModel notificationModel, PaymentTransactionType paymentTransactionType);

    /**
     * Creates a payment transaction entry and attaches it to the payment transaction
     *
     * @param paymentTransactionModel the payment transaction
     * @param status                  The order status
     * @param paymentTransactionType  The transaction type
     * @return {@link PaymentTransactionEntryModel} the created transactionEntryModel
     */
    PaymentTransactionEntryModel createPaymentTransactionEntry(final PaymentTransactionModel paymentTransactionModel,
                                                               final String status, final PaymentTransactionType paymentTransactionType);

    /**
     * Finds a payment transaction related to an event in an order. If the payment transaction cannot be found
     * then a new on will be created.
     * If payment transaction is not found. It will be created
     *
     * @param notificationModel the direct merchant notification
     * @param order the order which contains the payment transaction
     * @param nuveiTransactionType the status of the payment transaction
     * @return a {@link Optional <PaymentTransactionModel>} payment transaction
     */
    PaymentTransactionModel findOrCreatePaymentTransaction(NuveiDirectMerchantNotificationModel notificationModel, OrderModel order, NuveiTransactionType nuveiTransactionType);

    /**
     * Gets the first payment transaction for the related order
     *
     * @param order the order model
     * @return the first payment transaction
     */
    PaymentTransactionModel getPaymentTransaction(OrderModel order);
}
