package com.nuvei.services.util;

import com.nuvei.services.enums.NuveiTransactionType;
import de.hybris.platform.payment.enums.PaymentTransactionType;

public class NuveiPaymentStatusResolver {

    private NuveiPaymentStatusResolver() {
        throw new IllegalStateException("Utility class");
    }

    public static PaymentTransactionType paymentTransactionTypeResolver(final NuveiTransactionType nuveiTransactionType) {

        switch (nuveiTransactionType) {
            case AUTH:
                return PaymentTransactionType.AUTHORIZATION;
            case SALE:
                return PaymentTransactionType.SALE;
            case SETTLE:
                return PaymentTransactionType.CAPTURE;
            case CREDIT:
                return PaymentTransactionType.REFUND_FOLLOW_ON;
            case VOID:
                return PaymentTransactionType.VOID;
            default:
                return null;
        }
    }
}
