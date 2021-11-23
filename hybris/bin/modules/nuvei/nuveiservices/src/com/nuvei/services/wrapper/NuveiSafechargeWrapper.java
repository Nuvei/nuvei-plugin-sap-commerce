package com.nuvei.services.wrapper;

import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.util.NuveiHashAlgorithmUtil;
import com.safecharge.biz.RequestBuilder;
import com.safecharge.biz.Safecharge;
import com.safecharge.biz.SafechargeRequestExecutor;
import com.safecharge.biz.ServiceFactory;
import com.safecharge.exception.SafechargeConfigurationException;
import com.safecharge.exception.SafechargeException;
import com.safecharge.model.MerchantInfo;
import com.safecharge.model.PaymentMethod;
import com.safecharge.model.UserAddress;
import com.safecharge.request.*;
import com.safecharge.response.GetMerchantPaymentMethodsResponse;
import com.safecharge.response.OpenOrderResponse;
import com.safecharge.response.SafechargeResponse;
import com.safecharge.response.SafechargeTransactionResponse;
import com.safecharge.util.Constants;
import com.safecharge.util.Constants.APIResponseStatus;

import java.util.List;

public class NuveiSafechargeWrapper {

    protected static final String MISSING_MANDATORY_INFO_MESSAGE = "Missing mandatory info for execution of payments! Please run initialization method before creating payments.";
    protected static final String ERROR_IN_OPEN_ORDER_REQUEST = "Error in openOrder request: %s";

    protected ServiceFactory serviceFactory;
    protected String sessionToken;
    protected Safecharge safecharge;
    protected MerchantInfo merchantInfo;
    protected SafechargeRequestExecutor requestExecutor;

    public NuveiSafechargeWrapper(final NuveiMerchantConfigurationModel merchantConfiguration) {
        this.safecharge = new Safecharge();
        this.serviceFactory = new ServiceFactory();
        this.merchantInfo = createMerchantInfo(merchantConfiguration);
        this.requestExecutor = SafechargeRequestExecutor.getInstance();
    }

    /**
     * <p>
     * This method should be used to create request for getMerchantPaymentMethods endpoint in Safecharge's REST API.
     * </p>
     *
     * @return Passes through the response from Safecharge's REST API.
     * @throws SafechargeException if there are request related problems.
     */
    public List<PaymentMethod> getMerchantPaymentMethods() throws SafechargeException {
        initialize();
        ensureMerchantInfoAndSessionTokenNotNull();

        final SafechargeBaseRequest request = GetMerchantPaymentMethodsRequest.builder()
                .addSessionToken(sessionToken)
                .addMerchantInfo(merchantInfo)
                .build();

        final GetMerchantPaymentMethodsResponse response =
                (GetMerchantPaymentMethodsResponse) requestExecutor.execute(request);
        return response.getPaymentMethods();
    }

    /**
     * <p>
     * This method should be used to create request for openOrder endpoint in Safecharge's API.
     * </p>
     *
     * @param userTokenId     This field uniquely identifies your consumer/user in your system.
     *                        Required if you wish to use the paymentOptionId field for future charging of this user without re-collecting the payment details
     * @param currency        The three-character ISO currency code.
     * @param amount          The transaction amount.
     * @param billingAddress  The billing address.
     * @param shippingAddress The delivery address.
     * @return The session token of the response from Safecharge's API.
     * @throws SafechargeConfigurationException If the {@link Safecharge#initialize(String, String, String, String, Constants.HashAlgorithm)}
     *                                          method is not invoked beforehand SafechargeConfigurationException exception will be thrown.
     * @throws SafechargeException              if there are request related problems or the response is not success.
     */
    public String openOrder(final String userTokenId, final String currency, final String amount,
                            final UserAddress billingAddress, final UserAddress shippingAddress,
                            final String clientUniqueId) throws SafechargeException {
        initializeSafecharge();
        final OpenOrderResponse openOrderResponse = safecharge.openOrder(userTokenId, null, clientUniqueId,
                null, null, null, null, currency, amount,
                null, null, null, shippingAddress, billingAddress, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null, null);

        if (!APIResponseStatus.SUCCESS.equals(openOrderResponse.getStatus())) {
            throw new SafechargeException(
                    String.format(ERROR_IN_OPEN_ORDER_REQUEST, openOrderResponse.getErrorType().name()));
        }
        return openOrderResponse.getSessionToken();
    }

    /***
     * <p>
     * This method should be used to create request for settle endpoint in Safecharge's API.
     * </p>
     * @param currency
     * @param amount
     * @param clientUniqueId
     * @param relatedTransactionId
     * @throws SafechargeException
     * @return the Safecharge settle transaction request
     */
    public SettleTransactionRequest createSettleTransaction(final String currency, final String amount, final String clientUniqueId,
                                                            final String relatedTransactionId) throws SafechargeException {
        initializeSafecharge();
        final RequestBuilder requestBuilder = serviceFactory.getRequestBuilder();

        return (SettleTransactionRequest) requestBuilder.getSettleTransactionRequest(this.sessionToken, this.merchantInfo,
                clientUniqueId, null, null, null, null, null,
                amount, null, null, null, currency, null, null, relatedTransactionId, null);
    }

    /**
     * Executes the safecharge transaction
     *
     * @param request Safecharge transaction request
     * @return the Safecharge transaction response
     * @throws SafechargeException
     */
    public SafechargeTransactionResponse executeTransaction(final SafechargeTransactionRequest request) throws SafechargeException {
        return (SafechargeTransactionResponse) requestExecutor.execute(request);
    }

    /***
     * <p>
     * This method should be used to create request for void endpoint in Safecharge's API.
     * </p>
     * @param currency
     * @param amount
     * @param clientUniqueId
     * @param relatedTransactionId
     * @throws SafechargeException
     * @return the Safecharge void transaction request
     */
    public VoidTransactionRequest createVoidTransaction(final String currency, final String amount, final String clientUniqueId,
                                                        final String relatedTransactionId) throws SafechargeException {
        initializeSafecharge();
        final RequestBuilder requestBuilder = serviceFactory.getRequestBuilder();

        return (VoidTransactionRequest) requestBuilder.getVoidTransactionRequest(this.sessionToken, null,
                this.merchantInfo, relatedTransactionId, amount, currency, null, clientUniqueId, null, null,
                null, null, null, null);
    }

    /***
     * <p>
     * This method should be used to create request for refund endpoint in Safecharge's API.
     * </p>
     * @param currency
     * @param amount
     * @param clientUniqueId
     * @param relatedTransactionId
     * @throws SafechargeException
     * @return the Safecharge refund transaction request
     */
    public RefundTransactionRequest createRefundTransaction(final String currency, final String amount, final String clientUniqueId,
                                                        final String relatedTransactionId, final String clientRequestId) throws SafechargeException {
        initializeSafecharge();
        final RequestBuilder requestBuilder = serviceFactory.getRequestBuilder();

        return (RefundTransactionRequest) requestBuilder.getRefundTransactionRequest(this.sessionToken, this.merchantInfo,
                clientUniqueId, clientRequestId, null, amount, null, null,  currency,
                null, null, null, relatedTransactionId, null);
    }

    /**
     * <p>
     * This method should always be invoked before any other method from the {@link Safecharge}.
     * It takes care of setting up MerchantInfo object and getting the mandatory sessionToken.
     * </p>
     *
     * @throws SafechargeConfigurationException if getting a session token isn't successful.
     * @throws SafechargeException              if there are request related problems.
     */
    protected void initializeSafecharge() throws SafechargeException {
        safecharge.initialize(merchantInfo.getMerchantId(), merchantInfo.getMerchantSiteId(),
                merchantInfo.getMerchantKey(), merchantInfo.getServerHost(), merchantInfo.getHashAlgorithm());
    }

    /**
     * Initialize session token
     *
     * @throws SafechargeException
     */
    protected void initialize() throws SafechargeException {
        this.sessionToken = getSessionToken();
    }

    /**
     * Create {@link MerchantInfo} from {@NuveiMerchantConfigurationModel}
     *
     * @param merchantConfiguration
     * @return {@link MerchantInfo}
     */
    protected MerchantInfo createMerchantInfo(final NuveiMerchantConfigurationModel merchantConfiguration) {
        return new MerchantInfo(merchantConfiguration.getMerchantSecretKey(),
                merchantConfiguration.getMerchantId(),
                merchantConfiguration.getMerchantSiteId(),
                merchantConfiguration.getServerHost(),
                NuveiHashAlgorithmUtil.getNuveiHashAlgorithmName(merchantConfiguration.getHashAlgorithm()));
    }

    /**
     * Create session token
     *
     * @return String session token
     * @throws SafechargeException
     */
    protected String getSessionToken() throws SafechargeException {
        final RequestBuilder requestBuilder = serviceFactory.getRequestBuilder();
        final SafechargeBaseRequest request = requestBuilder.getSessionTokenRequest(merchantInfo);
        final SafechargeResponse response = requestExecutor.execute(request);
        if (APIResponseStatus.ERROR.equals(response.getStatus())) {
            throw new SafechargeConfigurationException(response.getReason());
        }

        return response.getSessionToken();
    }

    /**
     * Check if {@link MerchantInfo} and session token are not null
     */
    protected void ensureMerchantInfoAndSessionTokenNotNull() {
        if (merchantInfo == null || sessionToken == null) {
            throw new SafechargeConfigurationException(MISSING_MANDATORY_INFO_MESSAGE);
        }
    }
}
