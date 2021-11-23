package com.nuvei.voids.actions;

import com.nuvei.model.NuveiVoidProcessModel;
import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.services.exchange.NuveiExchangeService;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import com.safecharge.exception.SafechargeException;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
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
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiVoidOrderActionTest {

    private static final String NOK = "NOK";
    private static final String WAIT = "WAIT";
    private static final String OK = "OK";

    @InjectMocks
    private NuveiVoidOrderAction testObj;

    @Mock
    private NuveiExchangeService nuveiExchangeService;
    @Mock
    private NuveiPaymentTransactionService nuveiPaymentTransactionService;
    @Mock
    private ModelService modelServiceMock;

    private final NuveiVoidProcessModel nuveiVoidProcessModelStub = new NuveiVoidProcessModel();
    private final OrderModel orderModelStub = new OrderModel();
    private final PaymentTransactionModel paymentTransactionModelStub = new PaymentTransactionModel();

    @Before
    public void setUp() throws Exception {
        testObj.setModelService(modelServiceMock);
    }

    private final PaymentTransactionEntryModel paymentTransactionEntryModelStub = new PaymentTransactionEntryModel();
    private final PaymentTransactionEntryModel secondPaymentTransactionEntryModelStub = new PaymentTransactionEntryModel();

    @Test
    public void execute_shouldReturnNok_whenOrderIsNull() throws SafechargeException {
        final String result = testObj.execute(nuveiVoidProcessModelStub);

        assertThat(result).isEqualTo(NOK);
    }

    @Test
    public void execute_shouldReturnWait_whenOrderHasNoPaymentTransactions() throws SafechargeException {
        nuveiVoidProcessModelStub.setOrder(orderModelStub);

        final String result = testObj.execute(nuveiVoidProcessModelStub);

        assertThat(result).isEqualTo(NOK);
    }

    @Test
    public void execute_shouldReturnOK_whenVoidPaymentTransactionExist() throws SafechargeException {
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));
        nuveiVoidProcessModelStub.setOrder(orderModelStub);

        when(nuveiPaymentTransactionService.getPaymentTransaction(orderModelStub)).thenReturn(paymentTransactionModelStub);

        paymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.SUCCESS.getCode());
        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));

        final String result = testObj.execute(nuveiVoidProcessModelStub);

        verify(modelServiceMock).save(orderModelStub);
        assertThat(orderModelStub.getStatus()).isEqualTo(OrderStatus.VOIDED);
        assertThat(result).isEqualTo(OK);
    }

    @Test
    public void execute_shouldReturnWait_whenPaymentTransactionIsAuthorized() throws SafechargeException {
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));
        nuveiVoidProcessModelStub.setOrder(orderModelStub);

        when(nuveiPaymentTransactionService.getPaymentTransaction(orderModelStub)).thenReturn(paymentTransactionModelStub);
        when(nuveiExchangeService.requestSafechargeTransaction(anyObject())).thenReturn(secondPaymentTransactionEntryModelStub);

        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.SUCCESS.getCode());
        secondPaymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        secondPaymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));

        final String result = testObj.execute(nuveiVoidProcessModelStub);

        verify(modelServiceMock).save(orderModelStub);
        verify(nuveiExchangeService).requestSafechargeTransaction(orderModelStub);
        assertThat(result).isEqualTo(WAIT);
        assertThat(orderModelStub.getStatus()).isEqualTo(OrderStatus.VOID_STARTED);
    }

    @Test
    public void execute_shouldReturnNok_whenVoidIsRefused() throws SafechargeException {
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));
        nuveiVoidProcessModelStub.setOrder(orderModelStub);

        when(nuveiPaymentTransactionService.getPaymentTransaction(orderModelStub)).thenReturn(paymentTransactionModelStub);

        paymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.DECLINED.getCode());
        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));

        final String result = testObj.execute(nuveiVoidProcessModelStub);

        assertThat(result).isEqualTo(NOK);
    }

    @Test
    public void execute_shouldReturnNok_whenPaymentTransactionIsNotAuthorized() throws SafechargeException {
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));
        nuveiVoidProcessModelStub.setOrder(orderModelStub);

        when(nuveiPaymentTransactionService.getPaymentTransaction(orderModelStub)).thenReturn(paymentTransactionModelStub);

        paymentTransactionEntryModelStub.setType(PaymentTransactionType.PARTIAL_CAPTURE);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.SUCCESS.getCode());
        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));

        final String result = testObj.execute(nuveiVoidProcessModelStub);

        assertThat(result).isEqualTo(NOK);
    }

    @Test
    public void isVoidRefused_shouldReturnTrue_whenHasPaymentTransactionEntryModelVoidDeclined() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        secondPaymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);
        secondPaymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.DECLINED.getCode());
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, secondPaymentTransactionEntryModelStub));

        final Boolean result = testObj.isVoidRefused(paymentTransactionModelStub);

        assertThat(result).isTrue();
    }

    @Test
    public void isVoidRefused_shouldReturnTrue_whenHasPaymentTransactionEntryModelVoidError() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        secondPaymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);
        secondPaymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.ERROR.getCode());
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, secondPaymentTransactionEntryModelStub));

        final Boolean result = testObj.isVoidRefused(paymentTransactionModelStub);

        assertThat(result).isTrue();
    }

    @Test
    public void isVoidRefused_shouldReturnFalse_whenHasNoPaymentTransactionEntryModelVoidErrorOrDeclined() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        secondPaymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);
        secondPaymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, secondPaymentTransactionEntryModelStub));

        final Boolean result = testObj.isVoidRefused(paymentTransactionModelStub);

        assertThat(result).isFalse();
    }

    @Test
    public void isVoidPresent_shouldReturnTrue_whenHasPaymentTransactionEntryModelVoidApproved() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        secondPaymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);
        secondPaymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, secondPaymentTransactionEntryModelStub));

        final Boolean result = testObj.isVoidPresent(paymentTransactionModelStub);

        assertThat(result).isTrue();
    }

    @Test
    public void isVoidPresent_shouldReturnTrue_whenHasPaymentTransactionEntryModelVoidSuccess() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        secondPaymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);
        secondPaymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.SUCCESS.getCode());
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, secondPaymentTransactionEntryModelStub));

        final Boolean result = testObj.isVoidPresent(paymentTransactionModelStub);

        assertThat(result).isTrue();
    }

    @Test
    public void isVoidPresent_shouldReturnFalse_whenHasNoPaymentTransactionEntryModelVoidApprovedOrDeclined() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        secondPaymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);
        secondPaymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.DECLINED.getCode());
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, secondPaymentTransactionEntryModelStub));

        final Boolean result = testObj.isVoidPresent(paymentTransactionModelStub);

        assertThat(result).isFalse();
    }

    @Test
    public void findAcceptedAuthorizationEntry_shouldReturnPaymentTransaction_whenPaymentTransactionEntryModelAuthorizedSuccess() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.SUCCESS.getCode());
        secondPaymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        secondPaymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, secondPaymentTransactionEntryModelStub));

        final PaymentTransactionEntryModel result = testObj.findAcceptedAuthorizationEntry(paymentTransactionModelStub);

        assertThat(result).isEqualTo(paymentTransactionEntryModelStub);
    }

    @Test
    public void findAcceptedAuthorizationEntry_shouldReturnPaymentTransaction_whenPaymentTransactionEntryModelAuthorizedApproved() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.APPROVED.getCode());
        secondPaymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        secondPaymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, secondPaymentTransactionEntryModelStub));

        final PaymentTransactionEntryModel result = testObj.findAcceptedAuthorizationEntry(paymentTransactionModelStub);

        assertThat(result).isEqualTo(paymentTransactionEntryModelStub);
    }

    @Test
    public void findAcceptedAuthorizationEntry_shouldReturnNull_whenThereIsNotPaymentTransactionEntryModelAuthorizedApprovedOrSuccess() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.ERROR.getCode());
        secondPaymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        secondPaymentTransactionEntryModelStub.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, secondPaymentTransactionEntryModelStub));

        final PaymentTransactionEntryModel result = testObj.findAcceptedAuthorizationEntry(paymentTransactionModelStub);

        assertThat(result).isNull();
    }
}
