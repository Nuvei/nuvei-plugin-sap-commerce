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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(JUnitParamsRunner.class)
public class CheckAuthorizeOrderPaymentForSaleOrAuthNOKStatusStrategyTest {

    private static final String NOK = "NOK";

    @InjectMocks
    private NuveiCheckAuthorizeOrderPaymentForSaleOrAuthNOKStatusStrategy testObj;

    @Mock
    private ModelService modelServiceMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void execute_shouldReturnTransitionNokAndEnsureOrderIsSavedWithOrderStatusAsCheckedInvalid() {
        final OrderModel orderStub = Mockito.spy(OrderModel.class);

        final String result = testObj.execute(orderStub);

        verify(orderStub).setStatus(OrderStatus.PAYMENT_NOT_AUTHORIZED);
        verify(modelServiceMock).save(orderStub);
        assertThat(result).isEqualTo(NOK);
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

    @Parameters(method = "nonValidPaymentTransactionTypesForCaptureNOK")
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

    @Parameters(method = "validPaymentTransactionTypesForCaptureNOK")
    @Test
    public void isApplicable_shouldReturnTrueWhenStoredPaymentTransactionStatusIsApprovedOrSuccessAndAmountIsSameAsTotalPriceOfOrder(final PaymentTransactionType paymentTransactionType,
                                                                                                                                     final NuveiTransactionStatus nuveiTransactionStatus,
                                                                                                                                     final boolean expectedResult) {

        final OrderModel orderStub = new OrderModel();
        final PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        final PaymentTransactionEntryModel paymentTransactionEntryModel = new PaymentTransactionEntryModel();


        paymentTransactionEntryModel.setTransactionStatus(nuveiTransactionStatus.getCode());
        paymentTransactionEntryModel.setType(paymentTransactionType);

        paymentTransactionModel.setEntries(List.of(paymentTransactionEntryModel));

        orderStub.setPaymentTransactions(List.of(paymentTransactionModel));

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isEqualTo(expectedResult);
    }

    protected Object[] nonValidPaymentTransactionTypesForCaptureNOK() {
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

    protected Object[] validPaymentTransactionTypesForCaptureNOK() {
        return new Object[]{
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.APPROVED, false},
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.SUCCESS, false},
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.ERROR, true},
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.DECLINED, true},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.APPROVED, false},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.SUCCESS, false},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.ERROR, true},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.DECLINED, true}
        };
    }
}
