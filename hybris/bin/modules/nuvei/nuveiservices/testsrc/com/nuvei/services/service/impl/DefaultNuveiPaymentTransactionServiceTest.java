package com.nuvei.services.service.impl;

import com.nuvei.notifications.enums.NuveiPPPTransactionStatus;
import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import com.nuvei.services.model.NuveiPaymentMethodModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiPaymentTransactionServiceTest {

    private static final String PAYMENT_CODE = "paymentCode";
    private static final long VAL = 11L;
    private static final String TRANSACTION_ID = "transactionId";
    private static final String MERCHANT_ID = "merchantId";
    private static final String PAYMENT_METHOD_ID = "paymentMethodId";
    private static final String STRING_VAL = "11";
    private static final String ORDER_CODE = "orderCode";
    private static final String TRANSACTION_STATUS = "APPROVED";
    private static final String PAYMENT_TRANSACTION_PROVIDER = "NUVEI";
    private static final String NOTIFICATION_ID = "0000000001";
    private static final NuveiPPPTransactionStatus PPP_STATUS = NuveiPPPTransactionStatus.OK;

    @InjectMocks
    private DefaultNuveiPaymentTransactionService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private TimeService timeServiceMock;
    @Mock
    private GenericDao<PaymentTransactionEntryModel> paymentTransactionEntryModelGenericDaoMock;

    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private PaymentInfoModel paymentInfoModelMock;
    @Mock
    private NuveiPaymentMethodModel nuveiPaymentMethodModelMock;
    @Mock
    private OrderModel orderModelMock;

    private final NuveiDirectMerchantNotificationModel nuveiDirectMerchantNotificationModelStub = new NuveiDirectMerchantNotificationModel();
    private PaymentTransactionModel paymentTransactionModelStub, paymentTransactionModelTwoStub;
    private final PaymentTransactionEntryModel paymentTransactionEntryModelStub = new PaymentTransactionEntryModel();
    private final OrderModel orderModelStub = new OrderModel();
    private final PaymentTransactionType paymentTransactionTypeStub = PaymentTransactionType.VOID;
    private final String transactionStatusStub = NuveiTransactionStatus.PENDING.getCode();

    @Before
    public void setUp() {
        paymentTransactionModelStub = new PaymentTransactionModel();
        paymentTransactionModelTwoStub = new PaymentTransactionModel();
        paymentTransactionModelStub.setCode(PAYMENT_CODE);
        nuveiDirectMerchantNotificationModelStub.setTotalAmount(new BigDecimal(VAL));
        nuveiDirectMerchantNotificationModelStub.setCurrency(currencyModelMock);
        nuveiDirectMerchantNotificationModelStub.setTransactionId(TRANSACTION_ID);
        nuveiDirectMerchantNotificationModelStub.setMerchantId(MERCHANT_ID);
        nuveiDirectMerchantNotificationModelStub.setPaymentMethod(nuveiPaymentMethodModelMock);
        nuveiDirectMerchantNotificationModelStub.setId(NOTIFICATION_ID);
        nuveiDirectMerchantNotificationModelStub.setPppStatus(PPP_STATUS);
        nuveiDirectMerchantNotificationModelStub.setStatus(NuveiTransactionStatus.APPROVED);
        orderModelStub.setPaymentInfo(paymentInfoModelMock);
        orderModelStub.setCurrency(currencyModelMock);
        orderModelStub.setTotalPrice(Double.parseDouble(STRING_VAL));
        orderModelStub.setCode(ORDER_CODE);

        when(nuveiPaymentMethodModelMock.getId()).thenReturn(PAYMENT_METHOD_ID);
    }

    @Test
    public void createPaymentTransactionEntry_shouldCreatePaymentTransactionEntryAndSaveWhenNoPaymentTransactionEntryWithSameDMNIsAssociated() {
        when(paymentTransactionEntryModelGenericDaoMock.find(Map.of(PaymentTransactionEntryModel.NUVEINOTIFICATIONID, nuveiDirectMerchantNotificationModelStub.getId()))).thenReturn(null);
        when(modelServiceMock.create(PaymentTransactionEntryModel.class)).thenReturn(paymentTransactionEntryModelStub);
        paymentTransactionModelStub.setOrder(orderModelStub);

        final PaymentTransactionEntryModel result = testObj.createPaymentTransactionEntry(paymentTransactionModelStub, nuveiDirectMerchantNotificationModelStub, AUTHORIZATION);

        verify(modelServiceMock).create(PaymentTransactionEntryModel.class);
        verify(timeServiceMock).getCurrentTime();
        verify(modelServiceMock).save(paymentTransactionEntryModelStub);
        verify(modelServiceMock).refresh(paymentTransactionModelStub);

        assertThat(result.getCode()).isEqualTo(PAYMENT_CODE + "-" + AUTHORIZATION.getCode() + "-1");
        assertThat(result.getRequestId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.getType()).isEqualTo(AUTHORIZATION);
        assertThat(result.getPaymentTransaction()).isEqualTo(paymentTransactionModelStub);
        assertThat(result.getTransactionStatus()).isEqualTo(TRANSACTION_STATUS);
        assertThat(result.getTransactionStatusDetails()).isEqualTo(PPP_STATUS.getCode());
        assertThat(result.getAmount()).isEqualTo(new BigDecimal(VAL));
        assertThat(result.getCurrency()).isEqualTo(currencyModelMock);
    }

    @Test
    public void createPaymentTransactionEntry_shouldReturnExistingPaymentTransactionEntryWhenDMNNotificationIsAssociated() {
        when(paymentTransactionEntryModelGenericDaoMock.find(Map.of(PaymentTransactionEntryModel.NUVEINOTIFICATIONID, nuveiDirectMerchantNotificationModelStub.getId())))
                .thenReturn(List.of(paymentTransactionEntryModelStub));
        paymentTransactionEntryModelStub.setPaymentTransaction(paymentTransactionModelStub);
        paymentTransactionModelStub.setOrder(orderModelStub);

        final PaymentTransactionEntryModel result = testObj.createPaymentTransactionEntry(paymentTransactionModelStub, nuveiDirectMerchantNotificationModelStub, AUTHORIZATION);

        assertThat(result).isEqualTo(paymentTransactionEntryModelStub);

        verifyZeroInteractions(modelServiceMock);
        verifyZeroInteractions(timeServiceMock);
    }

    @Test
    public void createPaymentTransactionEntry_shouldCreatePaymentTransactionEntryFromOrderStatusAndPaymentTypeAndSave() {
        final Date date = new Date();
        when(modelServiceMock.create(PaymentTransactionEntryModel.class)).thenReturn(paymentTransactionEntryModelStub);
        when(timeServiceMock.getCurrentTime()).thenReturn(date);
        paymentTransactionModelStub.setOrder(orderModelStub);

        testObj.createPaymentTransactionEntry(paymentTransactionModelStub, transactionStatusStub, paymentTransactionTypeStub);

        verify(modelServiceMock).create(PaymentTransactionEntryModel.class);
        verify(timeServiceMock).getCurrentTime();
        verify(modelServiceMock).save(paymentTransactionEntryModelStub);
        verify(modelServiceMock).refresh(paymentTransactionModelStub);

        assertThat(paymentTransactionEntryModelStub.getCode()).startsWith(ORDER_CODE + "_" + transactionStatusStub + "_" + date.getTime());
        assertThat(paymentTransactionEntryModelStub.getRequestId()).startsWith(ORDER_CODE + "_" + transactionStatusStub + "_" + date.getTime());
        assertThat(paymentTransactionEntryModelStub.getType()).isEqualTo(paymentTransactionTypeStub);
        assertThat(paymentTransactionEntryModelStub.getPaymentTransaction()).isEqualTo(paymentTransactionModelStub);
        assertThat(paymentTransactionEntryModelStub.getTransactionStatus()).isEqualTo(transactionStatusStub);
    }

    @Test
    public void findOrCreatePaymentTransaction_shouldCreatePaymentTransactionWhenOrderHasNoPaymentTransactionsAndTransactionIsAUTH() {

        when(modelServiceMock.create(PaymentTransactionModel.class)).thenReturn(paymentTransactionModelStub);

        final PaymentTransactionModel result = testObj.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelStub, orderModelStub, NuveiTransactionType.AUTH);

        verify(modelServiceMock).create(PaymentTransactionModel.class);

        assertThat(result.getCode()).isEqualTo(TRANSACTION_ID);
        assertThat(result.getRequestId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.getRequestToken()).isEqualTo(MERCHANT_ID);
        assertThat(result.getRequestToken()).isEqualTo(MERCHANT_ID);
        assertThat(result.getPaymentProvider()).isEqualTo(PAYMENT_TRANSACTION_PROVIDER);
        assertThat(result.getOrder()).isEqualTo(orderModelStub);
        assertThat(result.getCurrency()).isEqualTo(currencyModelMock);
        assertThat(result.getInfo()).isEqualTo(paymentInfoModelMock);
        assertThat(result.getPlannedAmount()).isEqualTo(new BigDecimal(VAL));
    }

    @Test
    public void findOrCreatePaymentTransaction_shouldCreatePaymentTransactionWhenOrderHasNoPaymentTransactionsAndTransactionIsAUTHAndPaymentMethodNull() {
        when(modelServiceMock.create(PaymentTransactionModel.class)).thenReturn(paymentTransactionModelStub);
        nuveiDirectMerchantNotificationModelStub.setPaymentMethod(null);

        final PaymentTransactionModel result = testObj.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelStub, orderModelStub, NuveiTransactionType.AUTH);

        verify(modelServiceMock).create(PaymentTransactionModel.class);

        assertThat(result.getCode()).isEqualTo(TRANSACTION_ID);
        assertThat(result.getRequestId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.getRequestToken()).isEqualTo(MERCHANT_ID);
        assertThat(result.getRequestToken()).isEqualTo(MERCHANT_ID);
        assertThat(result.getPaymentProvider()).isEqualTo(PAYMENT_TRANSACTION_PROVIDER);
        assertThat(result.getOrder()).isEqualTo(orderModelStub);
        assertThat(result.getCurrency()).isEqualTo(currencyModelMock);
        assertThat(result.getInfo()).isEqualTo(paymentInfoModelMock);
        assertThat(result.getPlannedAmount()).isEqualTo(new BigDecimal(VAL));
    }

    @Test
    public void findOrCreatePaymentTransaction_shouldGetPaymentTransaction_WhenOrderHasPaymentTransactions() {
        orderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));

        final PaymentTransactionModel result = testObj.findOrCreatePaymentTransaction(nuveiDirectMerchantNotificationModelStub, orderModelStub, NuveiTransactionType.AUTH);

        assertThat(result).isEqualTo(paymentTransactionModelStub);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPaymentTransaction_shouldThrowException_whenOrderIsNull() {
        testObj.getPaymentTransaction(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPaymentTransaction_shouldReturnNull_whenOrderHasNoPaymentTransactions() {
        testObj.getPaymentTransaction(orderModelStub);
    }

    @Test
    public void getPaymentTransaction_shouldReturnFirstPaymentTransaction_whenOrderHasMoreThanOne() {
        when(orderModelMock.getPaymentTransactions()).thenReturn(List.of(paymentTransactionModelStub, paymentTransactionModelTwoStub));

        testObj.getPaymentTransaction(orderModelMock);

        verify(orderModelMock).getCode();
    }
}
