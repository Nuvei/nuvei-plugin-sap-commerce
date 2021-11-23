package com.nuvei.notifications.strategies;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
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
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiAbstractTriggerEventOrderProcessStrategyTest {

    private static final String TRANSITION_CHOICE = "transitionChoice";
    private static final String PAYMENT_CODE = "paymentCode";
    private static final String ORDER_CODE = "orderCode";
    private static final String CODE = "code";

    @InjectMocks
    private MyNuveiAbstractTriggerEventOrderProcessStrategy testObj;

    @Mock
    private BusinessProcessService businessProcessServiceMock;
    @Mock
    private ProcessDefinitionDao processDefinitionDaoMock;

    @Captor
    private ArgumentCaptor<BusinessProcessEvent> processEventArgumentCaptor;

    private BusinessProcessModel businessProcessModelStub, businessProcessModelTwoStub;

    private final OrderModel orderModelStub = new OrderModel();

    @Before
    public void setUp() {
        businessProcessModelStub = new BusinessProcessModel();
        businessProcessModelStub.setCode(CODE);
        businessProcessModelTwoStub = new BusinessProcessModel();
        orderModelStub.setCode(ORDER_CODE);
    }

    @Test
    public void triggerEvent_shouldReturnTrue_whenEventIsTriggered() {
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PAYMENT_CODE)).thenReturn(Collections.singletonList(businessProcessModelStub));
        when(businessProcessServiceMock.triggerEvent(processEventArgumentCaptor.capture())).thenReturn(true);

        final Boolean result = testObj.triggerEvent(orderModelStub, PAYMENT_CODE, TRANSITION_CHOICE);

        assertThat(result).isTrue();
    }

    @Test
    public void triggerEvent_shouldReturnFalse_whenEventIsNotTriggered() {
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PAYMENT_CODE)).thenReturn(Collections.singletonList(businessProcessModelStub));
        when(businessProcessServiceMock.triggerEvent(processEventArgumentCaptor.capture())).thenReturn(false);

        final Boolean result = testObj.triggerEvent(orderModelStub, PAYMENT_CODE, TRANSITION_CHOICE);

        assertThat(result).isFalse();
    }

    @Test
    public void triggerEvent_shouldReturnFalse_whenThereIsNotBusinessProcessModel() {
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PAYMENT_CODE)).thenReturn(List.of(businessProcessModelStub, businessProcessModelTwoStub));

        final Boolean result = testObj.triggerEvent(orderModelStub, PAYMENT_CODE, TRANSITION_CHOICE);

        assertThat(result).isFalse();
    }

    @Test
    public void triggerEvent_shouldReturnFalse_whenThereAreMoreThanOneBusinessProcessModel() {
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.CAPTURE.getCode())).thenReturn(List.of(businessProcessModelStub, businessProcessModelTwoStub));

        final Boolean result = testObj.triggerEvent(orderModelStub, PAYMENT_CODE, TRANSITION_CHOICE);

        assertThat(result).isFalse();
    }

    private static class MyNuveiAbstractTriggerEventOrderProcessStrategy extends NuveiAbstractTriggerEventOrderProcessStrategy {

        protected MyNuveiAbstractTriggerEventOrderProcessStrategy(final BusinessProcessService businessProcessService, final ProcessDefinitionDao processDefinitionDao) {
            super(LogManager.getLogger(MyNuveiAbstractTriggerEventOrderProcessStrategy.class), businessProcessService, processDefinitionDao);
        }

        @Override
        public Boolean execute(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
            return null;
        }

        @Override
        public boolean isApplicable(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
            return false;
        }
    }
}
