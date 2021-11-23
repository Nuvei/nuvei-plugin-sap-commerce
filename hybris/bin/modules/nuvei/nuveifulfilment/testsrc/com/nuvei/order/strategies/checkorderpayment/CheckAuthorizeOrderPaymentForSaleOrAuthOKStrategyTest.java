package com.nuvei.order.strategies.checkorderpayment;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(JUnitParamsRunner.class)
public class CheckAuthorizeOrderPaymentForSaleOrAuthOKStrategyTest {

    private static final String OK = "OK";
    public static final double TOTAL_PRICE = 12d;

    @InjectMocks
    private NuveiCheckAuthorizeOrderPaymentForSaleOrAuthOKStrategy testObj;

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

        verify(orderStub).setStatus(OrderStatus.PAYMENT_AUTHORIZED);
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


    @Test
    public void isApplicable_shouldReturnTrue_WhenHasPendingAndSuccessTransactionEnry() {
        final OrderModel orderStub = new OrderModel();
        final PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        final PaymentTransactionEntryModel paymentTransactionEntryModel = new PaymentTransactionEntryModel();
        paymentTransactionEntryModel.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModel.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
        paymentTransactionEntryModel.setAmount(BigDecimal.valueOf(TOTAL_PRICE));
        final PaymentTransactionEntryModel secondPaymentTransactionEntryModel = new PaymentTransactionEntryModel();
        secondPaymentTransactionEntryModel.setType(PaymentTransactionType.AUTHORIZATION);
        secondPaymentTransactionEntryModel.setTransactionStatus(NuveiTransactionStatus.SUCCESS.getCode());
        secondPaymentTransactionEntryModel.setAmount(BigDecimal.valueOf(TOTAL_PRICE));
        paymentTransactionModel.setEntries(List.of(paymentTransactionEntryModel, secondPaymentTransactionEntryModel));
        orderStub.setPaymentTransactions(List.of(paymentTransactionModel));
        orderStub.setTotalPrice(TOTAL_PRICE);

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isTrue();
    }

    @Test
    public void isApplicable_shouldReturnFalse_WhenHasPendingAuthorizeTransactionEnry() {
        final OrderModel orderStub = new OrderModel();
        final PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        final PaymentTransactionEntryModel paymentTransactionEntryModel = new PaymentTransactionEntryModel();
        paymentTransactionEntryModel.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntryModel.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
        paymentTransactionEntryModel.setAmount(BigDecimal.valueOf(TOTAL_PRICE));
        paymentTransactionModel.setEntries(List.of(paymentTransactionEntryModel));
        orderStub.setPaymentTransactions(List.of(paymentTransactionModel));
        orderStub.setTotalPrice(TOTAL_PRICE);

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isFalse();
    }

    @Parameters(method = "nonValidPaymentTransactionTypesForAuthOK")
    @Test
    public void isApplicable_shouldReturnFalseWhenStoredPaymentTransactionIsDifferentFromAuthorizedOrSale(final PaymentTransactionType paymentTransactionType) {
        final OrderModel orderStub = new OrderModel();
        final PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        final PaymentTransactionEntryModel paymentTransactionEntryModel = new PaymentTransactionEntryModel();
        paymentTransactionEntryModel.setType(paymentTransactionType);
        paymentTransactionModel.setEntries(List.of(paymentTransactionEntryModel));
        orderStub.setPaymentTransactions(List.of(paymentTransactionModel));

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isFalse();
    }


    @Parameters(method = "validPaymentTransactionTypesForAuthOK")
    @Test
    public void isApplicable_shouldReturnTrueWhenStoredPaymentTransactionStatusIsApprovedOrSuccessAndAmountIsSameAsTotalPriceOfOrder(final PaymentTransactionType paymentTransactionType,
                                                                                                                                     final NuveiTransactionStatus nuveiTransactionStatus,
                                                                                                                                     final Double totalPrice,
                                                                                                                                     final Double transactionPrice,
                                                                                                                                     final boolean expectedResult) {

        final OrderModel orderStub = new OrderModel();
        final PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        final PaymentTransactionEntryModel paymentTransactionEntryModel = new PaymentTransactionEntryModel();


        paymentTransactionEntryModel.setTransactionStatus(nuveiTransactionStatus.getCode());
        paymentTransactionEntryModel.setType(paymentTransactionType);
        paymentTransactionEntryModel.setAmount(BigDecimal.valueOf(transactionPrice));

        paymentTransactionModel.setEntries(List.of(paymentTransactionEntryModel));

        orderStub.setPaymentTransactions(List.of(paymentTransactionModel));
        orderStub.setTotalPrice(totalPrice);

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isEqualTo(expectedResult);
    }


    protected Object[] nonValidPaymentTransactionTypesForAuthOK() {
        return new Object[]{
                new Object[]{PaymentTransactionType.VOID},
                new Object[]{PaymentTransactionType.CANCEL},
                new Object[]{PaymentTransactionType.CAPTURE},
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

    protected Object[] validPaymentTransactionTypesForAuthOK() {
        return new Object[]{
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.APPROVED, 88.00D, 88.00D, true},
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.SUCCESS, 88.00D, 88.00D, true},
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.ERROR, 88.00D, 88.00D, false},
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.SUCCESS, 88.00D, 55.00D, false},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.APPROVED, 88.00D, 88.00D, true},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.SUCCESS, 88.00D, 88.00D, true},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.ERROR, 88.00D, 88.00D, false},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.SUCCESS, 88.00D, 55.00D, false}
        };
    }

}
