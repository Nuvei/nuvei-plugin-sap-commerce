package com.nuvei.services.exchange.impl;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.services.exchange.AbstractNuveiExchangeService;
import com.nuvei.services.exchange.NuveiExchangeService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import com.nuvei.services.wrapper.NuveiSafechargeWrapper;
import com.safecharge.exception.SafechargeException;
import com.safecharge.request.VoidTransactionRequest;
import com.safecharge.response.VoidTransactionResponse;
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

public class DefaultNuveiVoidService extends AbstractNuveiExchangeService implements NuveiExchangeService {

    protected static final Logger LOG = LogManager.getLogger(DefaultNuveiVoidService.class);

    protected static final String ORDER_CANNOT_BE_NULL = "Order cannot be null";
    protected static final String ERROR_IN_VOID_TRANSITION_REQUEST = "Error in void request: %s";
    protected static final String ERROR_IN_VOID_REQUEST = "Error voiding order with id [{}]";

    /**
     * Default constructor for {@link DefaultNuveiVoidService}
     * @param modelService
     * @param nuveiPaymentTransactionService
     */
    public DefaultNuveiVoidService(final ModelService modelService, final NuveiPaymentTransactionService nuveiPaymentTransactionService) {
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
                .filter(this::isNotValidTransactionType)
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
                        NuveiTransactionStatus.PENDING.getCode(), PaymentTransactionType.VOID);
            } catch (final SafechargeException e) {
                LOG.error(ERROR_IN_VOID_REQUEST, orderModel.getCode());
                return nuveiPaymentTransactionService.createPaymentTransactionEntry(paymentEntryAuthorized.getPaymentTransaction(),
                        NuveiTransactionStatus.ERROR.getCode(), PaymentTransactionType.VOID);
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

    /***
     * Returns false if the {@link PaymentTransactionEntryModel} is {@code CAPTURE}
     * @param paymentTransactionEntryModel the {@link PaymentTransactionEntryModel}
     * @return Returns false if the {@link PaymentTransactionEntryModel} is {@code CAPTURE}
     */
    protected boolean isNotValidTransactionType(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return !PaymentTransactionType.CAPTURE.equals(paymentTransactionEntryModel.getType());
    }

    /**
     * Execute Void transaction using safecharge-sdk
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

        final VoidTransactionRequest voidTransaction = nuveiSafechargeWrapper.createVoidTransaction(currencyIso, totalPrice, clientUniqueId, relatedTransactionId);
        storeSafechargeRequest(orderModel, voidTransaction);

        final VoidTransactionResponse voidTransactionResponse =
                (VoidTransactionResponse) nuveiSafechargeWrapper.executeTransaction(voidTransaction);
        storeSafechargeResponse(orderModel, voidTransactionResponse);

        modelService.save(orderModel);
        if (!Constants.APIResponseStatus.SUCCESS.equals(voidTransactionResponse.getStatus())) {
            throw new SafechargeException(
                    String.format(ERROR_IN_VOID_TRANSITION_REQUEST, voidTransactionResponse.getErrorType().name()));
        }
    }
}
