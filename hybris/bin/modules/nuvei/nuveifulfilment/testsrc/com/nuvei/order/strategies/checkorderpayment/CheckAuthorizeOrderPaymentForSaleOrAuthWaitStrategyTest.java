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
public class CheckAuthorizeOrderPaymentForSaleOrAuthWaitStrategyTest {

    private static final String WAIT = "WAIT";

    @InjectMocks
    private NuveiCheckAuthorizeOrderPaymentForSaleOrAuthWaitStrategy testObj;

    @Mock
    private ModelService modelServiceMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void execute_shouldReturnTransitionWaitAndEnsureOrderIsSavedWithOrderStatusAsPendingAuthorization() {
        final OrderModel orderStub = Mockito.spy(OrderModel.class);

        final String result = testObj.execute(orderStub);

        verify(orderStub).setStatus(OrderStatus.PENDING_AUTHORIZATION);
        verify(modelServiceMock).save(orderStub);
        assertThat(result).isEqualTo(WAIT);
    }

    @Test
    public void isApplicable_shouldReturnTrueWhenNoPaymentTransactionsExists() {
        final OrderModel orderStub = new OrderModel();

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isTrue();
    }

    @Test
    public void isApplicable_shouldReturnTrueWhenPaymentsTransactionsIsZero() {
        final OrderModel orderStub = new OrderModel();
        orderStub.setPaymentTransactions(List.of());

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isTrue();
    }

    @Test
    public void isApplicable_shouldReturnFalseWhenMoreThanOnePaymentTransactionExists() {
        final OrderModel orderStub = new OrderModel();
        orderStub.setPaymentTransactions(List.of(new PaymentTransactionModel(), new PaymentTransactionModel()));

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isFalse();
    }

    @Parameters(method = "nonValidPaymentTransactionTypesForAuthWait")
    @Test
    public void isApplicable_shouldReturnFalseWhenStoredPaymentTransactionIsAuthorizedOrSale(final PaymentTransactionType paymentTransactionType,
                                                                                             final NuveiTransactionStatus nuveiTransactionStatus,
                                                                                             final Boolean expectedResult) {
        final OrderModel orderStub = new OrderModel();
        final PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        final PaymentTransactionEntryModel paymentTransactionEntryModel = new PaymentTransactionEntryModel();
        paymentTransactionEntryModel.setType(paymentTransactionType);
        paymentTransactionEntryModel.setTransactionStatus(nuveiTransactionStatus.getCode());
        paymentTransactionModel.setEntries(List.of(paymentTransactionEntryModel));
        orderStub.setPaymentTransactions(List.of(paymentTransactionModel));

        final boolean result = testObj.isApplicable(orderStub);

        assertThat(result).isEqualTo(expectedResult);
    }

    protected Object[] nonValidPaymentTransactionTypesForAuthWait() {
        return new Object[]{
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.APPROVED, false},
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.SUCCESS, false},
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.ERROR, false},
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.DECLINED, false},
                new Object[]{PaymentTransactionType.AUTHORIZATION, NuveiTransactionStatus.PENDING, true},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.APPROVED, false},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.SUCCESS, false},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.ERROR, false},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.ERROR, false},
                new Object[]{PaymentTransactionType.SALE, NuveiTransactionStatus.PENDING, true}
        };
    }


}
