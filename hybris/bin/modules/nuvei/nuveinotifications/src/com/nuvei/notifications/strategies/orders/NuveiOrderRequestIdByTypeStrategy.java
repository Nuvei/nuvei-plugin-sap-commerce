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
public class NuveiOrderRequestIdByTypeStrategy implements NuveiStrategy<Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel>, AbstractOrderModel> {

    private static final Logger LOG = LogManager.getLogger(NuveiOrderRequestIdByTypeStrategy.class);

    protected final NuveiPaymentTransactionEntryService nuveiPaymentTransactionEntryService;

    public NuveiOrderRequestIdByTypeStrategy(final NuveiPaymentTransactionEntryService nuveiPaymentTransactionEntryService) {
        this.nuveiPaymentTransactionEntryService = nuveiPaymentTransactionEntryService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractOrderModel execute(final Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel> source) {
        LOG.info("Executing strategy  {}", NuveiOrderRequestIdByTypeStrategy.class.getName());

        final PaymentTransactionEntryModel paymentTransactionEntryModel =
                nuveiPaymentTransactionEntryService.findPaymentEntryByRequestIdAndType(source.getRight().getRelatedTransactionId(), PaymentTransactionType.AUTHORIZATION);

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
        return NuveiTransactionType.SETTLE.equals(source.getLeft()) ||
                NuveiTransactionType.VOID.equals(source.getLeft());
    }
}
