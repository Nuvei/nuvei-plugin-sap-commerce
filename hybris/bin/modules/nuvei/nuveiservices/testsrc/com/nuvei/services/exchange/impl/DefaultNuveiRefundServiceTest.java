package com.nuvei.services.exchange.impl;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.services.enums.NuveiHashAlgorithm;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.model.NuveiPayloadModel;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import com.nuvei.services.wrapper.NuveiSafechargeWrapper;
import com.safecharge.exception.SafechargeException;
import com.safecharge.request.RefundTransactionRequest;
import com.safecharge.response.RefundTransactionResponse;
import com.safecharge.response.SafechargeResponse;
import com.safecharge.util.Constants;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiRefundServiceTest {

    private static final String CLIENT_UNIQUE_ID = "clientUniqueId";
    private static final double AMOUNT = 12.00;
    private static final String ISO_CODE = "isoCode";
    private static final String REQUEST_ID = "requestId";
    private static final String HOST = "host";
    private static final String SECRET_KEY = "secretKey";
    private static final String SITE = "site";
    private static final String MERCHANT_ID = "merchantId";
    private static final String SESSION_TOKEN = "sessionToken";
    private static final String TOTAL_PRICE = "12";
    private static final String CLIENT_REQUEST_ID = "clientRequestId";
    private static final String RELATED_TRANSACTION_ID = "relatedTransactionId";
    private static final String STATUS = "status";
    private static final String REQUEST_CODE = "requestCode";

    @Spy
    @InjectMocks
    private DefaultNuveiRefundService testObj;

    @Mock
    private NuveiMerchantConfigurationModel nuveiMerchantConfigurationModelMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private TimeService timeService;
    @Mock
    private NuveiPaymentTransactionService nuveiPaymentTransactionService;

    @Mock
    private PaymentTransactionModel paymentTransactionModelMock, paymentTransactionModelTwoMock;
    @Mock
    private AbstractOrderModel abstractOrderModelMock;
    @Mock
    private OrderModel orderModelMock;
    @Mock
    private BaseSiteModel baseSiteModelMock;
    @Mock
    private PaymentTransactionEntryModel paymentTransactionEntryModelMock, paymentTransactionEntryModelTwoMock;
    @Mock
    private SafechargeResponse getSessionTokenResponseMock;
    @Mock
    private NuveiSafechargeWrapper nuveiSafechargeWrapperMock;
    @Mock
    private RefundTransactionRequest refundTransactionRequestMock;
    @Mock
    private RefundTransactionResponse refundTransactionResponseMock;
    @Mock
    private ReturnRequestModel returnRequestModelMock, returnRequestModelTwoMock;
    @Mock
    private NuveiPayloadModel nuveiPayloadModelMock;

    @Before
    public void setUp() throws Exception {
        when(nuveiMerchantConfigurationModelMock.getHashAlgorithm()).thenReturn(NuveiHashAlgorithm.SHA256);
        when(nuveiMerchantConfigurationModelMock.getMerchantId()).thenReturn(MERCHANT_ID);
        when(nuveiMerchantConfigurationModelMock.getMerchantSiteId()).thenReturn(SITE);
        when(nuveiMerchantConfigurationModelMock.getMerchantSecretKey()).thenReturn(SECRET_KEY);
        when(nuveiMerchantConfigurationModelMock.getServerHost()).thenReturn(HOST);
        when(getSessionTokenResponseMock.getSessionToken()).thenReturn(SESSION_TOKEN);
    }

    @Test
    public void requestSafechargeTransaction_ShouldNoThrowException_WhenPaymentTransactionEntryModelIsAuthorized() throws SafechargeException {
        when(orderModelMock.getTotalPrice()).thenReturn(12.00);
        when(orderModelMock.getReturnRequests()).thenReturn(Collections.singletonList(returnRequestModelMock));
        when(returnRequestModelMock.getOrder()).thenReturn(orderModelMock);

        doReturn(paymentTransactionEntryModelTwoMock).when(testObj).requestSafechargeTransaction(
                returnRequestModelMock, new BigDecimal(12.00));

        final PaymentTransactionEntryModel result = testObj.requestSafechargeTransaction(orderModelMock);

        assertThat(result).isEqualTo(paymentTransactionEntryModelTwoMock);
    }

    @Test
    public void requestSafechargeTransaction_ShouldThrowException_WhenOrderHasNoReturnRequest() throws SafechargeException {
        final PaymentTransactionEntryModel result = testObj.requestSafechargeTransaction(orderModelMock);

        assertThat(result).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestSafechargeTransaction_ShouldThrowException_WhenOrderIsNull() throws SafechargeException {
        testObj.requestSafechargeTransaction(null);
    }

    @Test
    public void requestSafechargeTransaction_ShouldThrowException_WhenOrderHasMoreThantOnePaymentTransactions() throws SafechargeException {
        when(orderModelMock.getReturnRequests()).thenReturn(List.of(returnRequestModelMock, returnRequestModelTwoMock));

        final PaymentTransactionEntryModel result = testObj.requestSafechargeTransaction(orderModelMock);

        assertThat(result).isNull();
    }

    @Test
    public void isValidTransactionType_shouldReturnFalse_whenIsDifferentFromCAPTURE() {
        when(paymentTransactionEntryModelMock.getType()).thenReturn(PaymentTransactionType.SALE);

        final boolean result = testObj.isValidTransactionType(paymentTransactionEntryModelMock);

        assertThat(result).isFalse();
    }

    @Test
    public void isValidTransactionType_shouldReturnTrue_whenIsEqualsToCAPTURE() {
        when(paymentTransactionEntryModelMock.getType()).thenReturn(PaymentTransactionType.CAPTURE);
        when(paymentTransactionEntryModelMock.getTransactionStatus()).thenReturn(NuveiTransactionStatus.APPROVED.getCode());

        final boolean result = testObj.isValidTransactionType(paymentTransactionEntryModelMock);

        assertThat(result).isTrue();
    }

    @Test
    public void requestSafechargeTransaction_shouldExecuteRequest() throws SafechargeException {
        doReturn(nuveiSafechargeWrapperMock).when(testObj).getNuveiSafechargeWrapper(nuveiMerchantConfigurationModelMock);

        doReturn(refundTransactionRequestMock).when(nuveiSafechargeWrapperMock).createRefundTransaction(
                ISO_CODE, TOTAL_PRICE, CLIENT_UNIQUE_ID, RELATED_TRANSACTION_ID, CLIENT_REQUEST_ID);

        doNothing().when(testObj).storeSafechargeRequest(orderModelMock, refundTransactionRequestMock);

        when(currencyModelMock.getIsocode()).thenReturn(ISO_CODE);
        when(paymentTransactionEntryModelMock.getPaymentTransaction()).thenReturn(paymentTransactionModelMock);
        when(paymentTransactionEntryModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(paymentTransactionEntryModelMock.getAmount()).thenReturn(new BigDecimal(AMOUNT));
        when(paymentTransactionEntryModelMock.getRequestId()).thenReturn(REQUEST_ID);
        when(paymentTransactionModelMock.getOrder()).thenReturn(abstractOrderModelMock);
        when(abstractOrderModelMock.getClientUniqueId()).thenReturn(CLIENT_UNIQUE_ID);
        doReturn(refundTransactionResponseMock).when(nuveiSafechargeWrapperMock)
                .executeTransaction(refundTransactionRequestMock);
        when(refundTransactionResponseMock.getStatus()).thenReturn(Constants.APIResponseStatus.SUCCESS);
        doNothing().when(testObj).storeSafechargeResponse(orderModelMock, refundTransactionResponseMock);

        when(modelServiceMock.create(NuveiPayloadModel.class)).thenReturn(nuveiPayloadModelMock);

        testObj.executeRequest(orderModelMock, paymentTransactionEntryModelMock.getCurrency().getIsocode(), paymentTransactionEntryModelMock.getAmount().toString(),
                nuveiMerchantConfigurationModelMock, RELATED_TRANSACTION_ID,
                paymentTransactionEntryModelMock.getPaymentTransaction().getOrder().getClientUniqueId(), CLIENT_REQUEST_ID);

        verify(nuveiSafechargeWrapperMock).createRefundTransaction(ISO_CODE, TOTAL_PRICE,
                CLIENT_UNIQUE_ID, RELATED_TRANSACTION_ID, CLIENT_REQUEST_ID);
    }

    @Test(expected = SafechargeException.class)
    public void requestSafechargeTransaction_shouldThrowException_WhenResponseStatusIsError() throws SafechargeException {
        when(currencyModelMock.getIsocode()).thenReturn(ISO_CODE);
        when(paymentTransactionEntryModelMock.getPaymentTransaction()).thenReturn(paymentTransactionModelMock);
        when(paymentTransactionEntryModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(paymentTransactionEntryModelMock.getAmount()).thenReturn(new BigDecimal(AMOUNT));
        when(paymentTransactionEntryModelMock.getRequestId()).thenReturn(REQUEST_ID);
        when(paymentTransactionModelMock.getOrder()).thenReturn(abstractOrderModelMock);
        when(abstractOrderModelMock.getClientUniqueId()).thenReturn(CLIENT_UNIQUE_ID);
        when(abstractOrderModelMock.getSite()).thenReturn(baseSiteModelMock);
        when(baseSiteModelMock.getNuveiMerchantConfiguration()).thenReturn(nuveiMerchantConfigurationModelMock);
        doReturn(nuveiSafechargeWrapperMock).when(testObj).getNuveiSafechargeWrapper(nuveiMerchantConfigurationModelMock);
        doNothing().when(testObj).storeSafechargeRequest(orderModelMock, refundTransactionRequestMock);
        when(nuveiSafechargeWrapperMock.executeTransaction(refundTransactionRequestMock)).thenReturn(refundTransactionResponseMock);
        when(refundTransactionResponseMock.getStatus()).thenReturn(Constants.APIResponseStatus.ERROR);
        when(refundTransactionResponseMock.getErrorType()).thenReturn(Constants.ErrorType.INVALID_REQUEST);
        doNothing().when(testObj).storeSafechargeResponse(orderModelMock, refundTransactionResponseMock);
        when(nuveiSafechargeWrapperMock.createRefundTransaction(ISO_CODE, new BigDecimal(AMOUNT).toString(), CLIENT_UNIQUE_ID, RELATED_TRANSACTION_ID, CLIENT_REQUEST_ID))
                .thenReturn(refundTransactionRequestMock);

        testObj.executeRequest(orderModelMock, paymentTransactionEntryModelMock.getCurrency().getIsocode(), paymentTransactionEntryModelMock.getAmount().toString(),
                nuveiMerchantConfigurationModelMock, RELATED_TRANSACTION_ID,
                paymentTransactionEntryModelMock.getPaymentTransaction().getOrder().getClientUniqueId(), CLIENT_REQUEST_ID);

        verify(nuveiSafechargeWrapperMock).createRefundTransaction(ISO_CODE, new BigDecimal(AMOUNT).toString(),
                CLIENT_UNIQUE_ID, RELATED_TRANSACTION_ID, CLIENT_REQUEST_ID);
    }

    @Test
    public void requestSafechargeTransaction_shouldReturnPaymentTransactionEntryError_whenSafechargeExceptionisThrown() throws SafechargeException {
        when(returnRequestModelMock.getOrder()).thenReturn(orderModelMock);
        when(returnRequestModelMock.getCode()).thenReturn(REQUEST_CODE);
        when(orderModelMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransactionModelMock));
        when(paymentTransactionModelMock.getEntries()).thenReturn(Collections.singletonList(paymentTransactionEntryModelMock));
        doReturn(Boolean.TRUE).when(testObj).isValidTransactionType(paymentTransactionEntryModelMock);
        when(orderModelMock.getSite()).thenReturn(baseSiteModelMock);
        when(orderModelMock.getClientUniqueId()).thenReturn(CLIENT_UNIQUE_ID);
        when(baseSiteModelMock.getNuveiMerchantConfiguration()).thenReturn(nuveiMerchantConfigurationModelMock);
        when(paymentTransactionEntryModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(paymentTransactionEntryModelMock.getRequestId()).thenReturn(REQUEST_CODE);
        when(currencyModelMock.getIsocode()).thenReturn(ISO_CODE);
        when(paymentTransactionEntryModelMock.getPaymentTransaction()).thenReturn(paymentTransactionModelMock);
        doThrow(SafechargeException.class).when(testObj).executeRequest(orderModelMock, ISO_CODE, new BigDecimal(AMOUNT).toString(),
                nuveiMerchantConfigurationModelMock,REQUEST_CODE,CLIENT_UNIQUE_ID,REQUEST_CODE);

        doReturn(paymentTransactionEntryModelTwoMock).when(testObj)
                .createPaymentTransactionEntry(paymentTransactionModelMock,returnRequestModelMock,NuveiTransactionStatus.ERROR.getCode());

        final PaymentTransactionEntryModel result = testObj.requestSafechargeTransaction(returnRequestModelMock, new BigDecimal(AMOUNT));

        assertThat(result).isNotNull();
    }

    @Test
    public void requestSafechargeTransaction_shouldReturnPaymentTransactionEntryModel() throws SafechargeException {
        when(returnRequestModelMock.getOrder()).thenReturn(orderModelMock);
        when(returnRequestModelMock.getCode()).thenReturn(REQUEST_CODE);
        when(orderModelMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransactionModelMock));
        when(paymentTransactionModelMock.getEntries()).thenReturn(Collections.singletonList(paymentTransactionEntryModelMock));
        doReturn(Boolean.TRUE).when(testObj).isValidTransactionType(paymentTransactionEntryModelMock);
        when(orderModelMock.getSite()).thenReturn(baseSiteModelMock);
        when(orderModelMock.getClientUniqueId()).thenReturn(CLIENT_UNIQUE_ID);
        when(baseSiteModelMock.getNuveiMerchantConfiguration()).thenReturn(nuveiMerchantConfigurationModelMock);
        when(paymentTransactionEntryModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(paymentTransactionEntryModelMock.getRequestId()).thenReturn(REQUEST_CODE);
        when(currencyModelMock.getIsocode()).thenReturn(ISO_CODE);
        when(paymentTransactionEntryModelMock.getPaymentTransaction()).thenReturn(paymentTransactionModelMock);
        doNothing().when(testObj).executeRequest(orderModelMock, ISO_CODE, new BigDecimal(AMOUNT).toString(),
                nuveiMerchantConfigurationModelMock,REQUEST_CODE,CLIENT_UNIQUE_ID,REQUEST_CODE);
        doReturn(paymentTransactionEntryModelTwoMock).when(testObj)
                .createPaymentTransactionEntry(paymentTransactionModelMock,returnRequestModelMock,NuveiTransactionStatus.PENDING.getCode());

        final PaymentTransactionEntryModel result = testObj.requestSafechargeTransaction(returnRequestModelMock, new BigDecimal(AMOUNT));

        assertThat(result).isNotNull();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void requestSafechargeTransaction_shouldThrowException_whenPaymentTransactionHasNoEntries() {
        when(returnRequestModelMock.getOrder()).thenReturn(orderModelMock);
        when(orderModelMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransactionModelMock));

        testObj.requestSafechargeTransaction(returnRequestModelMock, new BigDecimal(AMOUNT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestSafechargeTransaction_shouldThrowException_whenReturnRequestIsNUll() {
        testObj.requestSafechargeTransaction(null, new BigDecimal(AMOUNT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestSafechargeTransaction_shouldThrowException_whenOrderIsNUll() {
        when(returnRequestModelMock.getOrder()).thenReturn(null);

        testObj.requestSafechargeTransaction(returnRequestModelMock, new BigDecimal(AMOUNT));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void requestSafechargeTransaction_shouldThrowException_whenOrderHasMoreThanOneTransaction() {
        when(returnRequestModelMock.getOrder()).thenReturn(orderModelMock);
        when(orderModelMock.getPaymentTransactions()).thenReturn(List.of(paymentTransactionModelMock,paymentTransactionModelTwoMock));

        testObj.requestSafechargeTransaction(returnRequestModelMock, new BigDecimal(AMOUNT));
    }

    @Test
    public void createPaymentTransactionEntry_shoudlReturnPaymentTransactionEntry() {

        when(nuveiPaymentTransactionService
                .createPaymentTransactionEntry(paymentTransactionModelMock, STATUS, PaymentTransactionType.REFUND_FOLLOW_ON))
                .thenReturn(paymentTransactionEntryModelTwoMock);
        when(returnRequestModelMock.getTotalTax()).thenReturn(new BigDecimal(AMOUNT));
        when(returnRequestModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(returnRequestModelMock.getCode()).thenReturn(REQUEST_CODE);

        final PaymentTransactionEntryModel result = testObj.createPaymentTransactionEntry(paymentTransactionModelMock, returnRequestModelMock, STATUS);

        assertThat(result.getAmount()).isEqualTo(paymentTransactionEntryModelTwoMock.getAmount());
        assertThat(result.getCurrency()).isEqualTo(paymentTransactionEntryModelTwoMock.getCurrency());
        assertThat(result.getClientRequestId()).isEqualTo(paymentTransactionEntryModelTwoMock.getClientRequestId());
    }
}
