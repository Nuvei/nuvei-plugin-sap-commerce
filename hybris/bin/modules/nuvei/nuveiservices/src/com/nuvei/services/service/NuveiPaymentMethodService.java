package com.nuvei.services.service;

import com.nuvei.services.model.NuveiPaymentMethodModel;

/**
 * Service to handle logic of payment methods
 */
public interface NuveiPaymentMethodService {

    /**
     * Returns the payment method based on the id
     *
     * @param id the id
     * @return NuveiPaymentMethodModel the payment method match with id
     */
    NuveiPaymentMethodModel findNuveiPaymentMethodById(final String id);
}
