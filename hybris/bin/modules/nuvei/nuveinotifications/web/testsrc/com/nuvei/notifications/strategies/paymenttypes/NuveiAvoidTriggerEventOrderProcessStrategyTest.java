package com.nuvei.notifications.strategies.paymenttypes;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@RunWith(JUnitParamsRunner.class)
public class NuveiAvoidTriggerEventOrderProcessStrategyTest {

    private static final String ORDER_CODE = "orderCode";

    @InjectMocks
    private NuveiAvoidTriggerEventOrderProcessStrategy testObj;

    private final OrderModel orderModelStub = new OrderModel();
    private final PaymentTransactionEntryModel paymentTransactionEntryModelStub = new PaymentTransactionEntryModel();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        orderModelStub.setCode(ORDER_CODE);
        orderModelStub.setStatus(OrderStatus.VOIDED);
    }

    @Test
    public void execute_ShouldReturnTrue() {
        paymentTransactionEntryModelStub.setType(PaymentTransactionType.CAPTURE);

        final Boolean result = testObj.execute(Pair.of(paymentTransactionEntryModelStub, orderModelStub));
        assertThat(result).isTrue();
    }

    @Parameters(method = "orderStatusPaymentTypeCombinations")
    @Test
    public void isApplicable(final PaymentTransactionType paymentTransactionType, final OrderStatus orderStatus, final String transactionStatus, final Boolean expetedResult) {
        orderModelStub.setStatus(orderStatus);
        paymentTransactionEntryModelStub.setType(paymentTransactionType);
        paymentTransactionEntryModelStub.setTransactionStatus(transactionStatus);

        final boolean result = testObj.isApplicable(Pair.of(paymentTransactionEntryModelStub, orderModelStub));

        assertThat(result).isEqualTo(expetedResult);
    }

    protected Object[] orderStatusPaymentTypeCombinations() {
        return new Object[]{
                new Object[]{PaymentTransactionType.VOID, OrderStatus.VOIDED, StringUtils.EMPTY, true},
                new Object[]{PaymentTransactionType.VOID, OrderStatus.CAPTURE_PENDING, StringUtils.EMPTY, false},
                new Object[]{PaymentTransactionType.VOID, OrderStatus.CAPTURE_PENDING, NuveiTransactionStatus.PENDING.getCode(), true}
        };
    }
}
