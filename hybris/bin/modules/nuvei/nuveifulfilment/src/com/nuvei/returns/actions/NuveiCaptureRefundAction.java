package com.nuvei.returns.actions;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.services.exchange.NuveiRefundService;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static org.assertj.core.util.Preconditions.checkArgument;

/**
 * Custom Capture refund action that send request to Nuvei.
 */
public class NuveiCaptureRefundAction extends AbstractAction<ReturnProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(NuveiCaptureRefundAction.class);

    private static final String OK = "OK";
    private static final String WAIT = "WAIT";
    private static final String NOK = "NOK";

    protected final NuveiPaymentTransactionService nuveiPaymentTransactionService;
    protected final NuveiRefundService nuveiExchangeService;

    public NuveiCaptureRefundAction(final NuveiPaymentTransactionService nuveiPaymentTransactionService, final NuveiRefundService nuveiExchangeService) {
        this.nuveiPaymentTransactionService = nuveiPaymentTransactionService;
        this.nuveiExchangeService = nuveiExchangeService;
    }

    @Override
    public String execute(final ReturnProcessModel process) {
        LOG.debug("Process: {} in step {}", process.getCode(), getClass().getSimpleName());
        final ReturnRequestModel returnRequest = process.getReturnRequest();
        final OrderModel order = returnRequest.getOrder();
        if (Objects.nonNull(order)) {
            final List<PaymentTransactionModel> paymentTransactions = order.getPaymentTransactions();

            if (CollectionUtils.isEmpty(paymentTransactions)) {
                LOG.info("Unable to refund for ReturnRequest [{}], no PaymentTransactions found", returnRequest.getCode());
                setReturnRequestStatus(returnRequest, ReturnStatus.PAYMENT_REVERSAL_FAILED);
                return NOK;
            }

            final PaymentTransactionModel paymentTransaction = nuveiPaymentTransactionService.getPaymentTransaction(order);

            if (Boolean.TRUE.equals(hasBeenRefunded(paymentTransaction, returnRequest))) {
                setReturnRequestStatus(returnRequest, ReturnStatus.PAYMENT_REVERSED);
                return OK;
            }

            if (Boolean.TRUE.equals(hasBeenDeclined(paymentTransaction, returnRequest))) {
                return NOK;
            }

            final BigDecimal totalAmountToRefund = getRefundAmount(returnRequest);
            final PaymentTransactionEntryModel acceptedCaptureEntry = findAcceptedEntry(paymentTransaction);

            if (acceptedCaptureEntry != null) {
                final PaymentTransactionEntryModel paymentTransactionEntryModel = nuveiExchangeService.requestSafechargeTransaction(returnRequest, totalAmountToRefund);
                if (NuveiTransactionStatus.PENDING.getCode().equals(paymentTransactionEntryModel.getTransactionStatus())) {
                    return WAIT;
                }
            } else {
                LOG.error("Accepted sale or capture entry not present for order with code [{}] and process [{}]", order.getCode(), process.getCode());
                return NOK;
            }
        }

        return NOK;
    }

    /**
     * Check if  return request has been refunded
     *
     * @param paymentTransaction
     * @param returnRequest
     * @return true if  return request has been refunded, false otherwise
     */
    protected Boolean hasBeenRefunded(final PaymentTransactionModel paymentTransaction, final ReturnRequestModel returnRequest) {
        return paymentTransaction.getEntries().stream()
                .filter(this::checkRefundTransactionSuccessOrApprove)
                .anyMatch(paymentTransactionEntry -> (returnRequest.getCode()).equals(paymentTransactionEntry.getClientRequestId()));
    }

    /**
     * Check if transaction has type {@code REFUND_FOLLOW_ON} and status  {@code SUCCESS} or {@code SUCCESS}
     *
     * @param paymentTransactionEntry
     * @return true if transaction has type {@code REFUND_FOLLOW_ON} and status  {@code SUCCESS} or {@code SUCCESS} else otherwise
     */
    protected boolean checkRefundTransactionSuccessOrApprove(PaymentTransactionEntryModel paymentTransactionEntry) {
        return PaymentTransactionType.REFUND_FOLLOW_ON.equals(paymentTransactionEntry.getType()) &&
                NuveiTransactionStatus.SUCCESS.getCode().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus())
                || NuveiTransactionStatus.APPROVED.getCode().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus());
    }

    /**
     * Check if  return request has been declined
     *
     * @param paymentTransaction
     * @param returnRequest
     * @return true if  return request has been declined
     */
    protected Boolean hasBeenDeclined(final PaymentTransactionModel paymentTransaction, final ReturnRequestModel returnRequest) {
        return paymentTransaction.getEntries().stream()
                .filter(this::checkRefundTransactionDeclinedOrError)
                .anyMatch(paymentTransactionEntry -> (returnRequest.getCode()).equals(paymentTransactionEntry.getClientRequestId()));
    }

    /**
     * Check if transaction has type {@code REFUND_FOLLOW_ON} and status  {@code DECLINED} or {@code ERROR}
     *
     * @param paymentTransactionEntry
     * @return true if transaction has type {@code REFUND_FOLLOW_ON} and status  {@code DECLINED} or {@code ERROR} else otherwise
     */
    protected boolean checkRefundTransactionDeclinedOrError(PaymentTransactionEntryModel paymentTransactionEntry) {
        return PaymentTransactionType.REFUND_FOLLOW_ON.equals(paymentTransactionEntry.getType()) &&
                NuveiTransactionStatus.DECLINED.getCode().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus())
                || NuveiTransactionStatus.ERROR.getCode().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus());
    }

    /**
     * Find {@link PaymentTransactionEntryModel} is has type {@code CAPTURE} or {@code SALE} and status {@code APPROVED} or {@code SUCCESS}
     *
     * @param paymentTransaction
     * @return {@link PaymentTransactionEntryModel} is has type {@code CAPTURE} or {@code SALE} and status {@code APPROVED} or {@code SUCCESS}
     */
    protected PaymentTransactionEntryModel findAcceptedEntry(final PaymentTransactionModel paymentTransaction) {
        return paymentTransaction.getEntries().stream()
                .filter(this::checkCaptureOrSaleTransactionApproveOrSuccess)
                .findAny()
                .orElse(null);
    }

    private boolean checkCaptureOrSaleTransactionApproveOrSuccess(PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return (PaymentTransactionType.CAPTURE.equals(paymentTransactionEntryModel.getType()) ||
                PaymentTransactionType.SALE.equals(paymentTransactionEntryModel.getType())) &&
                (NuveiTransactionStatus.APPROVED.getCode().equals(paymentTransactionEntryModel.getTransactionStatus())
                        || NuveiTransactionStatus.SUCCESS.getCode().equals(paymentTransactionEntryModel.getTransactionStatus()));
    }

    /**
     * Simple calculation of the amount to be refunded. Use this as an example and adjust to your business requirements
     *
     * @param returnRequest the return request
     * @return the amount to refund
     */
    protected BigDecimal getRefundAmount(final ReturnRequestModel returnRequest) {
        checkArgument(CollectionUtils.isNotEmpty(returnRequest.getReturnEntries()), "Parameter Return Entries cannot be null");

        BigDecimal refundAmount = returnRequest.getReturnEntries().stream()
                .map(this::getRefundEntryAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (Boolean.TRUE.equals(returnRequest.getRefundDeliveryCost())) {
            refundAmount = refundAmount.add(BigDecimal.valueOf(returnRequest.getOrder().getDeliveryCost()));
        }
        return refundAmount.setScale(getNumberOfDigits(returnRequest), RoundingMode.CEILING);
    }

    /**
     * Calculate amount to refund
     *
     * @param returnEntryModel
     * @return amount to refund
     */
    protected BigDecimal getRefundEntryAmount(final ReturnEntryModel returnEntryModel) {
        validateParameterNotNull(returnEntryModel, "Parameter Return Entry cannot be null");

        final ReturnRequestModel returnRequest = returnEntryModel.getReturnRequest();

        return Optional.of(returnEntryModel)
                .filter(RefundEntryModel.class::isInstance)
                .map(RefundEntryModel.class::cast)
                .map(RefundEntryModel::getAmount)
                .map(amount -> amount.setScale(getNumberOfDigits(returnRequest), RoundingMode.HALF_DOWN))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Update the return status for all return entries in {@link ReturnRequestModel}
     *
     * @param returnRequest the return request
     * @param status        the return status
     */
    protected void setReturnRequestStatus(final ReturnRequestModel returnRequest, final ReturnStatus status) {
        returnRequest.setStatus(status);
        returnRequest.getReturnEntries().forEach(entry -> {
            entry.setStatus(status);
            save(entry);
        });
        save(returnRequest);
    }

    /**
     * get number of digits from {@link CurrencyModel}
     *
     * @param returnRequest
     * @return the digits from {@link CurrencyModel}
     */
    protected int getNumberOfDigits(final ReturnRequestModel returnRequest) {
        return returnRequest.getOrder().getCurrency().getDigits();
    }

    @Override
    public Set<String> getTransitions() {
        return AbstractAction.createTransitions(OK, WAIT, NOK);
    }
}
