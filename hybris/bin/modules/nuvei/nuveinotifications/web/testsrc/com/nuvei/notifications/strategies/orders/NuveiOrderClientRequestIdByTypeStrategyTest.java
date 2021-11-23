package com.nuvei.notifications.strategies.orders;

import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import com.nuvei.services.service.NuveiPaymentTransactionEntryService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiOrderClientRequestIdByTypeStrategyTest {

    private static final String CLIENT_REQUEST_ID = "clientRequestId";

    @InjectMocks
    private NuveiOrderClientRequestIdByTypeStrategy testObj;

    @Mock
    private NuveiPaymentTransactionEntryService nuveiPaymentTransactionEntryServiceMock;

    @Mock
    private NuveiDirectMerchantNotificationModel nuveiDirectMerchantNotificationModelMock;
    @Mock
    private AbstractOrderModel abstractOrderModelMock;
    @Mock
    private PaymentTransactionEntryModel paymentTransactionEntryModelMock;
    @Mock
    private PaymentTransactionModel paymentTransactionModelMock;

    @Test
    public void isApplicable_shouldReturnFalse_whenSourceIsDifferentCREDIT() {
        final boolean result = testObj.isApplicable(Pair.of(NuveiTransactionType.SETTLE, nuveiDirectMerchantNotificationModelMock));

        assertThat(result).isFalse();
    }

    @Test
    public void isApplicable_shouldReturnTrue_whenSourceIsEqualsCREDIT() {
        final boolean result = testObj.isApplicable(Pair.of(NuveiTransactionType.CREDIT, nuveiDirectMerchantNotificationModelMock));

        assertThat(result).isTrue();
    }

    @Test
    public void execute_shouldReturnAbstractOrderModel_WhenPaymentTransactionEntryIsFoundAndHasOrder() {
        when(nuveiDirectMerchantNotificationModelMock.getClientRequestId()).thenReturn(CLIENT_REQUEST_ID);
        when(nuveiPaymentTransactionEntryServiceMock.findPaymentEntryByClientRequestIDAndType(CLIENT_REQUEST_ID, PaymentTransactionType.REFUND_FOLLOW_ON)).thenReturn(paymentTransactionEntryModelMock);
        when(paymentTransactionEntryModelMock.getPaymentTransaction()).thenReturn(paymentTransactionModelMock);
        when(paymentTransactionModelMock.getOrder()).thenReturn(abstractOrderModelMock);

        final AbstractOrderModel result = testObj.execute(Pair.of(NuveiTransactionType.CREDIT, nuveiDirectMerchantNotificationModelMock));

        assertThat(result).isEqualTo(abstractOrderModelMock);
    }

    @Test
    public void execute_shouldReturnNull_WhenPaymentTransactionEntryIsNotFound() {
        when(nuveiDirectMerchantNotificationModelMock.getClientRequestId()).thenReturn(CLIENT_REQUEST_ID);
        when(nuveiPaymentTransactionEntryServiceMock.findPaymentEntryByClientRequestIDAndType(
                CLIENT_REQUEST_ID, PaymentTransactionType.REFUND_FOLLOW_ON)).thenReturn(null);
        when(paymentTransactionEntryModelMock.getPaymentTransaction()).thenReturn(paymentTransactionModelMock);
        when(paymentTransactionModelMock.getOrder()).thenReturn(abstractOrderModelMock);

        final AbstractOrderModel result = testObj.execute(Pair.of(NuveiTransactionType.CREDIT, nuveiDirectMerchantNotificationModelMock));

        assertThat(result).isNull();
    }

    @Test
    public void execute_shouldReturnNull_WhenPaymentTransactionEntryHasNoOrder() {
        when(nuveiDirectMerchantNotificationModelMock.getClientRequestId()).thenReturn(CLIENT_REQUEST_ID);
        when(nuveiPaymentTransactionEntryServiceMock.findPaymentEntryByClientRequestIDAndType(
                CLIENT_REQUEST_ID, PaymentTransactionType.REFUND_FOLLOW_ON)).thenReturn(paymentTransactionEntryModelMock);
        when(paymentTransactionEntryModelMock.getPaymentTransaction()).thenReturn(paymentTransactionModelMock);
        when(paymentTransactionModelMock.getOrder()).thenReturn(null);

        final AbstractOrderModel result = testObj.execute(Pair.of(NuveiTransactionType.CREDIT, nuveiDirectMerchantNotificationModelMock));

        assertThat(result).isNull();
    }
}
