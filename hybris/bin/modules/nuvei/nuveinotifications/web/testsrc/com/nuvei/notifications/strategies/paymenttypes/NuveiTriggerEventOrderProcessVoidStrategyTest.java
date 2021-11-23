package com.nuvei.notifications.strategies.paymenttypes;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import com.nuvei.services.service.NuveiOrderService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiTriggerEventOrderProcessVoidStrategyTest {

    private static final String ORDER_CODE = "orderCode";
    private static final String CODE = "code";

    @InjectMocks
    private NuveiTriggerEventOrderProcessVoidStrategy testObj;

    @Mock
    private BusinessProcessService businessProcessServiceMock;
    @Mock
    private ProcessDefinitionDao processDefinitionDaoMock;
    @Mock
    private NuveiOrderService nuveiOrderServiceMock;

    @Captor
    private ArgumentCaptor<BusinessProcessEvent> processEventArgumentCaptor;

    private BusinessProcessModel businessProcessModelStub, businessProcessModelTwoStub;

    private final OrderModel orderModelStub = new OrderModel();
    private final PaymentTransactionEntryModel paymentTransactionEntryModelStub = new PaymentTransactionEntryModel();

    @Before
    public void setUp() {
        businessProcessModelStub = new BusinessProcessModel();
        businessProcessModelStub.setCode(CODE);
        businessProcessModelTwoStub = new BusinessProcessModel();
        orderModelStub.setCode(ORDER_CODE);
        orderModelStub.setStatus(OrderStatus.CAPTURE_PENDING);
    }

    @Test
    public void isApplicable_shouldReturnTrue_whenSourceIsEqualsToVOIDAndOrderStatusIsNotVoided() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);

        final boolean result = testObj.isApplicable(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isTrue();
    }

    @Test
    public void isApplicable_shouldReturnFalse_whenSourceIsDifferentToVOID() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);

        final boolean result = testObj.isApplicable(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isFalse();
    }

    @Test
    public void isApplicable_shouldReturnFalse_whenSourceIsEqualsToVOIDAndOrderStatusIsVoided() {
        orderModelStub.setStatus(OrderStatus.VOIDED);
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);

        final boolean result = testObj.isApplicable(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isFalse();
    }

    @Test
    public void execute_shouldReturnTrue_WhenEventIsTriggered() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);

        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.VOID.getCode())).thenReturn(Collections.singletonList(businessProcessModelStub));
        when(businessProcessServiceMock.triggerEvent(processEventArgumentCaptor.capture())).thenReturn(true);

        final Boolean result = testObj.execute(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isTrue();
    }

    @Test
    public void execute_shouldReturnTrue_WhenEventIsNotTriggeredAndRequestCancelOrderIsCreated() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);

        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.VOID.getCode())).thenReturn(Collections.singletonList(businessProcessModelStub));
        when(businessProcessServiceMock.triggerEvent(processEventArgumentCaptor.capture())).thenReturn(false);
        when(nuveiOrderServiceMock.requestCancelOrder(orderModelStub)).thenReturn(true);

        final Boolean result = testObj.execute(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isTrue();
        verify(nuveiOrderServiceMock).requestCancelOrder(orderModelStub);
    }

    @Test
    public void execute_shouldReturnFalse_WhenEventIsNotTriggeredAndRequestCancelOrderIsNotCreated() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);

        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.VOID.getCode())).thenReturn(Collections.singletonList(businessProcessModelStub));
        when(businessProcessServiceMock.triggerEvent(processEventArgumentCaptor.capture())).thenReturn(false);
        when(nuveiOrderServiceMock.requestCancelOrder(orderModelStub)).thenReturn(false);

        final Boolean result = testObj.execute(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isFalse();
        verify(nuveiOrderServiceMock).requestCancelOrder(orderModelStub);
    }
}
