package com.nuvei.order.actions;

import com.nuvei.services.exchange.NuveiExchangeService;
import com.safecharge.exception.SafechargeException;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiCapturePaymentActionTest {

    @InjectMocks
    private NuveiCapturePaymentAction testObj;

    @Mock
    private NuveiExchangeService defaultNuveiSettleServiceMock;

    @Mock
    private OrderProcessModel orderBusinessProcessModelMock;
    @Mock
    private OrderModel orderModelMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private PaymentTransactionEntryModel paymentTransactionEntryModelMock;

    @Test
    public void execute_shouldReturnOk_whenSettleResquestIsSuccess() throws SafechargeException {
        when(orderBusinessProcessModelMock.getOrder()).thenReturn(orderModelMock);
        doReturn(paymentTransactionEntryModelMock).when(defaultNuveiSettleServiceMock).requestSafechargeTransaction(orderModelMock);

        testObj.executeAction(orderBusinessProcessModelMock);

        verify(defaultNuveiSettleServiceMock).requestSafechargeTransaction(orderModelMock);
    }

    @Test
    public void execute_shouldReturnOk_whenSettleResquestIsError() throws SafechargeException {
        when(orderBusinessProcessModelMock.getOrder()).thenReturn(orderModelMock);
        doThrow(SafechargeException.class).when(defaultNuveiSettleServiceMock).requestSafechargeTransaction(orderModelMock);

        testObj.setModelService(modelServiceMock);

        testObj.executeAction(orderBusinessProcessModelMock);

        verify(modelServiceMock).save(orderModelMock);
    }

    @Test
    public void execute_shouldReturnOk_whenOrderIsNull() {
        when(orderBusinessProcessModelMock.getOrder()).thenReturn(null);

        testObj.executeAction(orderBusinessProcessModelMock);

        verifyZeroInteractions(defaultNuveiSettleServiceMock);
    }
}
