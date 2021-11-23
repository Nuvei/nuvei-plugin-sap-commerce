package com.nuvei.services.merchant;


import com.nuvei.services.model.NuveiMerchantConfigurationModel;

import java.util.List;

/**
 * Manages the merchant configurations
 */
public interface NuveiMerchantConfigurationService {

    /**
     * Returns the NuveiMerchantConfigurationModel for the current site
     *
     * @return a {@link NuveiMerchantConfigurationModel}
     */
    NuveiMerchantConfigurationModel getCurrentConfiguration();


    /**
     * Returns the set of NuveiMerchantConfigurationModel related to all the sites in the system
     *
     * @return a {@link List <NuveiMerchantConfigurationModel>}
     */
    List<NuveiMerchantConfigurationModel> getAllMerchantConfigurations();

    /**
     * Returns the NuveiMerchantConfigurationModel for the given id
     *
     * @return a {@link NuveiMerchantConfigurationModel}
     */
    NuveiMerchantConfigurationModel getMerchantConfigurationByMerchantIdAndSiteId(final String merchantId, final String siteId);
}
