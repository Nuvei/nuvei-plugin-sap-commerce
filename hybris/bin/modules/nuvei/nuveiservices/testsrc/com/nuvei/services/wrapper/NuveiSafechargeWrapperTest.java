package com.nuvei.services.wrapper;

import com.nuvei.services.enums.NuveiHashAlgorithm;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.safecharge.biz.RequestBuilder;
import com.safecharge.biz.Safecharge;
import com.safecharge.biz.SafechargeRequestExecutor;
import com.safecharge.biz.ServiceFactory;
import com.safecharge.exception.SafechargeConfigurationException;
import com.safecharge.exception.SafechargeException;
import com.safecharge.model.MerchantInfo;
import com.safecharge.model.PaymentMethod;
import com.safecharge.model.UserAddress;
import com.safecharge.request.SafechargeBaseRequest;
import com.safecharge.request.SettleTransactionRequest;
import com.safecharge.response.GetMerchantPaymentMethodsResponse;
import com.safecharge.response.OpenOrderResponse;
import com.safecharge.response.SafechargeResponse;
import com.safecharge.response.SettleTransactionResponse;
import com.safecharge.util.Constants;
import de.hybris.bootstrap.annotations.UnitTest;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(JUnitParamsRunner.class)
public class NuveiSafechargeWrapperTest {

    private static final String AMOUNT = "amount";
    private static final String MERCHANT_ID = "merchantId";
    private static final String SERVER_HOST = "serverHost";
    private static final String USER_TOKEN_ID = "userTokenId";
    private static final String SESSION_TOKEN = "sessionToken";
    private static final String MERCHANT_SITE_ID = "merchantSiteId";
    private static final String CURRENCY_ISO_CODE = "currencyIsoCode";
    private static final String MERCHANT_SECRET_ID = "merchantSecretId";
    private static final String CLIENT_UNIQUE_ID = "clientUniqueId";
    private static final String RELATED_TRANSACTION_ID = "relatedTransactionId";
    private static final String CLIENT_REQUEST_ID = "clientRequestId";

    private NuveiSafechargeWrapper testObj;

    public static Collection<Object> getApiResponseStatus() {
        return Arrays.stream(Constants.APIResponseStatus.values())
                .filter(apiResponseStatus -> !apiResponseStatus.equals(Constants.APIResponseStatus.SUCCESS))
                .collect(Collectors.toSet());
    }

    @Mock
    protected Safecharge safechargeMock;
    @Mock
    protected ServiceFactory serviceFactoryMock;
    @Mock
    protected SafechargeRequestExecutor requestExecutorMock;
    @Mock
    private SettleTransactionRequest settleTransactionRequestMock;
    @Mock
    private UserAddress billingAddressMock;
    @Mock
    private UserAddress shippingAddressMock;
    @Mock
    private PaymentMethod paymentMethodMock;
    @Mock
    private RequestBuilder requestBuilderMock;
    @Mock
    private SafechargeBaseRequest baseRequestMock;
    @Mock
    private OpenOrderResponse openOrderResponseMock;
    @Mock
    private SafechargeResponse getSessionTokenResponseMock;
    @Mock
    private GetMerchantPaymentMethodsResponse getMerchantPaymentMethodsResponse;
    @Mock
    private SettleTransactionResponse settleTransactionResponseMock;

    final NuveiMerchantConfigurationModel merchantConfiguration = new NuveiMerchantConfigurationModel();

    @Before
    public void setUp() throws SafechargeException {
        MockitoAnnotations.initMocks(this);
        merchantConfiguration.setHashAlgorithm(NuveiHashAlgorithm.SHA256);
        merchantConfiguration.setMerchantSecretKey(MERCHANT_SECRET_ID);
        merchantConfiguration.setMerchantSiteId(MERCHANT_SITE_ID);
        merchantConfiguration.setMerchantId(MERCHANT_ID);
        merchantConfiguration.setServerHost(SERVER_HOST);

        testObj = new NuveiSafechargeWrapper(merchantConfiguration);

        testObj.requestExecutor = requestExecutorMock;
        testObj.serviceFactory = serviceFactoryMock;
        testObj.safecharge = safechargeMock;

        when(serviceFactoryMock.getRequestBuilder()).thenReturn(requestBuilderMock);
        when(requestBuilderMock.getSessionTokenRequest(any())).thenReturn(baseRequestMock);
        when(requestExecutorMock.execute(baseRequestMock)).thenReturn(getSessionTokenResponseMock);
        when(getSessionTokenResponseMock.getStatus()).thenReturn(Constants.APIResponseStatus.SUCCESS);
        when(getSessionTokenResponseMock.getSessionToken()).thenReturn(SESSION_TOKEN);
    }

    @Test
    public void getMerchantPaymentMethods_ShouldReturnPaymentMethods() throws SafechargeException {
        when(requestExecutorMock.execute(any()))
                .thenReturn(getSessionTokenResponseMock)
                .thenReturn(getMerchantPaymentMethodsResponse);
        when(getMerchantPaymentMethodsResponse.getPaymentMethods()).thenReturn(List.of(paymentMethodMock));

        final List<PaymentMethod> result = testObj.getMerchantPaymentMethods();

        assertThat(result).containsExactly(paymentMethodMock);
    }

    @Test
    public void createMerchantInfo_ShouldCreateMerchantInfo() {
        final MerchantInfo result = testObj.createMerchantInfo(merchantConfiguration);

        assertThat(result.getHashAlgorithm()).isEqualTo(Constants.HashAlgorithm.SHA256);
        assertThat(result.getMerchantId()).isEqualTo(MERCHANT_ID);
        assertThat(result.getMerchantKey()).isEqualTo(MERCHANT_SECRET_ID);
        assertThat(result.getServerHost()).isEqualTo(SERVER_HOST);
        assertThat(result.getMerchantSiteId()).isEqualTo(MERCHANT_SITE_ID);
    }

    @Test
    public void initialize_ShouldInitializeTheSessionToken() throws SafechargeException {
        testObj.initialize();

        assertThat(testObj.sessionToken).isEqualTo(SESSION_TOKEN);
    }

    @Test
    public void getSessionToken_ShouldReturnSessionToken_WhenResponseStatusIsNotError() throws SafechargeException {
        when(getSessionTokenResponseMock.getStatus()).thenReturn(Constants.APIResponseStatus.SUCCESS);

        final String result = testObj.getSessionToken();

        assertThat(result).isEqualTo(SESSION_TOKEN);
    }

    @Test(expected = SafechargeConfigurationException.class)
    public void getSessionToken_ShouldThrowException_WhenResponseStatusIsError() throws SafechargeException {
        when(getSessionTokenResponseMock.getStatus()).thenReturn(Constants.APIResponseStatus.ERROR);

        testObj.getSessionToken();
    }

    @Test
    public void ensureMerchantInfoAndSessionTokenNotNull_ShouldDoNothing_WhenMerchantInfoAndSessionTokenAreSet() {
        testObj.sessionToken = SESSION_TOKEN;

        testObj.ensureMerchantInfoAndSessionTokenNotNull();
    }

    @Test(expected = SafechargeConfigurationException.class)
    public void ensureMerchantInfoAndSessionTokenNotNull_ShouldDoNothing_WhenSessionTokenIsNotSet() {
        testObj.sessionToken = null;

        testObj.ensureMerchantInfoAndSessionTokenNotNull();
    }

    @Test(expected = SafechargeConfigurationException.class)
    public void ensureMerchantInfoAndSessionTokenNotNull_ShouldDoNothing_WhenMerchantInfoIsNotSet() {
        testObj.sessionToken = SESSION_TOKEN;
        testObj.merchantInfo = null;

        testObj.ensureMerchantInfoAndSessionTokenNotNull();
    }

    @Test
    public void openOrder_ShouldGetSessionToken_WhenRequestIsSuccess() throws SafechargeException {
        when(safechargeMock.openOrder(USER_TOKEN_ID, null, CLIENT_UNIQUE_ID,
                null, null, null, null, CURRENCY_ISO_CODE, AMOUNT,
                null, null, null, shippingAddressMock, billingAddressMock, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null, null))
                .thenReturn(openOrderResponseMock);
        when(openOrderResponseMock.getStatus()).thenReturn(Constants.APIResponseStatus.SUCCESS);
        when(openOrderResponseMock.getSessionToken()).thenReturn(SESSION_TOKEN);

        final String result = testObj
                .openOrder(USER_TOKEN_ID, CURRENCY_ISO_CODE, AMOUNT, billingAddressMock, shippingAddressMock, CLIENT_UNIQUE_ID);

        assertThat(result).isEqualTo(SESSION_TOKEN);
    }

    @Test(expected = SafechargeException.class)
    @Parameters(method = "getApiResponseStatus")
    public void openOrder_ShouldThrowException_WhenRequestIsNotSuccess(final Constants.APIResponseStatus status) throws SafechargeException {
        when(safechargeMock.openOrder(USER_TOKEN_ID, null, CLIENT_UNIQUE_ID,
                null, null, null, null, CURRENCY_ISO_CODE, AMOUNT,
                null, null, null, shippingAddressMock, billingAddressMock, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null, null))
                .thenReturn(openOrderResponseMock);
        when(openOrderResponseMock.getStatus()).thenReturn(status);
        when(openOrderResponseMock.getErrorType()).thenReturn(Constants.ErrorType.INVALID_REQUEST);
        when(openOrderResponseMock.getSessionToken()).thenReturn(SESSION_TOKEN);

        testObj.openOrder(USER_TOKEN_ID, CURRENCY_ISO_CODE, AMOUNT, billingAddressMock, shippingAddressMock, CLIENT_UNIQUE_ID);
    }

    @Test
    public void createSettleTransaction_ShouldCreateSettleRequest() throws SafechargeException {
        testObj.sessionToken = SESSION_TOKEN;
        testObj.createSettleTransaction(CURRENCY_ISO_CODE, AMOUNT, CLIENT_UNIQUE_ID, RELATED_TRANSACTION_ID);
        verify(serviceFactoryMock).getRequestBuilder();
        when(serviceFactoryMock.getRequestBuilder()).thenReturn(requestBuilderMock);
        verify(requestBuilderMock).getSettleTransactionRequest(SESSION_TOKEN, testObj.merchantInfo,
                CLIENT_UNIQUE_ID, null, null, null, null, null,
                AMOUNT, null, null, null, CURRENCY_ISO_CODE, null, null, RELATED_TRANSACTION_ID, null);
    }

    @Test
    public void createRefundTransaction_ShouldCreateRefundRequest() throws SafechargeException {
        testObj.sessionToken = SESSION_TOKEN;
        testObj.createRefundTransaction(CURRENCY_ISO_CODE, AMOUNT, CLIENT_UNIQUE_ID, RELATED_TRANSACTION_ID, CLIENT_REQUEST_ID);
        verify(serviceFactoryMock).getRequestBuilder();
        when(serviceFactoryMock.getRequestBuilder()).thenReturn(requestBuilderMock);
        verify(requestBuilderMock).getRefundTransactionRequest(SESSION_TOKEN, testObj.merchantInfo,
                CLIENT_UNIQUE_ID, CLIENT_REQUEST_ID, null, AMOUNT, null, null, CURRENCY_ISO_CODE,
                null, null, null, RELATED_TRANSACTION_ID, null);
    }

    @Test
    public void executeSettleTransaction_ShouldExecuteSettleRequest() throws SafechargeException {
        testObj.executeTransaction(settleTransactionRequestMock);
        verify(requestExecutorMock).execute(settleTransactionRequestMock);
    }

    @Test
    public void initializeSafecharge_ShouldCallSafechargeInitialize() throws SafechargeException {
        testObj.initializeSafecharge();

        verify(safechargeMock).initialize(MERCHANT_ID, MERCHANT_SITE_ID, MERCHANT_SECRET_ID, SERVER_HOST, Constants.HashAlgorithm.SHA256);
    }
}
