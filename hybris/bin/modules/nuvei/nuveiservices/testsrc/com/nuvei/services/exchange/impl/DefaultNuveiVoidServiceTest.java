package com.nuvei.services.exchange.impl;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.services.enums.NuveiHashAlgorithm;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import com.nuvei.services.wrapper.NuveiSafechargeWrapper;
import com.safecharge.exception.SafechargeException;
import com.safecharge.request.VoidTransactionRequest;
import com.safecharge.response.SafechargeResponse;
import com.safecharge.response.VoidTransactionResponse;
import com.safecharge.util.Constants;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
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

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiVoidServiceTest {

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

    @Spy
    @InjectMocks
    private DefaultNuveiVoidService testObj;

    @Mock
    private NuveiMerchantConfigurationModel nuveiMerchantConfigurationModelMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private PaymentTransactionModel paymentTransactionModelMock, paymentTransactionModelTwoMock;
    @Mock
    private AbstractOrderModel abstractOrderModelMock;
    @Mock
    private OrderModel orderModelMock;
    @Mock
    private BaseSiteModel baseSiteModelMock;
    @Mock
    private PaymentTransactionEntryModel paymentTransactionEntryModelMock;
    @Mock
    private SafechargeResponse getSessionTokenResponseMock;
    @Mock
    private NuveiSafechargeWrapper nuveiSafechargeWrapperMock;
    @Mock
    private VoidTransactionRequest voidTransactionRequestMock;
    @Mock
    private VoidTransactionResponse voidTransactionResponseMock;
    @Mock
    private NuveiPaymentTransactionService nuveiPaymentTransactionServiceMock;

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
        when(orderModelMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransactionModelMock));
        when(orderModelMock.getSite()).thenReturn(baseSiteModelMock);
        when(orderModelMock.getClientUniqueId()).thenReturn(CLIENT_UNIQUE_ID);
        when(baseSiteModelMock.getNuveiMerchantConfiguration()).thenReturn(nuveiMerchantConfigurationModelMock);
        when(paymentTransactionEntryModelMock.getPaymentTransaction()).thenReturn(paymentTransactionModelMock);
        when(paymentTransactionEntryModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(ISO_CODE);
        when(paymentTransactionEntryModelMock.getAmount()).thenReturn(new BigDecimal(AMOUNT));
        when(paymentTransactionEntryModelMock.getRequestId()).thenReturn(REQUEST_ID);
        when(paymentTransactionEntryModelMock.getPaymentTransaction()).thenReturn(paymentTransactionModelMock);
        when(paymentTransactionModelMock.getOrder()).thenReturn(orderModelMock);
        when(paymentTransactionModelMock.getEntries()).thenReturn(Collections.singletonList(paymentTransactionEntryModelMock));
        when(paymentTransactionEntryModelMock.getType()).thenReturn(PaymentTransactionType.AUTHORIZATION);

        doNothing().when(testObj).executeRequest(orderModelMock, ISO_CODE,
                TOTAL_PRICE, nuveiMerchantConfigurationModelMock, REQUEST_ID, CLIENT_UNIQUE_ID);

        doReturn(nuveiSafechargeWrapperMock).when(testObj).getNuveiSafechargeWrapper(nuveiMerchantConfigurationModelMock);


        when(nuveiSafechargeWrapperMock.createVoidTransaction(ISO_CODE, new BigDecimal(AMOUNT).toString(), CLIENT_UNIQUE_ID, REQUEST_ID))
                .thenReturn(voidTransactionRequestMock);

        testObj.requestSafechargeTransaction(orderModelMock);

        verify(nuveiPaymentTransactionServiceMock).createPaymentTransactionEntry(paymentTransactionModelMock,
                NuveiTransactionStatus.PENDING.getCode(), PaymentTransactionType.VOID);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void requestSafechargeTransaction_ShouldThrowException_WhenOrderHasNoPaymentTransactions() throws SafechargeException {
        testObj.requestSafechargeTransaction(orderModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestSafechargeTransaction_ShouldThrowException_WhenOrderIsNull() throws SafechargeException {
        testObj.requestSafechargeTransaction(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void requestSafechargeTransaction_ShouldThrowException_WhenOrderHasMoreThantOnePaymentTransactions() throws SafechargeException {
        when(orderModelMock.getPaymentTransactions()).thenReturn(List.of(paymentTransactionModelMock, paymentTransactionModelTwoMock));
        testObj.requestSafechargeTransaction(orderModelMock);
    }

    @Test
    public void isValidTransactionType_shouldReturnFalse_whenIsDifferentFromAUTHORIZATION() {
        when(paymentTransactionEntryModelMock.getType()).thenReturn(PaymentTransactionType.SALE);

        final boolean result = testObj.isValidTransactionType(paymentTransactionEntryModelMock);

        assertThat(result).isFalse();
    }

    @Test
    public void isValidTransactionType_shouldReturnFalse_whenIsEqualsToAUTHORIZATION() {
        when(paymentTransactionEntryModelMock.getType()).thenReturn(PaymentTransactionType.AUTHORIZATION);

        final boolean result = testObj.isValidTransactionType(paymentTransactionEntryModelMock);

        assertThat(result).isTrue();
    }

    @Test
    public void requestSafechargeTransaction_shouldExecuteRequest() throws SafechargeException {
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
        doNothing().when(testObj).storeSafechargeRequest(orderModelMock, voidTransactionRequestMock);
        when(nuveiSafechargeWrapperMock.executeTransaction(voidTransactionRequestMock)).thenReturn(voidTransactionResponseMock);
        when(voidTransactionResponseMock.getStatus()).thenReturn(Constants.APIResponseStatus.SUCCESS);
        doNothing().when(testObj).storeSafechargeResponse(orderModelMock, voidTransactionResponseMock);
        when(nuveiSafechargeWrapperMock.createVoidTransaction(ISO_CODE, new BigDecimal(AMOUNT).toString(), CLIENT_UNIQUE_ID, REQUEST_ID))
                .thenReturn(voidTransactionRequestMock);

        testObj.executeRequest(orderModelMock, paymentTransactionEntryModelMock.getCurrency().getIsocode(), paymentTransactionEntryModelMock.getAmount().toString(),
                nuveiMerchantConfigurationModelMock, paymentTransactionEntryModelMock.getRequestId(),
                paymentTransactionEntryModelMock.getPaymentTransaction().getOrder().getClientUniqueId());

        verify(nuveiSafechargeWrapperMock).createVoidTransaction(ISO_CODE, new BigDecimal(AMOUNT).toString(), CLIENT_UNIQUE_ID, REQUEST_ID);
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
        doNothing().when(testObj).storeSafechargeRequest(orderModelMock, voidTransactionRequestMock);
        when(nuveiSafechargeWrapperMock.executeTransaction(voidTransactionRequestMock)).thenReturn(voidTransactionResponseMock);
        when(voidTransactionResponseMock.getStatus()).thenReturn(Constants.APIResponseStatus.ERROR);
        when(voidTransactionResponseMock.getErrorType()).thenReturn(Constants.ErrorType.INVALID_REQUEST);
        doNothing().when(testObj).storeSafechargeResponse(orderModelMock, voidTransactionResponseMock);
        when(nuveiSafechargeWrapperMock.createVoidTransaction(ISO_CODE, new BigDecimal(AMOUNT).toString(), CLIENT_UNIQUE_ID, REQUEST_ID))
                .thenReturn(voidTransactionRequestMock);

        testObj.executeRequest(orderModelMock, paymentTransactionEntryModelMock.getCurrency().getIsocode(), paymentTransactionEntryModelMock.getAmount().toString(),
                nuveiMerchantConfigurationModelMock, paymentTransactionEntryModelMock.getRequestId(),
                paymentTransactionEntryModelMock.getPaymentTransaction().getOrder().getClientUniqueId());

        verify(nuveiSafechargeWrapperMock).createVoidTransaction(ISO_CODE, new BigDecimal(AMOUNT).toString(), CLIENT_UNIQUE_ID, REQUEST_ID);
    }
}
