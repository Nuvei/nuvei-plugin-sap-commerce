package com.nuvei.order.strategies.checkorderpayment;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
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
 * This strategy ensures the order process go to NOK step by checking there is at least one order entry transaction
 * of type {@code AUTHORIZATION} or {@code SALE} and the transaction status is {@code DECLINED} or {@code ERROR}
 */
public class NuveiCheckAuthorizeOrderPaymentForSaleOrAuthNOKStatusStrategy extends NuveiAbstractActionTransitionStrategy {

    private static final Logger LOG = LogManager.getLogger(NuveiCheckAuthorizeOrderPaymentForSaleOrAuthNOKStatusStrategy.class);

    /**
     * Default constructor for {@link NuveiCheckAuthorizeOrderPaymentForSaleOrAuthNOKStatusStrategy}
     *
     * @param modelService injected
     */
    protected NuveiCheckAuthorizeOrderPaymentForSaleOrAuthNOKStatusStrategy(final ModelService modelService) {
        super(LOG, modelService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final OrderModel source) {
        updateOrderStatus(source, OrderStatus.PAYMENT_NOT_AUTHORIZED);
        return NOK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final OrderModel source) {
        if (!CollectionUtils.isEmpty(source.getPaymentTransactions()) && source.getPaymentTransactions().size() != 1) {
            return false;
        }

        final PaymentTransactionEntryModel paymentEntryAuthorized = Stream.ofNullable(source.getPaymentTransactions())
                .flatMap(Collection::stream)
                .map(PaymentTransactionModel::getEntries)
                .flatMap(Stream::ofNullable)
                .flatMap(List::stream)
                .filter(this::isValidType)
                .findAny()
                .orElse(null);

        if (paymentEntryAuthorized == null) {
            return false;
        }

        return isErrorOrDeclined(paymentEntryAuthorized);
    }

    /**
     * Returns true if the {@link PaymentTransactionEntryModel} is {@code DECLINED} or {@code ERROR}
     *
     * @param paymentTransactionEntryModel the {@link PaymentTransactionEntryModel}
     * @return Returns true if the {@link PaymentTransactionEntryModel} is {@code DECLINED} or {@code ERROR}
     */
    protected boolean isErrorOrDeclined(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return NuveiTransactionStatus.DECLINED.getCode().equals(paymentTransactionEntryModel.getTransactionStatus())
                || NuveiTransactionStatus.ERROR.getCode().equals(paymentTransactionEntryModel.getTransactionStatus());
    }

    /**
     * Returns true if {@link PaymentTransactionModel} is {@code SALE} or {@code AUTHORIZATION}
     *
     * @param entry the {@link PaymentTransactionModel}
     * @return Returns true if {@link PaymentTransactionModel} is {@code SALE} or {@code AUTHORIZATION}
     */
    protected boolean isValidType(final PaymentTransactionEntryModel entry) {
        return PaymentTransactionType.AUTHORIZATION.equals(entry.getType())
                || PaymentTransactionType.SALE.equals(entry.getType());
    }

}
