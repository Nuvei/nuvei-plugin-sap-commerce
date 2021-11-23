package com.nuvei.services.util;

import com.nuvei.services.enums.NuveiTransactionType;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@RunWith(JUnitParamsRunner.class)
public class NuveiPaymentStatusResolverTest {

    @InjectMocks
    private NuveiPaymentStatusResolver testObj;

    protected Object[] transactionTypes() {
        return new Object[]{
                new Object[]{NuveiTransactionType.AUTH, PaymentTransactionType.AUTHORIZATION},
                new Object[]{NuveiTransactionType.SALE, PaymentTransactionType.SALE},
                new Object[]{NuveiTransactionType.SETTLE, PaymentTransactionType.CAPTURE},
                new Object[]{NuveiTransactionType.CREDIT, PaymentTransactionType.REFUND_FOLLOW_ON},
                new Object[]{NuveiTransactionType.VOID, PaymentTransactionType.VOID}
        };
    }

    @Test
    @Parameters(method = "transactionTypes")
    public void paymentTransactionTypeResolver(NuveiTransactionType transactionType, PaymentTransactionType paymentTransactionType) {
        final PaymentTransactionType result = testObj.paymentTransactionTypeResolver(transactionType);

        assertThat(result).isEqualTo(paymentTransactionType);
    }
}
