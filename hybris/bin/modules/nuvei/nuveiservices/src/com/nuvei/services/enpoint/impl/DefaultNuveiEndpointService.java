package com.nuvei.services.enpoint.impl;

import com.nuvei.services.enpoint.NuveiEndpointService;
import com.nuvei.services.enums.NuveiEnv;
import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.safecharge.util.APIConstants;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.net.URI;

/**
 * {@inheritDoc}
 */
public class DefaultNuveiEndpointService implements NuveiEndpointService {

    private static final String NUVEISERVICES_REDIRECT_ENDPOINT_ACTION_CONFIGURATION_KEY = "nuveiservices.redirect.endpoint.action";
    private static final String PURCHASE_ENDPOINT_ACTION = "purchase.do";

    private final NuveiMerchantConfigurationService merchantConfigurationService;
    private final ConfigurationService configurationService;

    /**
     * Default constructor
     *
     * @param merchantConfigurationService injected
     * @param configurationService         injected
     */
    public DefaultNuveiEndpointService(final NuveiMerchantConfigurationService merchantConfigurationService, final ConfigurationService configurationService) {
        this.merchantConfigurationService = merchantConfigurationService;
        this.configurationService = configurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getRedirectEndpoint() {
        final String purchaseEndpoint = configurationService.getConfiguration().getString(NUVEISERVICES_REDIRECT_ENDPOINT_ACTION_CONFIGURATION_KEY, PURCHASE_ENDPOINT_ACTION);
        return URI.create(getEnvironmentURL() + purchaseEndpoint);
    }

    protected String getEnvironmentURL() {
        final NuveiMerchantConfigurationModel currentConfiguration = merchantConfigurationService.getCurrentConfiguration();
        final NuveiEnv environment = currentConfiguration.getEnv();
        return NuveiEnv.PROD.equals(environment) ?
                APIConstants.Environment.PRODUCTION_HOST.getUrl() :
                APIConstants.Environment.INTEGRATION_HOST.getUrl();
    }

}
