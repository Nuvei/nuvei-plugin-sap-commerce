package com.nuvei.services.exchange.impl;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.services.exchange.AbstractNuveiExchangeService;
import com.nuvei.services.exchange.NuveiRefundService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import com.nuvei.services.wrapper.NuveiSafechargeWrapper;
import com.safecharge.exception.SafechargeException;
import com.safecharge.request.RefundTransactionRequest;
import com.safecharge.response.RefundTransactionResponse;
import com.safecharge.util.Constants;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class DefaultNuveiRefundService extends AbstractNuveiExchangeService implements NuveiRefundService {

    protected static final Logger LOG = LogManager.getLogger(DefaultNuveiRefundService.class);

    protected static final String ORDER_CANNOT_BE_NULL = "Order cannot be null";
    protected static final String RETURN_REQUEST_CANNOT_BE_NULL = "Return request cannot be null";
    protected static final String ERROR_IN_REFUND_TRANSITION_REQUEST = "Error in refund request: %s";
    protected static final String ERROR_IN_REFUND_REQUEST = "Error refunding order with id [{}]";

    /**
     * Default constructor for {@link DefaultNuveiRefundService}
     *
     * @param modelService
     * @param nuveiPaymentTransactionService
     */
    public DefaultNuveiRefundService(final ModelService modelService, final NuveiPaymentTransactionService nuveiPaymentTransactionService) {
        super(modelService, nuveiPaymentTransactionService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentTransactionEntryModel requestSafechargeTransaction(final OrderModel orderModel) {
        validateParameterNotNull(orderModel, ORDER_CANNOT_BE_NULL);

        if (CollectionUtils.isNotEmpty(orderModel.getReturnRequests()) && orderModel.getReturnRequests().size() == 1) {
            return requestSafechargeTransaction(orderModel.getReturnRequests().get(0), new BigDecimal(orderModel.getTotalPrice()));
        }
        return null;
    }

    /***
     * Returns true if the {@link PaymentTransactionEntryModel} is {@code CAPTURE} and {@code APPROVED} or {@code SUCCESS}
     * @param paymentTransactionEntryModel the {@link PaymentTransactionEntryModel}
     * @return Returns true if the {@link PaymentTransactionEntryModel} is {@code CAPTURE} and {@code APPROVED} or {@code SUCCESS}
     */
    protected boolean isValidTransactionType(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return (PaymentTransactionType.CAPTURE.equals(paymentTransactionEntryModel.getType()) ||
                PaymentTransactionType.SALE.equals(paymentTransactionEntryModel.getType())) && (
                NuveiTransactionStatus.APPROVED.getCode().equals(paymentTransactionEntryModel.getTransactionStatus())
                        || NuveiTransactionStatus.SUCCESS.getCode().equals(paymentTransactionEntryModel.getTransactionStatus()));
    }

    /**
     * Execute Return transaction using safecharge-sdk
     *
     * @param currencyIso
     * @param totalPrice
     * @param merchantConfiguration
     * @param relatedTransactionId
     * @param clientUniqueId
     * @throws SafechargeException
     */
    protected void executeRequest(final OrderModel orderModel, final String currencyIso, final String totalPrice,
                                  final NuveiMerchantConfigurationModel merchantConfiguration,
                                  final String relatedTransactionId, final String clientUniqueId,
                                  final String clientRequestId) throws SafechargeException {
        final NuveiSafechargeWrapper nuveiSafechargeWrapper = getNuveiSafechargeWrapper(merchantConfiguration);

        final RefundTransactionRequest refundTransaction = nuveiSafechargeWrapper.
                createRefundTransaction(currencyIso, totalPrice, clientUniqueId, relatedTransactionId, clientRequestId);

        storeSafechargeRequest(orderModel, refundTransaction);

        final RefundTransactionResponse refundTransactionResponse =
                (RefundTransactionResponse) nuveiSafechargeWrapper.executeTransaction(refundTransaction);
        storeSafechargeResponse(orderModel, refundTransactionResponse);

        modelService.save(orderModel);
        if (!Constants.APIResponseStatus.SUCCESS.equals(refundTransactionResponse.getStatus())) {
            throw new SafechargeException(
                    String.format(ERROR_IN_REFUND_TRANSITION_REQUEST, refundTransactionResponse.getErrorType().name()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentTransactionEntryModel requestSafechargeTransaction(final ReturnRequestModel requestModel, final BigDecimal totalAmount) {
        validateParameterNotNull(requestModel, RETURN_REQUEST_CANNOT_BE_NULL);
        validateParameterNotNull(requestModel.getOrder(), ORDER_CANNOT_BE_NULL);

        final OrderModel orderModel = requestModel.getOrder();

        if (CollectionUtils.isNotEmpty(orderModel.getPaymentTransactions()) && orderModel.getPaymentTransactions().size() > 1) {
            throw new UnsupportedOperationException("More than one  PaymentTransactionModel found");
        }

        final PaymentTransactionEntryModel paymentEntryCaptured = Stream.ofNullable(orderModel.getPaymentTransactions())
                .flatMap(Collection::stream)
                .map(PaymentTransactionModel::getEntries)
                .flatMap(Stream::ofNullable)
                .flatMap(List::stream)
                .filter(this::isValidTransactionType)
                .findAny()
                .orElse(null);

        if (paymentEntryCaptured != null) {
            final NuveiMerchantConfigurationModel currentConfiguration = orderModel.getSite().getNuveiMerchantConfiguration();

            try {
                executeRequest(orderModel, paymentEntryCaptured.getCurrency().getIsocode(),
                        totalAmount.toString(), currentConfiguration, paymentEntryCaptured.getRequestId(),
                        requestModel.getOrder().getClientUniqueId(), requestModel.getCode());

                return createPaymentTransactionEntry(paymentEntryCaptured.getPaymentTransaction(),
                        requestModel, NuveiTransactionStatus.PENDING.getCode());

            } catch (final SafechargeException e) {
                LOG.error(ERROR_IN_REFUND_REQUEST, orderModel.getCode());
                return createPaymentTransactionEntry(paymentEntryCaptured.getPaymentTransaction(),
                        requestModel, NuveiTransactionStatus.ERROR.getCode());
            }

        } else {
            throw new UnsupportedOperationException(String.format("No PaymentTransactionEntryModel with type %s or %s has been found",
                    PaymentTransactionType.CAPTURE.getCode(), PaymentTransactionType.SALE.getCode()));
        }
    }

    /**
     * Create {@link PaymentTransactionEntryModel} with type {@code REFUND_FOLLOW_ON}
     *
     * @param paymentTransactionModel
     * @param requestModel
     * @param status
     * @return {@link PaymentTransactionEntryModel} with type {@code REFUND_FOLLOW_ON}
     */
    protected PaymentTransactionEntryModel createPaymentTransactionEntry(final PaymentTransactionModel paymentTransactionModel,
                                                                         final ReturnRequestModel requestModel, final String status) {
        final PaymentTransactionEntryModel paymentTransactionEntry = nuveiPaymentTransactionService.
                createPaymentTransactionEntry(paymentTransactionModel, status, PaymentTransactionType.REFUND_FOLLOW_ON);
        paymentTransactionEntry.setAmount(requestModel.getTotalTax());
        paymentTransactionEntry.setCurrency(requestModel.getCurrency());
        paymentTransactionEntry.setClientRequestId(requestModel.getCode());
        modelService.save(paymentTransactionEntry);

        return paymentTransactionEntry;
    }
}
