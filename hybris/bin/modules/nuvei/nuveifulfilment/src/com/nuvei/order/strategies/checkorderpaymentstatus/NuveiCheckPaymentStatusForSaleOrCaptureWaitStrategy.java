package com.nuvei.order.strategies.checkorderpaymentstatus;

import com.nuvei.order.strategies.NuveiAbstractActionTransitionStrategy;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * This strategy ensures the order process go to WAIT step by checking there is not at least one order entry transaction
 * of type {@code CAPTURE} or {@code SALE}
 */
public class NuveiCheckPaymentStatusForSaleOrCaptureWaitStrategy extends NuveiAbstractActionTransitionStrategy {

    private static final Logger LOG = LogManager.getLogger(NuveiCheckPaymentStatusForSaleOrCaptureWaitStrategy.class);

    /**
     * Default constructor for {@link NuveiCheckPaymentStatusForSaleOrCaptureWaitStrategy}
     *
     * @param modelService injected
     */
    protected NuveiCheckPaymentStatusForSaleOrCaptureWaitStrategy(final ModelService modelService) {
        super(LOG, modelService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final OrderModel source) {
        updateOrderStatus(source, OrderStatus.CAPTURE_PENDING);
        return WAIT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final OrderModel source) {
        if (!CollectionUtils.isEmpty(source.getPaymentTransactions()) && source.getPaymentTransactions().size() > 1) {
            return false;
        }

        if (CollectionUtils.isEmpty(source.getPaymentTransactions())) {
            return true;
        }


        final PaymentTransactionEntryModel paymentEntryAuthorized = Stream.ofNullable(source.getPaymentTransactions())
                .flatMap(Collection::stream)
                .map(PaymentTransactionModel::getEntries)
                .flatMap(Stream::ofNullable)
                .flatMap(List::stream)
                .filter(this::isValidTransactionType)
                .findAny()
                .orElse(null);

        return paymentEntryAuthorized == null;
    }

    /**
     * Returns true when {@link PaymentTransactionEntryModel} is {@code CAPTURE} or {@code SALE}
     *
     * @param paymentTransactionEntryModel the {@link PaymentTransactionEntryModel}
     * @return Returns true when {@link PaymentTransactionEntryModel} is {@code CAPTURE} or {@code SALE}
     */
    protected boolean isValidTransactionType(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return PaymentTransactionType.CAPTURE.equals(paymentTransactionEntryModel.getType())
                || PaymentTransactionType.SALE.equals(paymentTransactionEntryModel.getType());
    }
}
