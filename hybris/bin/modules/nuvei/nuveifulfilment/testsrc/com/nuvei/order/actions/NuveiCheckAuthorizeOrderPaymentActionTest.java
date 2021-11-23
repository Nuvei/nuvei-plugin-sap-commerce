package com.nuvei.order.actions;

import com.nuvei.strategy.NuveiAbstractStrategyExecutor;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiCheckAuthorizeOrderPaymentActionTest {

    private static final String OK = "OK";
    private static final String NOK = "NOK";

    @InjectMocks
    private NuveiCheckAuthorizeOrderPaymentAction testObj;

    @Mock
    private NuveiAbstractStrategyExecutor<OrderModel, String> strategyExecutorMock;

    private final OrderProcessModel orderProcessModel = new OrderProcessModel();
    private final OrderModel orderModel = new OrderModel();

    @Test
    public void execute_ShouldExecuteAction_WhenOrderProcessContainsOrderModel() {
        orderProcessModel.setOrder(orderModel);
        when(strategyExecutorMock.execute(orderModel)).thenReturn(OK);
        final String result = testObj.execute(orderProcessModel);

        verify(strategyExecutorMock).execute(orderModel);
        assertThat(result).isEqualTo(OK);

    }

    @Test
    public void execute_ShouldReturnNokTransition_WhenOrderProcessNotContainsOrderModel() {
        final String result = testObj.execute(orderProcessModel);

        verifyZeroInteractions(strategyExecutorMock);
        assertThat(result).isEqualTo(NOK);
    }
}
