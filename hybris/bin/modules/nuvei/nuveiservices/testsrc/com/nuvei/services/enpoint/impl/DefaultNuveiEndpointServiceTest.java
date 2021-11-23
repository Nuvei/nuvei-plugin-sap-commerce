package com.nuvei.services.enpoint.impl;

import com.nuvei.services.enums.NuveiEnv;
import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiEndpointServiceTest {

	private static final String PURCHASE_ENDPOINT_ACTION = "purchase.do";
	private static final String NUVEISERVICES_REDIRECT_ENDPOINT_ACTION_CONFIGURATION_KEY = "nuveiservices.redirect.endpoint.action";
	private static final String LIVE_URL = "https://secure.safecharge.com/ppp/";
	private static final String TEST_URL = "https://ppp-test.safecharge.com/ppp/";

	@InjectMocks
	private DefaultNuveiEndpointService testObj;

	@Mock
	private NuveiMerchantConfigurationService nuveiMerchantConfigurationServiceMock;
	@Mock
	private ConfigurationService configurationServiceMock;

	@Mock
	private NuveiMerchantConfigurationModel nuveiMerchantConfigurationModelMock;
	@Mock
	private Configuration configurationMock;

	@Before
	public void setUp() {
		when(configurationServiceMock.getConfiguration()).thenReturn(configurationMock);
		when(configurationMock.getString(NUVEISERVICES_REDIRECT_ENDPOINT_ACTION_CONFIGURATION_KEY, PURCHASE_ENDPOINT_ACTION)).thenReturn(PURCHASE_ENDPOINT_ACTION);
	}

	@Test
	public void getRedirectEndpoint_shouldReturnProductionEndpoint_whenMerchantConfigurationIsSetToLive() {
		when(nuveiMerchantConfigurationServiceMock.getCurrentConfiguration()).thenReturn(nuveiMerchantConfigurationModelMock);
		when(nuveiMerchantConfigurationModelMock.getEnv()).thenReturn(NuveiEnv.PROD);

		final URI result = testObj.getRedirectEndpoint();

		assertThat(result).hasToString(LIVE_URL + PURCHASE_ENDPOINT_ACTION);
	}

	@Test
	public void getRedirectEndpoint_shouldReturnIntegrationEndpoint_whenMerchantConfigurationIsSetToTest() {
		when(nuveiMerchantConfigurationServiceMock.getCurrentConfiguration()).thenReturn(nuveiMerchantConfigurationModelMock);
		when(nuveiMerchantConfigurationModelMock.getEnv()).thenReturn(NuveiEnv.INT);

		final URI result = testObj.getRedirectEndpoint();

		assertThat(result).hasToString(TEST_URL + PURCHASE_ENDPOINT_ACTION);
	}
}
