package com.nuvei.backoffice.widgets.actions;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.nuvei.notifications.dao.ProcessDefinitionDao;
import com.nuvei.notifications.enums.NuveiTransactionStatus;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiManualCapturePaymentActionTest {

    private static final String ORDER_CODE = "orderCode";

    @Spy
    @InjectMocks
    private NuveiManualCapturePaymentAction testObj;

    @Mock
    private ProcessDefinitionDao processDefinitionDaoMock;
    @Mock
    private BusinessProcessService businessProcessServiceMock;

    @Mock
    private ActionContext<OrderModel> actionContextMock;
    @Mock
    private BusinessProcessModel businessProcessModelOneMock, businessProcessModelTwoMock;
    @Mock
    private BusinessProcessEvent businessProcessEventMock;
    @Mock
    private PaymentTransactionEntryModel paymentTransactionEntryModelMock;
    @Mock
    private PaymentTransactionModel paymentTransactionOneModelMock, paymentTransactionTwoModelMock;
    @Mock
    private OrderProcessModel orderProcessModelMock;
    @Mock
    private ModelService modelServiceMock;

    private final OrderModel orderModelStub = new OrderModel();

    @Before
    public void setUp() {
        orderModelStub.setCode(ORDER_CODE);
        when(actionContextMock.getData()).thenReturn(orderModelStub);
    }

    @Test
    public void perform_shouldReturnSuccess_whenOrderProcessIsTriggered() {
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.CAPTURE.getCode()))
                .thenReturn(Collections.singletonList(businessProcessModelOneMock));
        when(businessProcessServiceMock.triggerEvent((BusinessProcessEvent) any())).thenReturn(Boolean.TRUE);
        doNothing().when(testObj).showErrorMessage(actionContextMock);

        final ActionResult<OrderModel> result = testObj.perform(actionContextMock);

        assertThat(result.getResultCode()).isEqualTo(ActionResult.SUCCESS);
    }

    @Test
    public void perform_shouldReturnNok_whenOrderProcessIsNotTriggered() {
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.CAPTURE.getCode()))
                .thenReturn(Collections.singletonList(businessProcessModelOneMock));
        when(businessProcessServiceMock.triggerEvent(businessProcessEventMock)).thenReturn(Boolean.FALSE);
        doNothing().when(testObj).showErrorMessage(actionContextMock);

        final ActionResult<OrderModel> result = testObj.perform(actionContextMock);

        assertThat(result.getResultCode()).isEqualTo(ActionResult.ERROR);
    }

    @Test
    public void perform_shouldReturnError_whenOrderHasMoreThantOneOrderProcess() {
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.CAPTURE.getCode()))
                .thenReturn(List.of(businessProcessModelOneMock, businessProcessModelTwoMock));
        doNothing().when(testObj).showErrorMessage(actionContextMock);

        final ActionResult<OrderModel> result = testObj.perform(actionContextMock);

        assertThat(result.getResultCode()).isEqualTo(ActionResult.ERROR);
    }

    @Test
    public void perform_shouldReturnError_whenOrderHasNotOrderProcess() {
        when(processDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, PaymentTransactionType.CAPTURE.getCode()))
                .thenReturn(Collections.emptyList());
        doNothing().when(testObj).showErrorMessage(actionContextMock);

        final ActionResult<OrderModel> result = testObj.perform(actionContextMock);

        assertThat(result.getResultCode()).isEqualTo(ActionResult.ERROR);
    }

    @Test
    public void perform_shouldReturnError_whenOrderIsNull() {
        when(actionContextMock.getData()).thenReturn(null);

        final ActionResult<OrderModel> result = testObj.perform(actionContextMock);

        assertThat(result.getResultCode()).isEqualTo(ActionResult.ERROR);
    }

    @Test
    public void perform_shouldReturnError_whenContextIsNull() {
        final ActionResult<OrderModel> result = testObj.perform(null);

        assertThat(result.getResultCode()).isEqualTo(ActionResult.ERROR);
    }

    @Test
    public void checkAuthorizationSuccess_shouldReturnTrue_whenPaymentTransactionEntryModelIsAuthorizedAndApproved() {
        when(paymentTransactionEntryModelMock.getType()).thenReturn(PaymentTransactionType.AUTHORIZATION);
        when(paymentTransactionEntryModelMock.getTransactionStatus()).thenReturn(NuveiTransactionStatus.APPROVED.getCode());

        final boolean result = testObj.checkAuthorizationSuccess(paymentTransactionEntryModelMock);

        assertThat(result).isTrue();
    }

    @Test
    public void checkAuthorizationSuccess_shouldReturnTrue_whenPaymentTransactionEntryModelIsAuthorizedAndSuccess() {
        when(paymentTransactionEntryModelMock.getType()).thenReturn(PaymentTransactionType.AUTHORIZATION);
        when(paymentTransactionEntryModelMock.getTransactionStatus()).thenReturn(NuveiTransactionStatus.SUCCESS.getCode());

        final boolean result = testObj.checkAuthorizationSuccess(paymentTransactionEntryModelMock);

        assertThat(result).isTrue();
    }

    @Test
    public void checkAuthorizationSuccess_shouldReturnTrue_whenPaymentTransactionEntryModelIsNotAuthorized() {
        when(paymentTransactionEntryModelMock.getType()).thenReturn(PaymentTransactionType.SALE);
        when(paymentTransactionEntryModelMock.getTransactionStatus()).thenReturn(NuveiTransactionStatus.SUCCESS.getCode());

        final boolean result = testObj.checkAuthorizationSuccess(paymentTransactionEntryModelMock);

        assertThat(result).isFalse();
    }

    @Test
    public void checkAuthorizationSuccess_shouldReturnTrue_whenPaymentTransactionEntryModelIsNotSuccessOrApprove() {
        when(paymentTransactionEntryModelMock.getType()).thenReturn(PaymentTransactionType.AUTHORIZATION);
        when(paymentTransactionEntryModelMock.getTransactionStatus()).thenReturn(NuveiTransactionStatus.DECLINED.getCode());

        final boolean result = testObj.checkAuthorizationSuccess(paymentTransactionEntryModelMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_shouldReturnFalse_whenContextIsNull() {
        final boolean result = testObj.canPerform(null);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_shouldReturnFalse_whenOrderIsNull() {
        when(actionContextMock.getData()).thenReturn(null);

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_shouldReturnFalse_whenOrderStatusIsDifferentToCAPTURE_PENDING() {
        orderModelStub.setStatus(OrderStatus.CANCELLED);
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionOneModelMock));
        orderModelStub.setOrderProcess(Collections.singletonList(orderProcessModelMock));

        when(actionContextMock.getData()).thenReturn(orderModelStub);

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_shouldReturnFalse_whenOrderHasNoOrderProcess() {
        orderModelStub.setStatus(OrderStatus.CAPTURE_PENDING);
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionOneModelMock));

        when(actionContextMock.getData()).thenReturn(orderModelStub);

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_shouldReturnFalse_whenOrderHasNoPaymentTransactions() {
        orderModelStub.setStatus(OrderStatus.CAPTURE_PENDING);
        orderModelStub.setPaymentTransactions(Collections.emptyList());
        orderModelStub.setOrderProcess(Collections.singletonList(orderProcessModelMock));

        when(actionContextMock.getData()).thenReturn(orderModelStub);

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_shouldReturnFalse_whenOrderHasMoreThanOnePaymentTransaction() {
        orderModelStub.setStatus(OrderStatus.CAPTURE_PENDING);
        orderModelStub.setPaymentTransactions(List.of(paymentTransactionOneModelMock, paymentTransactionTwoModelMock));
        orderModelStub.setOrderProcess(Collections.singletonList(orderProcessModelMock));

        when(actionContextMock.getData()).thenReturn(orderModelStub);

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_shouldReturnFalse_whenPaymentTransactionHasNoEntries() {
        orderModelStub.setStatus(OrderStatus.CAPTURE_PENDING);
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionOneModelMock));
        orderModelStub.setOrderProcess(Collections.singletonList(orderProcessModelMock));

        when(paymentTransactionOneModelMock.getEntries()).thenReturn(Collections.emptyList());
        when(actionContextMock.getData()).thenReturn(orderModelStub);

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_shouldReturnFalse_whenPaymentTransactionHasNoValidEntries() {
        orderModelStub.setStatus(OrderStatus.CAPTURE_PENDING);
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionOneModelMock));
        orderModelStub.setOrderProcess(Collections.singletonList(orderProcessModelMock));

        when(paymentTransactionOneModelMock.getEntries()).thenReturn(Collections.singletonList(paymentTransactionEntryModelMock));
        when(actionContextMock.getData()).thenReturn(orderModelStub);
        doReturn(false).when(testObj).checkAuthorizationSuccess(paymentTransactionEntryModelMock);

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_shouldReturnTrue_whenPaymentTransactionHasValidEntry() {
        orderModelStub.setStatus(OrderStatus.CAPTURE_PENDING);
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionOneModelMock));
        orderModelStub.setOrderProcess(Collections.singletonList(orderProcessModelMock));

        when(paymentTransactionOneModelMock.getEntries()).thenReturn(Collections.singletonList(paymentTransactionEntryModelMock));
        doReturn(true).when(testObj).checkAuthorizationSuccess(paymentTransactionEntryModelMock);

        when(actionContextMock.getData()).thenReturn(orderModelStub);

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isTrue();
    }

    @Test
    public void needsConfirmation_ShouldReturnFalse() {
        final boolean result = testObj.needsConfirmation(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void getConfirmationMessage_ShouldReturnNull() {
        final String result = testObj.getConfirmationMessage(actionContextMock);

        assertThat(result).isNull();
    }
}
