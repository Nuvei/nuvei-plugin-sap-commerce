package com.nuvei.order.strategies.checkorderpaymentstatus;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(JUnitParamsRunner.class)
public class CheckPaymentStatusForSaleOrCaptureOKStrategyTest {

    private static final Object OK = "OK";

    @InjectMocks
    private NuveiCheckPaymentStatusForSaleOrCaptureOKStrategy testObj;


    @Mock
    private ModelService modelServiceMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void execute_shouldReturnTransitionOkAndEnsureOrderIsSavedWithOrderStatusAsPaymentAuthorized() {
        final OrderModel orderStub = Mockito.spy(OrderModel.class);

        final String result = testObj.execute(orderStub);

        verify(orderStub).setStatus(OrderStatus.PAYMENT_CAPTURED);
        verify(modelServiceMock).save(orderStub);
        assertThat(result).isEqualTo(OK);
    }

    @Test
    public void isApplicable_shouldReturnFalseWhenNoPaymentTransactionsExists() {
        final OrderModel orderStub = new OrderModel();

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isFalse();
    }

    @Test
    public void isApplicable_shouldReturnFalseWhenPaymentsTransactionsIsZero() {
        final OrderModel orderStub = new OrderModel();
        orderStub.setPaymentTransactions(List.of());

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isFalse();
    }

    @Test
    public void isApplicable_shouldReturnFalseWhenMoreThanOnePaymentTransactionExists() {
        final OrderModel orderStub = new OrderModel();
        orderStub.setPaymentTransactions(List.of(new PaymentTransactionModel(), new PaymentTransactionModel()));

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isFalse();
    }

    @Parameters(method = "nonValidPaymentTransactionTypesForCaptureOK")
    @Test
    public void isApplicable_shouldReturnFalseWhenStoredPaymentTransactionIsDifferentFromCapturedOrSale(final PaymentTransactionType paymentTransactionType) {
        final OrderModel orderStub = new OrderModel();
        final PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        final PaymentTransactionEntryModel paymentTransactionEntryModel = new PaymentTransactionEntryModel();
        paymentTransactionEntryModel.setType(paymentTransactionType);
        paymentTransactionModel.setEntries(List.of(paymentTransactionEntryModel));
        orderStub.setPaymentTransactions(List.of(paymentTransactionModel));

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isFalse();
    }


    @Parameters(method = "validPaymentTransactionTypesForCaptureOK")
    @Test
    public void isApplicable_shouldReturnTrueWhenStoredPaymentTransactionStatusIsApprovedOrSuccessAndAmountIsSameAsTotalPriceOfOrder(final List<PaymentTransactionType> paymentTransactionTypeList,
                                                                                                                                     final List<NuveiTransactionStatus> nuveiTransactionStatusList,
                                                                                                                                     final Double totalPrice,
                                                                                                                                     final Double transactionPrice,
                                                                                                                                     final boolean expectedResult) {
        final OrderModel orderStub = new OrderModel();
        final PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();

        List<PaymentTransactionEntryModel> transactionEntryModels = new ArrayList<>();

        for (PaymentTransactionType paymentTransactionType: paymentTransactionTypeList) {
            final PaymentTransactionEntryModel paymentTransactionEntryModel = new PaymentTransactionEntryModel();

            paymentTransactionEntryModel.setType(paymentTransactionType);
            paymentTransactionEntryModel.setAmount(BigDecimal.valueOf(transactionPrice));

            transactionEntryModels.add(paymentTransactionEntryModel);
        }

        int i = 0;
        for (PaymentTransactionEntryModel entryModel : transactionEntryModels) {
            entryModel.setTransactionStatus(nuveiTransactionStatusList.get(i).getCode());
            i++;
        }

        paymentTransactionModel.setEntries(transactionEntryModels);

        orderStub.setPaymentTransactions(List.of(paymentTransactionModel));
        orderStub.setTotalPrice(totalPrice);

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isEqualTo(expectedResult);
    }


    protected Object[] nonValidPaymentTransactionTypesForCaptureOK() {
        return new Object[]{
                new Object[]{PaymentTransactionType.VOID},
                new Object[]{PaymentTransactionType.CANCEL},
                new Object[]{PaymentTransactionType.AUTHORIZATION},
                new Object[]{PaymentTransactionType.CREATE_SUBSCRIPTION},
                new Object[]{PaymentTransactionType.DELETE_SUBSCRIPTION},
                new Object[]{PaymentTransactionType.GET_SUBSCRIPTION_DATA},
                new Object[]{PaymentTransactionType.PARTIAL_CAPTURE},
                new Object[]{PaymentTransactionType.REFUND_FOLLOW_ON},
                new Object[]{PaymentTransactionType.REFUND_STANDALONE},
                new Object[]{PaymentTransactionType.REVIEW_DECISION},
                new Object[]{PaymentTransactionType.UPDATE_SUBSCRIPTION}
        };
    }

    protected Object[] validPaymentTransactionTypesForCaptureOK() {
        return new Object[]{
                new Object[]{List.of(PaymentTransactionType.CAPTURE), List.of(NuveiTransactionStatus.APPROVED), 88.00D, 88.00D, true},
                new Object[]{List.of(PaymentTransactionType.CAPTURE), List.of(NuveiTransactionStatus.SUCCESS), 88.00D, 88.00D, true},
                new Object[]{List.of(PaymentTransactionType.CAPTURE), List.of(NuveiTransactionStatus.ERROR), 88.00D, 88.00D, false},
                new Object[]{List.of(PaymentTransactionType.CAPTURE), List.of(NuveiTransactionStatus.DECLINED), 88.00D, 88.00D, false},
                new Object[]{List.of(PaymentTransactionType.SALE), List.of(NuveiTransactionStatus.APPROVED), 88.00D, 88.00D, true},
                new Object[]{List.of(PaymentTransactionType.SALE), List.of(NuveiTransactionStatus.SUCCESS), 88.00D, 88.00D, true},
                new Object[]{List.of(PaymentTransactionType.SALE), List.of(NuveiTransactionStatus.ERROR), 88.00D, 88.00D, false},
                new Object[]{List.of(PaymentTransactionType.SALE), List.of(NuveiTransactionStatus.DECLINED), 88.00D, 88.00D, false},
                new Object[]{List.of(PaymentTransactionType.CAPTURE, PaymentTransactionType.CAPTURE), List.of(NuveiTransactionStatus.APPROVED, NuveiTransactionStatus.PENDING), 88.00D, 88.00D, true},
                new Object[]{List.of(PaymentTransactionType.CAPTURE, PaymentTransactionType.AUTHORIZATION), List.of(NuveiTransactionStatus.PENDING, NuveiTransactionStatus.APPROVED), 88.00D, 88.00D, false},
                new Object[]{List.of(PaymentTransactionType.CAPTURE, PaymentTransactionType.AUTHORIZATION), List.of(NuveiTransactionStatus.APPROVED, NuveiTransactionStatus.APPROVED), 88.00D, 88.00D, true},
                new Object[]{List.of(PaymentTransactionType.AUTHORIZATION, PaymentTransactionType.CAPTURE, PaymentTransactionType.CAPTURE), List.of(NuveiTransactionStatus.APPROVED, NuveiTransactionStatus.PENDING, NuveiTransactionStatus.APPROVED), 88.00D, 88.00D, true},
                new Object[]{List.of(PaymentTransactionType.AUTHORIZATION, PaymentTransactionType.CAPTURE, PaymentTransactionType.CAPTURE), List.of(NuveiTransactionStatus.APPROVED, NuveiTransactionStatus.PENDING, NuveiTransactionStatus.ERROR), 88.00D, 88.00D, false},
        };
    }
}
