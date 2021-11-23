package com.nuvei.notifications.strategies.orders;

import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import com.nuvei.services.service.NuveiPaymentTransactionEntryService;
import com.nuvei.strategy.NuveiStrategy;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Find {@link AbstractOrderModel} by default {@link NuveiTransactionType}
 */
public class NuveiOrderClientRequestIdByTypeStrategy implements NuveiStrategy<Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel>, AbstractOrderModel> {

    private static final Logger LOG = LogManager.getLogger(NuveiOrderClientRequestIdByTypeStrategy.class);

    protected final NuveiPaymentTransactionEntryService nuveiPaymentTransactionEntryService;

    public NuveiOrderClientRequestIdByTypeStrategy(final NuveiPaymentTransactionEntryService nuveiPaymentTransactionEntryService) {
        this.nuveiPaymentTransactionEntryService = nuveiPaymentTransactionEntryService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractOrderModel execute(final Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel> source) {
        LOG.info("Executing strategy  {}", NuveiOrderClientRequestIdByTypeStrategy.class.getName());

        final PaymentTransactionEntryModel paymentTransactionEntryModel =
                nuveiPaymentTransactionEntryService.findPaymentEntryByClientRequestIDAndType(source.getRight().getClientRequestId(), PaymentTransactionType.REFUND_FOLLOW_ON);

        return Optional.ofNullable(paymentTransactionEntryModel)
                .map(PaymentTransactionEntryModel::getPaymentTransaction)
                .map(PaymentTransactionModel::getOrder)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel> source) {
        return NuveiTransactionType.CREDIT.equals(source.getLeft());
    }
}
