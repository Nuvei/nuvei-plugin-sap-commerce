package com.nuvei.services.exchange.impl;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.services.exchange.AbstractNuveiExchangeService;
import com.nuvei.services.exchange.NuveiExchangeService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import com.nuvei.services.wrapper.NuveiSafechargeWrapper;
import com.safecharge.exception.SafechargeException;
import com.safecharge.request.SettleTransactionRequest;
import com.safecharge.response.SettleTransactionResponse;
import com.safecharge.util.Constants;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Implementation of {@link NuveiExchangeService} for Settle transactions
 */
public class DefaultNuveiSettleService extends AbstractNuveiExchangeService implements NuveiExchangeService {

    protected static final Logger LOG = LogManager.getLogger(DefaultNuveiSettleService.class);

    protected static final String ORDER_CANNOT_BE_NULL = "Order cannot be null";
    protected static final String ERROR_IN_SETTLE_TRANSITION_REQUEST = "Error in settle request: %s";
    protected static final String ERROR_IN_CAPTURE_REQUEST = "Error capturing order with id [{}]";

    /**
     * Default constructor for {@link DefaultNuveiSettleService}
     * @param modelService
     * @param nuveiPaymentTransactionService
     */
    public DefaultNuveiSettleService(final ModelService modelService, final NuveiPaymentTransactionService nuveiPaymentTransactionService) {
        super(modelService, nuveiPaymentTransactionService);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentTransactionEntryModel requestSafechargeTransaction(final OrderModel orderModel) {
        validateParameterNotNull(orderModel, ORDER_CANNOT_BE_NULL);

        if (CollectionUtils.isNotEmpty(orderModel.getPaymentTransactions()) && orderModel.getPaymentTransactions().size() > 1) {
            throw new UnsupportedOperationException("More than one  PaymentTransactionModel found");
        }

        final PaymentTransactionEntryModel paymentEntryAuthorized = Stream.ofNullable(orderModel.getPaymentTransactions())
                .flatMap(Collection::stream)
                .map(PaymentTransactionModel::getEntries)
                .flatMap(Stream::ofNullable)
                .flatMap(List::stream)
                .filter(this::isValidTransactionType)
                .findAny()
                .orElse(null);

        if (paymentEntryAuthorized != null) {
            final NuveiMerchantConfigurationModel currentConfiguration = orderModel.getSite().getNuveiMerchantConfiguration();
            try {
                executeRequest(orderModel, paymentEntryAuthorized.getCurrency().getIsocode(),
                        paymentEntryAuthorized.getAmount().toString(), currentConfiguration,
                        paymentEntryAuthorized.getRequestId(),
                        paymentEntryAuthorized.getPaymentTransaction().getOrder().getClientUniqueId());
                return nuveiPaymentTransactionService.createPaymentTransactionEntry(paymentEntryAuthorized.getPaymentTransaction(),
                        NuveiTransactionStatus.PENDING.getCode(), PaymentTransactionType.CAPTURE);
            } catch (final SafechargeException e) {
                LOG.error(ERROR_IN_CAPTURE_REQUEST, orderModel.getCode());
                return nuveiPaymentTransactionService.createPaymentTransactionEntry(paymentEntryAuthorized.getPaymentTransaction(),
                        NuveiTransactionStatus.ERROR.getCode(), PaymentTransactionType.CAPTURE);
            }
        } else {
            throw new UnsupportedOperationException(String.format("No PaymentTransactionEntryModel with type %s has been found",
                    PaymentTransactionType.AUTHORIZATION.getCode()));
        }
    }

    /***
     * Returns true if the {@link PaymentTransactionEntryModel} is {@code AUTHORIZATION}
     * @param paymentTransactionEntryModel the {@link PaymentTransactionEntryModel}
     * @return Returns true if the {@link PaymentTransactionEntryModel} is {@code AUTHORIZATION}
     */
    protected boolean isValidTransactionType(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return PaymentTransactionType.AUTHORIZATION.equals(paymentTransactionEntryModel.getType());
    }

    /**
     * Execute Settle transaction using safecharge-sdk
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
                                  final String relatedTransactionId, final String clientUniqueId) throws SafechargeException {
        final NuveiSafechargeWrapper nuveiSafechargeWrapper = getNuveiSafechargeWrapper(merchantConfiguration);

        final SettleTransactionRequest settleTransaction = nuveiSafechargeWrapper.createSettleTransaction(currencyIso, totalPrice,
                clientUniqueId, relatedTransactionId);
        storeSafechargeRequest(orderModel, settleTransaction);

        final SettleTransactionResponse settleTransactionResponse =
                (SettleTransactionResponse)nuveiSafechargeWrapper.executeTransaction(settleTransaction);
        storeSafechargeResponse(orderModel, settleTransactionResponse);

        modelService.save(orderModel);
        if (!Constants.APIResponseStatus.SUCCESS.equals(settleTransactionResponse.getStatus())) {
            throw new SafechargeException(
                    String.format(ERROR_IN_SETTLE_TRANSITION_REQUEST, settleTransactionResponse.getErrorType().name()));
        }
    }


}
