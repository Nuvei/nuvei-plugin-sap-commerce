package com.nuvei.returns.actions;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.services.exchange.NuveiRefundService;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
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
public class NuveiCaptureRefundActionTest {

    private static final String CLIENT_REQUEST_ID = "clientRequestId";
    private static final String WAIT = "WAIT";
    private static final String NOK = "NOK";
    private static final String OK = "OK";

    private static final int AMOUNT = 12;
    private static final int DIGITS = 2;

    @Spy
    @InjectMocks
    private NuveiCaptureRefundAction testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private NuveiPaymentTransactionService nuveiPaymentTransactionServiceMock;
    @Mock
    private NuveiRefundService nuveiExchangeServiceMock;

    @Mock
    private OrderModel orderModelMock;
    @Mock
    private PaymentTransactionModel paymentTransactionModelMock;
    @Mock
    private ReturnRequestModel returnRequestModelMock;
    @Mock
    private PaymentTransactionEntryModel paymentTransactionEntryModelMock,  paymentTransactionEntryModelTwoMock;

    private ReturnRequestModel returnRequestModelStub = new ReturnRequestModel();
    private OrderModel orderModelStub = new OrderModel();
    private CurrencyModel currencyModelStub = new CurrencyModel();
    private RefundEntryModel returnEntryModelStub, returnEntryModelTwoStub;
    private PaymentTransactionModel paymentTransactionModelStub = new PaymentTransactionModel();
    private PaymentTransactionEntryModel paymentTransactionEntryModelStub = new PaymentTransactionEntryModel();
    private ReturnProcessModel returnProcessModelStub = new ReturnProcessModel();

    @Before
    public void setUp() throws Exception {
        testObj.setModelService(modelServiceMock);
        returnEntryModelStub = new RefundEntryModel();
        returnEntryModelTwoStub = new RefundEntryModel();
    }

    @Test
    public void execute_shouldReturnWAIT() {
        returnProcessModelStub.setReturnRequest(returnRequestModelMock);
        when(returnRequestModelMock.getOrder()).thenReturn(orderModelMock);
        when(orderModelMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransactionModelMock));
        doReturn(paymentTransactionModelMock).when(nuveiPaymentTransactionServiceMock).getPaymentTransaction(orderModelMock);
        doReturn(Boolean.FALSE).when(testObj).hasBeenRefunded(paymentTransactionModelMock, returnRequestModelMock);
        doNothing().when(testObj).setReturnRequestStatus(returnRequestModelMock, ReturnStatus.PAYMENT_REVERSED);
        doReturn(new BigDecimal(AMOUNT)).when(testObj).getRefundAmount(returnRequestModelMock);
        doReturn(paymentTransactionEntryModelMock).when(testObj).findAcceptedEntry(paymentTransactionModelMock);

        when(nuveiExchangeServiceMock.requestSafechargeTransaction(returnRequestModelMock, new BigDecimal(AMOUNT))).thenReturn(paymentTransactionEntryModelTwoMock);
        when(paymentTransactionEntryModelTwoMock.getTransactionStatus()).thenReturn(NuveiTransactionStatus.PENDING.getCode());

        final String execute = testObj.execute(returnProcessModelStub);

        assertThat(execute).isEqualTo(WAIT);

        verify(nuveiExchangeServiceMock).requestSafechargeTransaction(returnRequestModelMock, new BigDecimal(AMOUNT));
    }

    @Test
    public void execute_shouldReturnNOK_whenAcceptedCaptureEntryIsNotFound() {
        returnProcessModelStub.setReturnRequest(returnRequestModelMock);
        when(returnRequestModelMock.getOrder()).thenReturn(orderModelMock);
        when(orderModelMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransactionModelMock));
        doReturn(paymentTransactionModelMock).when(nuveiPaymentTransactionServiceMock).getPaymentTransaction(orderModelMock);
        doReturn(Boolean.FALSE).when(testObj).hasBeenRefunded(paymentTransactionModelMock, returnRequestModelMock);
        doNothing().when(testObj).setReturnRequestStatus(returnRequestModelMock, ReturnStatus.PAYMENT_REVERSED);
        doReturn(new BigDecimal(AMOUNT)).when(testObj).getRefundAmount(returnRequestModelMock);
        doReturn(null).when(testObj).findAcceptedEntry(paymentTransactionModelMock);

        final String execute = testObj.execute(returnProcessModelStub);

        assertThat(execute).isEqualTo(NOK);
    }

    @Test
    public void execute_shouldReturnNOK_whenRefundHasBeenDeclined() {
        returnProcessModelStub.setReturnRequest(returnRequestModelMock);
        when(returnRequestModelMock.getOrder()).thenReturn(orderModelMock);
        when(orderModelMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransactionModelMock));
        doReturn(paymentTransactionModelMock).when(nuveiPaymentTransactionServiceMock).getPaymentTransaction(orderModelMock);
        doReturn(Boolean.FALSE).when(testObj).hasBeenRefunded(paymentTransactionModelMock, returnRequestModelMock);
        doReturn(Boolean.TRUE).when(testObj).hasBeenDeclined(paymentTransactionModelMock, returnRequestModelMock);

        final String execute = testObj.execute(returnProcessModelStub);

        assertThat(execute).isEqualTo(NOK);
    }

    @Test
    public void execute_shouldReturnOK_whenReturnHasBeenRefunded() {
        returnProcessModelStub.setReturnRequest(returnRequestModelMock);
        when(returnRequestModelMock.getOrder()).thenReturn(orderModelMock);
        when(orderModelMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransactionModelMock));

        doReturn(paymentTransactionModelMock).when(nuveiPaymentTransactionServiceMock).getPaymentTransaction(orderModelMock);
        doReturn(Boolean.TRUE).when(testObj).hasBeenRefunded(paymentTransactionModelMock, returnRequestModelMock);
        doNothing().when(testObj).setReturnRequestStatus(returnRequestModelMock, ReturnStatus.PAYMENT_REVERSED);

        final String execute = testObj.execute(returnProcessModelStub);

        assertThat(execute).isEqualTo(OK);
    }

    @Test
    public void execute_shouldReturnNOK_whenOrderHasNoPaymentsNotExist() {
        returnProcessModelStub.setReturnRequest(returnRequestModelStub);
        returnRequestModelStub.setOrder(orderModelStub);
        returnRequestModelStub.setReturnEntries(List.of(returnEntryModelStub,returnEntryModelTwoStub));

        final String execute = testObj.execute(returnProcessModelStub);

        assertThat(execute).isEqualTo(NOK);
    }

    @Test
    public void execute_shouldReturnNOK_whenOrderNotExist() {
        returnProcessModelStub.setReturnRequest(returnRequestModelStub);

        final String execute = testObj.execute(returnProcessModelStub);

        assertThat(execute).isEqualTo(NOK);
    }

    @Test
    public void hasBeenDeclined_shouldReturnTrue_whenTypeStatusAndClientRequestIdMatches() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.REFUND_FOLLOW_ON);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.ERROR.getCode());
        paymentTransactionEntryModelStub.setClientRequestId(CLIENT_REQUEST_ID);
        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));
        returnRequestModelStub.setCode(CLIENT_REQUEST_ID);

        final Boolean result = testObj.hasBeenDeclined(paymentTransactionModelStub, returnRequestModelStub);

        assertThat(result).isTrue();
    }

    @Test
    public void hasBeenDeclined_shouldReturnFalse_whenTypeStatusOrClientRequestIdNoMatches() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.REFUND_FOLLOW_ON);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
        paymentTransactionEntryModelStub.setClientRequestId(CLIENT_REQUEST_ID);
        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));
        returnRequestModelStub.setCode(CLIENT_REQUEST_ID);

        final Boolean result = testObj.hasBeenDeclined(paymentTransactionModelStub, returnRequestModelStub);

        assertThat(result).isFalse();
    }

    @Test
    public void hasBeenRefunded_shouldReturnTrue_whenTypeStatusAndClientRequestIdMatches() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.REFUND_FOLLOW_ON);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.SUCCESS.getCode());
        paymentTransactionEntryModelStub.setClientRequestId(CLIENT_REQUEST_ID);
        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));
        returnRequestModelStub.setCode(CLIENT_REQUEST_ID);

        final Boolean result = testObj.hasBeenRefunded(paymentTransactionModelStub, returnRequestModelStub);

        assertThat(result).isTrue();
    }

    @Test
    public void hasBeenRefunded_shouldReturnFalse_whenTypeStatusOrClientRequestIdNoMatches() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.REFUND_FOLLOW_ON);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
        paymentTransactionEntryModelStub.setClientRequestId(CLIENT_REQUEST_ID);
        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));
        returnRequestModelStub.setCode(CLIENT_REQUEST_ID);

        final Boolean result = testObj.hasBeenRefunded(paymentTransactionModelStub, returnRequestModelStub);

        assertThat(result).isFalse();
    }

    @Test
    public void findAcceptedEntry_shouldReturnPaymentTransactionEntryModel_whenStatusAndTypeMatches() {
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.CAPTURE);

        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));

        final PaymentTransactionEntryModel result = testObj.findAcceptedEntry(paymentTransactionModelStub);

        assertThat(result).isEqualTo(paymentTransactionEntryModelStub);
    }

    @Test
    public void findAcceptedEntry_shouldReturnNull_whenStatusOrTypeNotMatches() {
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);

        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));

        final PaymentTransactionEntryModel result = testObj.findAcceptedEntry(paymentTransactionModelStub);

        assertThat(result).isNull();
    }

    @Test
    public void getRefundAmount_shouldReturnAmount_whenRequestHasEntries() {
        currencyModelStub.setDigits(DIGITS);
        orderModelStub.setCurrency(currencyModelStub);
        orderModelStub.setDeliveryCost(6d);
        returnRequestModelStub.setOrder(orderModelStub);
        returnRequestModelStub.setReturnEntries(List.of(returnEntryModelStub,returnEntryModelTwoStub));
        returnRequestModelStub.setRefundDeliveryCost(Boolean.TRUE);
        returnEntryModelStub.setReturnRequest(returnRequestModelStub);
        returnEntryModelTwoStub.setReturnRequest(returnRequestModelStub);
        returnEntryModelStub.setAmount(new BigDecimal(12.00));
        returnEntryModelTwoStub.setAmount(new BigDecimal(12.00));

        final BigDecimal result = testObj.getRefundAmount(returnRequestModelStub);

        assertThat(result).isEqualTo(new BigDecimal(30.00).setScale(DIGITS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRefundAmount_shouldThrowException_whenReturnRequestHasNoEntries() {
        testObj.getRefundAmount(returnRequestModelStub);
    }

    @Test
    public void getRefundEntryAmount_shouldReturnEntryAmount() {
        currencyModelStub.setDigits(DIGITS);
        orderModelStub.setCurrency(currencyModelStub);
        returnRequestModelStub.setOrder(orderModelStub);
        returnEntryModelStub.setReturnRequest(returnRequestModelStub);
        returnEntryModelStub.setAmount(new BigDecimal(12));

        final BigDecimal result = testObj.getRefundEntryAmount(returnEntryModelStub);

        assertThat(result).isEqualTo(new BigDecimal(12.00).setScale(DIGITS));
    }

    @Test
    public void setReturnRequestStatus_shouldChangeStatusAndCallModelServiceSave() {

        returnRequestModelStub.setReturnEntries(List.of(returnEntryModelStub, returnEntryModelTwoStub));

        testObj.setReturnRequestStatus(returnRequestModelStub, ReturnStatus.PAYMENT_REVERSAL_FAILED);

        assertThat(returnEntryModelStub.getStatus()).isEqualTo(ReturnStatus.PAYMENT_REVERSAL_FAILED);
        assertThat(returnEntryModelTwoStub.getStatus()).isEqualTo(ReturnStatus.PAYMENT_REVERSAL_FAILED);

        verify(modelServiceMock).save(returnEntryModelStub);
        verify(modelServiceMock).save(returnEntryModelTwoStub);
        verify(modelServiceMock).save(returnEntryModelStub);
    }

    @Test
    public void getNumberOfDigits_shouldReturnDigitsOfCurrencyModel() {
        currencyModelStub.setDigits(DIGITS);
        orderModelStub.setCurrency(currencyModelStub);
        returnRequestModelStub.setOrder(orderModelStub);

        final int result = testObj.getNumberOfDigits(returnRequestModelStub);

        assertThat(result).isEqualTo(DIGITS);
    }
}
