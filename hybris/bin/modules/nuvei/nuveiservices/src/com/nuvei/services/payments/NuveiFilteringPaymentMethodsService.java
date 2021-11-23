package com.nuvei.services.payments;


import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.safecharge.exception.SafechargeException;

/**
 * Service that provides methods to get the available payment methods of a merchant
 */
public interface NuveiFilteringPaymentMethodsService {

    /**
     * Synchronizes the available payment methods of the given {@link NuveiMerchantConfigurationModel} and stores them in the
     * database
     *
     * @param nuveiMerchantConfigurationModel The merchantConfiguration
     */
    void synchFilteringPaymentMethodsForMerchant(final NuveiMerchantConfigurationModel nuveiMerchantConfigurationModel) throws SafechargeException;
}
