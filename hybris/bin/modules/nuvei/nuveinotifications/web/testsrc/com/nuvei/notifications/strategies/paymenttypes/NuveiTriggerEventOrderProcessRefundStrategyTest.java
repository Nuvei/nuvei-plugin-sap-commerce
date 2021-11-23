package com.nuvei.notifications.strategies.paymenttypes;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import com.nuvei.notifications.enums.NuveiTransactionStatus;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
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
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiTriggerEventOrderProcessRefundStrategyTest {

    private static final String ORDER_CODE = "orderCode";
    private static final String CODE = "code";
    private static final String REQUEST_CODE = "requestCode";

    @InjectMocks
    private NuveiTriggerEventOrderProcessRefundStrategy testObj;

    @Mock
    private BusinessProcessService businessProcessServiceMock;
    @Mock
    private ProcessDefinitionDao processDefinitionDaoMock;

    @Captor
    private ArgumentCaptor<BusinessProcessEvent> processEventArgumentCaptor;

    private BusinessProcessModel businessProcessModelStub, businessProcessModelTwoStub;

    private final OrderModel orderModelStub = new OrderModel();
    private final PaymentTransactionModel paymentTransactionModelStub = new PaymentTransactionModel();
    private final PaymentTransactionEntryModel paymentTransactionEntryModelStub = new PaymentTransactionEntryModel();
    private final PaymentTransactionEntryModel paymentTransactionEntryModelTwoStub = new PaymentTransactionEntryModel();
    private final ReturnRequestModel returnRequestModelStub = new ReturnRequestModel();
    private ReturnProcessModel returnProcessModelStub = new ReturnProcessModel();

    @Before
    public void setUp() throws Exception {
        businessProcessModelStub = new BusinessProcessModel();
        businessProcessModelStub.setCode(CODE);
        businessProcessModelTwoStub = new BusinessProcessModel();
        orderModelStub.setCode(ORDER_CODE);
    }

    @Test
    public void isApplicable_shouldReturnTrue_whenSourceIsEqualsToREFUND_FOLLOW_ON() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.REFUND_FOLLOW_ON);
        paymentTransactionEntryModelStub.setClientRequestId(REQUEST_CODE);
        paymentTransactionEntryModelTwoStub.setType(PaymentTransactionType.REFUND_FOLLOW_ON);
        paymentTransactionEntryModelTwoStub.setClientRequestId(REQUEST_CODE);
        paymentTransactionEntryModelTwoStub.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));
        paymentTransactionModelStub.setEntries(Collections.singletonList(paymentTransactionEntryModelTwoStub));
        orderModelStub.setReturnRequests(Collections.singletonList(returnRequestModelStub));
        returnRequestModelStub.setCode(REQUEST_CODE);

        final boolean result = testObj.isApplicable(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isTrue();
    }

    @Test
    public void isApplicable_shouldReturnFalse_whenSourceIsDifferentToREFUND_FOLLOW_ON() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.VOID);
        final boolean result = testObj.isApplicable(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isFalse();
    }

    @Test
    public void execute_shouldReturnTrue_WhenEventIsTriggered() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.CAPTURE);
        paymentTransactionEntryModelStub.setClientRequestId(REQUEST_CODE);
        orderModelStub.setReturnRequests(Collections.singletonList(returnRequestModelStub));
        returnRequestModelStub.setCode(REQUEST_CODE);
        returnRequestModelStub.setReturnProcess(Collections.singletonList(returnProcessModelStub));
        returnProcessModelStub.setReturnRequest(returnRequestModelStub);

        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.CAPTURE.getCode())).thenReturn(Collections.singletonList(businessProcessModelStub));
        when(businessProcessServiceMock.triggerEvent(processEventArgumentCaptor.capture())).thenReturn(true);

        final Boolean result = testObj.execute(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isTrue();
    }

    @Test
    public void execute_shouldReturnFalse_WhenEventIsNotTriggered() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.CAPTURE);
        orderModelStub.setReturnRequests(Collections.emptyList());
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.CAPTURE.getCode())).thenReturn(List.of(businessProcessModelStub, businessProcessModelTwoStub));

        final Boolean result = testObj.execute(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isFalse();
    }

}
