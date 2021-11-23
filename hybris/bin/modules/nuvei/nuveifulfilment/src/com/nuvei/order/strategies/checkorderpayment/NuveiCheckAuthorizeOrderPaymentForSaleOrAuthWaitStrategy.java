package com.nuvei.order.strategies.checkorderpayment;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.order.strategies.NuveiAbstractActionTransitionStrategy;
import com.nuvei.order.strategies.checkorderpaymentstatus.NuveiCheckPaymentStatusForSaleOrCaptureNOKStrategy;
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
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This strategy ensures the order process go to WAIT step by checking there is not at least one order entry transaction
 * of type {@code AUTHORIZATION} or {@code SALE}
 */
public class NuveiCheckAuthorizeOrderPaymentForSaleOrAuthWaitStrategy extends NuveiAbstractActionTransitionStrategy {

    private static final Logger LOG = LogManager.getLogger(NuveiCheckPaymentStatusForSaleOrCaptureNOKStrategy.class);

    /**
     * Default constructor for {@link NuveiCheckAuthorizeOrderPaymentForSaleOrAuthWaitStrategy}
     *
     * @param modelService injected
     */
    protected NuveiCheckAuthorizeOrderPaymentForSaleOrAuthWaitStrategy(final ModelService modelService) {
        super(LOG, modelService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final OrderModel source) {
        updateOrderStatus(source, OrderStatus.PENDING_AUTHORIZATION);
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
                .filter(this::isValidType)
                .filter(Predicate.not(this::isValidStatus))
                .findAny()
                .orElse(null);

        return paymentEntryAuthorized == null;
    }

    /**
     * Returns true if the {@link PaymentTransactionEntryModel} is {@code AUTHORIZATION} or {@code SALE}
     *
     * @param paymentTransactionEntryModel the {@link PaymentTransactionEntryModel}
     * @return Returns true if the {@link PaymentTransactionEntryModel} is {@code AUTHORIZATION} or {@code SALE}
     */
    protected boolean isValidType(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return PaymentTransactionType.AUTHORIZATION.equals(paymentTransactionEntryModel.getType())
                || PaymentTransactionType.SALE.equals(paymentTransactionEntryModel.getType());
    }

    /**
     * Returns true if the {@link PaymentTransactionEntryModel} is {@code PENDING}
     *
     * @param paymentTransactionEntryModel the {@link PaymentTransactionEntryModel}
     * @return Returns true if the {@link PaymentTransactionEntryModel} is {@code PENDING}
     */
    protected boolean isValidStatus(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return NuveiTransactionStatus.PENDING.getCode().equals(paymentTransactionEntryModel.getTransactionStatus());
    }
}
