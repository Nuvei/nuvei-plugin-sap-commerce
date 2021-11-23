package com.nuvei.backoffice.renderers;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiFraudCheckButtonCellRendererTest {

    private static final String SEPARATOR = "_";
    private static final String EVENT = "CAPTURE";
    private static final String CSA_ORDER_VERIFIED = "CSAOrderVerified";
    private static final String STORE_ORDER_PROCESS_CODE = "order-process";
    private static final String SPECIFIC_ORDER_PROCESS_CODE = "order-process-123456";
    private static final String DIFFERENT_STORE_ORDER_PROCESS_CODE = "order2-process";

    @InjectMocks
    private NuveiFraudCheckButtonCellRenderer testObj;

    @Mock
    private BusinessProcessService businessProcessServiceMock;
    @Mock
    private ModelService modelServiceMock;

    @Captor
    private ArgumentCaptor<BusinessProcessEvent> businessProcessEventArgumentCaptor;

    private OrderModel orderModel;
    private BaseStoreModel baseStoreModel;
    private OrderProcessModel orderProcess;

    @Before
    public void setUp() {
        orderModel = new OrderModel();
        baseStoreModel = new BaseStoreModel();
        orderProcess = new OrderProcessModel();

        orderModel.setStore(baseStoreModel);
        orderProcess.setCode(SPECIFIC_ORDER_PROCESS_CODE);
    }

    @Test
    public void executeFraudulentOperation_ShouldSaveTheOrderAndTriggerTheEvent_WhenTheOrderProcessIsFound() {
        baseStoreModel.setSubmitOrderProcessCode(STORE_ORDER_PROCESS_CODE);
        orderModel.setOrderProcess(List.of(orderProcess));

        testObj.executeFraudulentOperation(orderModel);

        verify(modelServiceMock).save(orderModel);
        verify(businessProcessServiceMock).triggerEvent(businessProcessEventArgumentCaptor.capture());

        final BusinessProcessEvent businessProcessEvent = businessProcessEventArgumentCaptor.getValue();

        assertThat(businessProcessEvent.getChoice()).isEqualTo(CSA_ORDER_VERIFIED);
        assertThat(businessProcessEvent.getEvent()).isEqualTo(SPECIFIC_ORDER_PROCESS_CODE + SEPARATOR + EVENT);
    }

    @Test
    public void executeFraudulentOperation_ShouldNotTriggerTheEvent_WhenTheOrderProcessIsDoesntMatchTheStoreOrderProcess() {
        baseStoreModel.setSubmitOrderProcessCode(DIFFERENT_STORE_ORDER_PROCESS_CODE);
        orderModel.setOrderProcess(List.of(orderProcess));

        testObj.executeFraudulentOperation(orderModel);

        verify(modelServiceMock).save(orderModel);
        verify(businessProcessServiceMock, never()).triggerEvent(any(BusinessProcessEvent.class));
    }

    @Test
    public void executeFraudulentOperation_ShouldNotTriggerTheEvent_WhenTheOrderHasNoOrderProcess() {
        baseStoreModel.setSubmitOrderProcessCode(DIFFERENT_STORE_ORDER_PROCESS_CODE);
        orderModel.setOrderProcess(Collections.emptyList());

        testObj.executeFraudulentOperation(orderModel);

        verify(modelServiceMock).save(orderModel);
        verify(businessProcessServiceMock, never()).triggerEvent(any(BusinessProcessEvent.class));
    }
}
