package com.nuvei.notifications.core.services.impl;

import com.nuvei.notifications.core.services.NuveiDMNService;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import com.nuvei.strategy.NuveiStrategyExecutor;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiNotificationsProcessServiceTest {

    private static final String CLIENT_UNIQUE_ID_ONE = "clientUniqueIdOne";
    private static final String CLIENT_UNIQUE_ID_TWO = "clientUniqueIdTwo";
    private static final String ORDER_CODE_ONE = "orderCodeOne";
    private static final String ORDER_CODE_TWO = "orderCodeTwo";
    private static final String BUSINESS_PROCESS_CODE = "businessProcessCode";

    @InjectMocks
    private DefaultNuveiNotificationsProcessService testObj;

    @Mock
    private NuveiDMNService nuveiDMNService;
    @Mock
    private ModelService modelService;
    @Mock
    private NuveiPaymentTransactionService nuveiPaymentTransactionService;
    @Mock
    private NuveiStrategyExecutor<Pair<PaymentTransactionEntryModel, OrderModel>, Boolean> triggerEventOrderProcessStrategyExecutor;
    @Mock
    private NuveiStrategyExecutor<Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel>, AbstractOrderModel> orderTransactionTypeStrategyExecutor;
    @Mock
    private PaymentTransactionModel paymentTransactionModelOneMock, paymentTransactionModelTwoMock;
    @Mock
    private CartModel cartModelOneMock;


    private NuveiDirectMerchantNotificationModel nuveiDirectMerchantNotificationModelOneStub, nuveiDirectMerchantNotificationModelTwoStub;
    private OrderModel orderModelOneStub, orderModelTwoStub;
    private BusinessProcessModel businessProcessModelStub, businessProcessModelTwoStub;
    private final PaymentTransactionEntryModel paymentTransactionEntryModelStub = new PaymentTransactionEntryModel();

    @Before
    public void setUp() {
        testObj = new DefaultNuveiNotificationsProcessService(nuveiDMNService, nuveiPaymentTransactionService, modelService,
                triggerEventOrderProcessStrategyExecutor, orderTransactionTypeStrategyExecutor);
        nuveiDirectMerchantNotificationModelOneStub = new NuveiDirectMerchantNotificationModel();
        nuveiDirectMerchantNotificationModelTwoStub = new NuveiDirectMerchantNotificationModel();
        orderModelOneStub = new OrderModel();
        orderModelTwoStub = new OrderModel();
        businessProcessModelStub = new BusinessProcessModel();
        businessProcessModelTwoStub = new BusinessProcessModel();
        nuveiDirectMerchantNotificationModelOneStub.setMerchantUniqueId(CLIENT_UNIQUE_ID_ONE);
        nuveiDirectMerchantNotificationModelTwoStub.setMerchantUniqueId(CLIENT_UNIQUE_ID_TWO);
        orderModelOneStub.setCode(ORDER_CODE_ONE);
        orderModelTwoStub.setCode(ORDER_CODE_TWO);
        businessProcessModelStub.setCode(BUSINESS_PROCESS_CODE);
        businessProcessModelTwoStub.setCode(BUSINESS_PROCESS_CODE);
    }

    @Test
    public void processNuveiNotifications_shouldProcessAllNotifications_whenThereAreUnprocessedDMNs() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
        when(nuveiDMNService.getUnprocessedDMNsByType(NuveiTransactionType.AUTH)).thenReturn(List.of(nuveiDirectMerchantNotificationModelOneStub, nuveiDirectMerchantNotificationModelTwoStub));
        when(orderTransactionTypeStrategyExecutor.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelOneStub))).thenReturn(orderModelOneStub);
        when(orderTransactionTypeStrategyExecutor.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelTwoStub))).thenReturn(orderModelTwoStub);
        when(nuveiPaymentTransactionService.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelOneStub, orderModelOneStub, NuveiTransactionType.AUTH)).thenReturn(paymentTransactionModelOneMock);
        when(nuveiPaymentTransactionService.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelTwoStub, orderModelTwoStub, NuveiTransactionType.AUTH)).thenReturn(paymentTransactionModelTwoMock);
        when(nuveiPaymentTransactionService.createPaymentTransactionEntry(paymentTransactionModelOneMock, nuveiDirectMerchantNotificationModelOneStub, PaymentTransactionType.AUTHORIZATION)).thenReturn(paymentTransactionEntryModelStub);
        when(nuveiPaymentTransactionService.createPaymentTransactionEntry(paymentTransactionModelTwoMock, nuveiDirectMerchantNotificationModelTwoStub, PaymentTransactionType.AUTHORIZATION)).thenReturn(paymentTransactionEntryModelStub);
        when(triggerEventOrderProcessStrategyExecutor.execute(Pair.of(paymentTransactionEntryModelStub, orderModelOneStub))).thenReturn(true);
        when(triggerEventOrderProcessStrategyExecutor.execute(Pair.of(paymentTransactionEntryModelStub, orderModelTwoStub))).thenReturn(true);

        testObj.processNuveiNotifications(NuveiTransactionType.AUTH);

        verify(modelService).saveAll(List.of(nuveiDirectMerchantNotificationModelOneStub, nuveiDirectMerchantNotificationModelTwoStub));
    }

    @Test
    public void processNuveiNotifications_shouldNoProcessNotifications_whenThereIsNoOrderRelated() {
        when(nuveiDMNService.getUnprocessedDMNsByType(NuveiTransactionType.AUTH)).thenReturn(List.of(nuveiDirectMerchantNotificationModelOneStub, nuveiDirectMerchantNotificationModelTwoStub));
        when(orderTransactionTypeStrategyExecutor.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelOneStub))).thenReturn(cartModelOneMock);

        testObj.processNuveiNotifications(NuveiTransactionType.AUTH);

        verifyZeroInteractions(modelService);
    }

    @Test
    public void processNuveiNotifications_shouldNoProcessNotifications_whenThereIsNoPaymentTransaction() {
        when(nuveiDMNService.getUnprocessedDMNsByType(NuveiTransactionType.AUTH)).thenReturn(List.of(nuveiDirectMerchantNotificationModelOneStub, nuveiDirectMerchantNotificationModelTwoStub));
        when(orderTransactionTypeStrategyExecutor.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelOneStub))).thenReturn(orderModelOneStub);
        when(orderTransactionTypeStrategyExecutor.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelTwoStub))).thenReturn(orderModelTwoStub);
        when(nuveiPaymentTransactionService.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelOneStub, orderModelOneStub, NuveiTransactionType.AUTH)).thenReturn(null);
        when(nuveiPaymentTransactionService.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelTwoStub, orderModelTwoStub, NuveiTransactionType.AUTH)).thenReturn(null);

        testObj.processNuveiNotifications(NuveiTransactionType.AUTH);

        verifyZeroInteractions(modelService);
    }

    @Test
    public void processNuveiNotifications_shouldNoProcessNotifications_whenThereIsNoBusinessProcess() {
        when(nuveiDMNService.getUnprocessedDMNsByType(NuveiTransactionType.AUTH)).thenReturn(List.of(nuveiDirectMerchantNotificationModelOneStub, nuveiDirectMerchantNotificationModelTwoStub));
        when(orderTransactionTypeStrategyExecutor.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelOneStub))).thenReturn(orderModelOneStub);
        when(orderTransactionTypeStrategyExecutor.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelTwoStub))).thenReturn(orderModelTwoStub);
        when(nuveiPaymentTransactionService.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelOneStub, orderModelOneStub, NuveiTransactionType.AUTH)).thenReturn(paymentTransactionModelOneMock);
        when(nuveiPaymentTransactionService.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelTwoStub, orderModelTwoStub, NuveiTransactionType.AUTH)).thenReturn(paymentTransactionModelTwoMock);

        testObj.processNuveiNotifications(NuveiTransactionType.AUTH);

        verifyZeroInteractions(modelService);
    }

    @Test
    public void processNuveiNotifications_shouldNoProcessNotifications_whenThereIsMoreThanOneBusinessProcess() {
        when(nuveiDMNService.getUnprocessedDMNsByType(NuveiTransactionType.AUTH)).thenReturn(List.of(nuveiDirectMerchantNotificationModelOneStub, nuveiDirectMerchantNotificationModelTwoStub));
        when(orderTransactionTypeStrategyExecutor.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelOneStub))).thenReturn(orderModelOneStub);
        when(orderTransactionTypeStrategyExecutor.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelTwoStub))).thenReturn(orderModelTwoStub);
        when(nuveiPaymentTransactionService.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelOneStub, orderModelOneStub, NuveiTransactionType.AUTH)).thenReturn(paymentTransactionModelOneMock);
        when(nuveiPaymentTransactionService.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelTwoStub, orderModelTwoStub, NuveiTransactionType.AUTH)).thenReturn(paymentTransactionModelTwoMock);

        testObj.processNuveiNotifications(NuveiTransactionType.AUTH);

        verifyZeroInteractions(modelService);
    }

}
