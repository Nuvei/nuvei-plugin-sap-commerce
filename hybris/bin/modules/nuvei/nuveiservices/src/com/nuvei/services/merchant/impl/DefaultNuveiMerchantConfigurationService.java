package com.nuvei.services.merchant.impl;

import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.site.BaseSiteService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfSingleResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.lang.String.format;

public class DefaultNuveiMerchantConfigurationService implements NuveiMerchantConfigurationService {

    private static final String ID_MUST_NOT_BE_NULL = "Parameter id must not be null";
    private static final String SITE_ID_MUST_NOT_BE_NULL = "Parameter siteId must not be null";
    private static final String MERCHANT_CONFIGURATION_NOT_FOUND = "MerchantConfiguration with id '%s' not found!";
    private static final String NULL_BASE_SITE_OR_WITHOUT_MERCHANT = "Current BaseSite is null os has no MerchantConfiguration";
    private static final String MULTIPLE_MERCHANT_CONFIGURATION_FOUND = "MerchantConfiguration id '%s' is not unique, %d MerchantConfiguration found!";

    protected final BaseSiteService baseSiteService;
    protected final DefaultGenericDao<NuveiMerchantConfigurationModel> nuveiMerchantConfigurationGenericDao;

    /**
     * Default constructor for {@link DefaultNuveiMerchantConfigurationService}
     *
     * @param baseSiteService                      injected
     * @param nuveiMerchantConfigurationGenericDao injected
     */
    public DefaultNuveiMerchantConfigurationService(final BaseSiteService baseSiteService,
                                                    final DefaultGenericDao<NuveiMerchantConfigurationModel> nuveiMerchantConfigurationGenericDao) {
        this.baseSiteService = baseSiteService;
        this.nuveiMerchantConfigurationGenericDao = nuveiMerchantConfigurationGenericDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NuveiMerchantConfigurationModel getCurrentConfiguration() {
        return Optional.ofNullable(baseSiteService.getCurrentBaseSite())
                .map(BaseSiteModel::getNuveiMerchantConfiguration)
                .orElseThrow(() -> new IllegalStateException(NULL_BASE_SITE_OR_WITHOUT_MERCHANT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NuveiMerchantConfigurationModel> getAllMerchantConfigurations() {
        return nuveiMerchantConfigurationGenericDao.find();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NuveiMerchantConfigurationModel getMerchantConfigurationByMerchantIdAndSiteId(final String merchantId, final String siteId) {
        validateParameterNotNull(merchantId, ID_MUST_NOT_BE_NULL);
        validateParameterNotNull(siteId, SITE_ID_MUST_NOT_BE_NULL);

        final List<NuveiMerchantConfigurationModel> nuveiMerchantInfoConfigurationModels =
                nuveiMerchantConfigurationGenericDao.find(Map.of(NuveiMerchantConfigurationModel.MERCHANTID, merchantId, NuveiMerchantConfigurationModel.MERCHANTSITEID, siteId));

        validateIfSingleResult(nuveiMerchantInfoConfigurationModels, format(MERCHANT_CONFIGURATION_NOT_FOUND, merchantId),
                format(MULTIPLE_MERCHANT_CONFIGURATION_FOUND, merchantId,
                        nuveiMerchantInfoConfigurationModels.size()));

        return nuveiMerchantInfoConfigurationModels.get(0);
    }
}
