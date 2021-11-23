package com.nuvei.ordercancel.denialstrategies.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.DefaultOrderCancelDenialReason;
import de.hybris.platform.ordercancel.OrderCancelDenialReason;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiPaymentStatusOrderCancelDenialStrategyTest {

    @Spy
    @InjectMocks
    private NuveiPaymentStatusOrderCancelDenialStrategy testObj;

    @Mock
    private OrderCancelConfigModel orderCancelConfigModelMock;
    @Mock
    private PrincipalModel principalModelMock;

    private OrderModel orderModelStub = new OrderModel();
    private PaymentTransactionModel paymentTransactionModelStub = new PaymentTransactionModel();
    private PaymentTransactionEntryModel paymentTransactionEntryModelStub, paymentTransactionEntryModelTwoStub;

    @Before
    public void setUp() throws Exception {
        paymentTransactionEntryModelStub = new PaymentTransactionEntryModel();
        paymentTransactionEntryModelTwoStub = new PaymentTransactionEntryModel();
        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelStub));
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCancelDenialReason_shouldThrowException_whenOrderIsNull() {
        testObj.getCancelDenialReason(orderCancelConfigModelMock, null, principalModelMock, false, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCancelDenialReason_shouldThrowException_whenOrderCancelConfigIsNull_() {
        testObj.getCancelDenialReason(null, orderModelStub, principalModelMock, false, false);
    }

    @Test
    public void getCancelDenialReason_shouldReturnDecision_whenHasAuthorizedTransactionTypeIsFalse() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.CAPTURE);

        when(testObj.getReason()).thenReturn(new DefaultOrderCancelDenialReason());

        final OrderCancelDenialReason result = testObj.getCancelDenialReason(orderCancelConfigModelMock, orderModelStub, principalModelMock, false, false);

        assertThat(result).isNotNull();
    }

    @Test
    public void getCancelDenialReason_shouldReturnDecision_whenHasNoCaptureTransactionTypeIsFalse() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelTwoStub.setType(PaymentTransactionType.CAPTURE);
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, paymentTransactionEntryModelTwoStub));

        when(testObj.getReason()).thenReturn(new DefaultOrderCancelDenialReason());

        final OrderCancelDenialReason result = testObj.getCancelDenialReason(orderCancelConfigModelMock, orderModelStub, principalModelMock, false, false);

        assertThat(result).isNotNull();
    }

    @Test
    public void getCancelDenialReason_shouldReturnNull_whenHasNoCaptureTransactionTypeIsTrueAndHasAuthorizedTransactionTypeIsTrue() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModelTwoStub.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub, paymentTransactionEntryModelTwoStub));

        when(testObj.getReason()).thenReturn(new DefaultOrderCancelDenialReason());

        final OrderCancelDenialReason result = testObj.getCancelDenialReason(orderCancelConfigModelMock, orderModelStub, principalModelMock, false, false);

        assertThat(result).isNull();
    }
}
