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
 * This strategy ensures the order process continues to next step by checking there is at least one order entry transaction
 * of type {@code AUTHORIZATION} or {@code SALE} the transaction status is {@code APPROVED} or {@code SUCCESS} and the amount
 * of the transactions is equals to the amount in the order
 */
public class NuveiCheckAuthorizeOrderPaymentForSaleOrAuthOKStrategy extends NuveiAbstractActionTransitionStrategy {

    private static final Logger LOG = LogManager.getLogger(NuveiCheckAuthorizeOrderPaymentForSaleOrAuthOKStrategy.class);

    /**
     * Default constructor for {@link NuveiCheckAuthorizeOrderPaymentForSaleOrAuthOKStrategy}
     *
     * @param modelService injected
     */
    protected NuveiCheckAuthorizeOrderPaymentForSaleOrAuthOKStrategy(final ModelService modelService) {
        super(LOG, modelService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final OrderModel source) {
        updateOrderStatus(source, OrderStatus.PAYMENT_AUTHORIZED);
        return OK;
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
                .filter(this::isValidStatus)
                .findAny()
                .orElse(null);

        if (paymentEntryAuthorized == null) {
            return false;
        }

        return isTransactionPriceSameAsPlacedOrder(source, paymentEntryAuthorized);
    }


    /***
     * Returns true of price of the {@link OrderModel} is the same as the {@link PaymentTransactionEntryModel}
     * @param order The order
     * @param paymentTransactionEntryModel The payment transaction entry
     * @return true when prices matches
     */
    protected boolean isTransactionPriceSameAsPlacedOrder(final OrderModel order, final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return paymentTransactionEntryModel.getAmount().doubleValue() == order.getTotalPrice();
    }


    /**
     * Returns true if the {@link PaymentTransactionEntryModel} is {@code APPROVED} or {@code SUCCESS}
     *
     * @param paymentTransactionEntryModel the {@link PaymentTransactionEntryModel}
     * @return Returns true if the {@link PaymentTransactionEntryModel} is {@code APPROVED} or {@code SUCCESS}
     */
    protected boolean isValidStatus(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return NuveiTransactionStatus.APPROVED.getCode().equals(paymentTransactionEntryModel.getTransactionStatus())
                || NuveiTransactionStatus.SUCCESS.getCode().equals(paymentTransactionEntryModel.getTransactionStatus());
    }

    /***
     * Returns true if the {@link PaymentTransactionEntryModel} is {@code AUTHORIZATION} or {@code SALE}
     * @param paymentTransactionEntryModel the {@link PaymentTransactionEntryModel}
     * @return Returns true if the {@link PaymentTransactionEntryModel} is {@code AUTHORIZATION} or {@code SALE}
     */
    protected boolean isValidType(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return PaymentTransactionType.AUTHORIZATION.equals(paymentTransactionEntryModel.getType())
                || PaymentTransactionType.SALE.equals(paymentTransactionEntryModel.getType());
    }

}
