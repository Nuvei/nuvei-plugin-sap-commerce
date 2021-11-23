package com.nuvei.notifications.strategies.paymenttypes;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import de.hybris.bootstrap.annotations.UnitTest;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiTriggerEventOrderProcessDefaultStrategyTest {

    private static final String ORDER_CODE = "orderCode";
    private static final String CODE = "code";

    @InjectMocks
    private NuveiTriggerEventOrderProcessDefaultStrategy testObj;

    @Mock
    private BusinessProcessService businessProcessServiceMock;
    @Mock
    private ProcessDefinitionDao processDefinitionDaoMock;

    @Captor
    private ArgumentCaptor<BusinessProcessEvent> processEventArgumentCaptor;

    private BusinessProcessModel businessProcessModelStub, businessProcessModelTwoStub;

    private final OrderModel orderModelStub = new OrderModel();
    private final PaymentTransactionEntryModel paymentTransactionEntryModelStub = new PaymentTransactionEntryModel();

    @Before
    public void setUp() throws Exception {
        businessProcessModelStub = new BusinessProcessModel();
        businessProcessModelStub.setCode(CODE);
        businessProcessModelTwoStub = new BusinessProcessModel();
        orderModelStub.setCode(ORDER_CODE);
    }

    @Test
    public void isApplicable_shouldReturnTrue_whenSourceIsDifferentToSALEAndCAPTURE() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        final boolean result = testObj.isApplicable(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isTrue();
    }

    @Test
    public void isApplicable_shouldReturnFalse_whenSourceIsEqualsToSALE() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.SALE);
        final boolean result = testObj.isApplicable(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isFalse();
    }

    @Test
    public void isApplicable_shouldReturnFalse_whenSourceIsEqualsToCAPTURE() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.CAPTURE);
        final boolean result = testObj.isApplicable(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isFalse();
    }

    @Test
    public void isApplicable_shouldReturnFalse_whenSourceIsEqualsToVoid() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);
        final boolean result = testObj.isApplicable(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isFalse();
    }

    @Test
    public void execute_shouldReturnTrue_WhenEventIsTriggered() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.AUTHORIZATION.getCode())).thenReturn(Collections.singletonList(businessProcessModelStub));
        when(businessProcessServiceMock.triggerEvent(processEventArgumentCaptor.capture())).thenReturn(true);

        final Boolean result = testObj.execute(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isTrue();
    }

    @Test
    public void execute_shouldReturnFalse_WhenEventIsNotTriggered() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.CAPTURE);
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.CAPTURE.getCode())).thenReturn(List.of(businessProcessModelStub, businessProcessModelTwoStub));

        final Boolean result = testObj.execute(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        verifyZeroInteractions(businessProcessServiceMock);
        assertThat(result).isFalse();
    }
}
