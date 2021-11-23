package com.nuvei.voids.actions;

import com.nuvei.model.NuveiVoidProcessModel;
import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.services.exchange.NuveiExchangeService;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * Nuvei action to cancel an Order
 */
public class NuveiVoidOrderAction extends AbstractAction<NuveiVoidProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(NuveiVoidOrderAction.class);

    protected static final String OK = "OK";
    protected static final String NOK = "NOK";
    protected static final String WAIT = "WAIT";

    protected final NuveiPaymentTransactionService nuveiPaymentTransactionService;
    protected final NuveiExchangeService nuveiExchangeService;

    public NuveiVoidOrderAction(final NuveiPaymentTransactionService nuveiPaymentTransactionService,
                                final NuveiExchangeService nuveiExchangeService) {
        this.nuveiPaymentTransactionService = nuveiPaymentTransactionService;
        this.nuveiExchangeService = nuveiExchangeService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getTransitions() {
        return AbstractAction.createTransitions(OK, NOK, WAIT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final NuveiVoidProcessModel process) {
        final OrderModel order = process.getOrder();

        if (order != null && CollectionUtils.isNotEmpty(order.getPaymentTransactions())) {
            final PaymentTransactionModel paymentTransaction = nuveiPaymentTransactionService.getPaymentTransaction(order);

            if (Boolean.TRUE.equals(isVoidPresent(paymentTransaction))) {
                LOG.info("Order with code [{}] has been already voided.", order.getCode());
                setOrderStatus(order, OrderStatus.VOIDED);
                LOG.info("Order with id [{}] status updated to [{}]", order.getCode(), OrderStatus.VOIDED.getCode());
                return OK;
            }

            if (Boolean.TRUE.equals(isVoidRefused(paymentTransaction))) {
                LOG.info("Order with code [{}] can not be voided.", order.getCode());
                return NOK;
            }

            final PaymentTransactionEntryModel authorizationTransactionEntryAccepted = findAcceptedAuthorizationEntry(paymentTransaction);

            if (authorizationTransactionEntryAccepted != null) {
                final PaymentTransactionEntryModel paymentTransactionEntryModel = nuveiExchangeService.requestSafechargeTransaction(order);
                if (NuveiTransactionStatus.PENDING.getCode().equals(paymentTransactionEntryModel.getTransactionStatus())) {
                    setOrderStatus(order, OrderStatus.VOID_STARTED);
                    LOG.info("Order with id [{}] status updated to [{}]", order.getCode(), OrderStatus.VOID_STARTED.getCode());
                    return WAIT;
                }
            } else {
                LOG.error("Accepted authorization entry not present for order with code [{}] and process [{}]", order.getCode(), process.getCode());
                return NOK;
            }
        }

        LOG.error("Order not found for order process with code [{}] or transaction not present.", process.getCode());
        return NOK;
    }

    protected PaymentTransactionEntryModel findAcceptedAuthorizationEntry(final PaymentTransactionModel paymentTransaction) {
        return paymentTransaction.getEntries().stream()
                .filter(paymentTransactionEntryModel ->
                        PaymentTransactionType.AUTHORIZATION.equals(paymentTransactionEntryModel.getType()) &&
                                (NuveiTransactionStatus.APPROVED.getCode().equals(paymentTransactionEntryModel.getTransactionStatus())
                                        || NuveiTransactionStatus.SUCCESS.getCode().equals(paymentTransactionEntryModel.getTransactionStatus()))
                ).findAny()
                .orElse(null);
    }

    protected Boolean isVoidRefused(final PaymentTransactionModel paymentTransactionModel) {
        return paymentTransactionModel.getEntries().stream()
                .filter(paymentTransactionEntry -> PaymentTransactionType.VOID.equals(paymentTransactionEntry.getType()))
                .anyMatch(transactionEntry -> (NuveiTransactionStatus.DECLINED.getCode().equalsIgnoreCase(transactionEntry.getTransactionStatus())
                        || NuveiTransactionStatus.ERROR.getCode().equalsIgnoreCase(transactionEntry.getTransactionStatus())));
    }

    protected Boolean isVoidPresent(final PaymentTransactionModel paymentTransactionModel) {
        return paymentTransactionModel.getEntries().stream()
                .filter(paymentTransactionEntry -> PaymentTransactionType.VOID.equals(paymentTransactionEntry.getType()))
                .anyMatch(transactionEntry -> (NuveiTransactionStatus.SUCCESS.getCode().equalsIgnoreCase(transactionEntry.getTransactionStatus())
                        || NuveiTransactionStatus.APPROVED.getCode().equalsIgnoreCase(transactionEntry.getTransactionStatus())));
    }
}
