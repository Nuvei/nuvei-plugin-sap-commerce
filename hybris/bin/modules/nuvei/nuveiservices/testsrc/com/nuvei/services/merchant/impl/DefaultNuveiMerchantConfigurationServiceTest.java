package com.nuvei.services.merchant.impl;

import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiMerchantConfigurationServiceTest {

	private static final String MERCHANT_SITE_ID = "siteId";
	private static final String MERCHANT_CONFIG_ID = "merchantConfigId";
	private static final String ID_MUST_NOT_BE_NULL = "Parameter id must not be null";
	private static final String SITE_ID_MUST_NOT_BE_NULL = "Parameter siteId must not be null";
	private static final String MERCHANT_CONFIGURATION_NOT_FOUND = "MerchantConfiguration with id '%s' not found!";
	private static final String NULL_BASE_SITE_OR_WITHOUT_MERCHANT = "Current BaseSite is null os has no MerchantConfiguration";
	private static final String MULTIPLE_MERCHANT_CONFIGURATION_FOUND = "MerchantConfiguration id '%s' is not unique, %d MerchantConfiguration found!";

	@InjectMocks
	private DefaultNuveiMerchantConfigurationService testObj;

	@Mock
	private BaseSiteService baseSiteServiceMock;
	@Mock
	private DefaultGenericDao<NuveiMerchantConfigurationModel> merchantConfigurationDaoMock;

	@Mock
	private BaseSiteModel baseSiteModelMock;
	@Mock
	private NuveiMerchantConfigurationModel nuveiMerchantConfigurationModelMock;
	@Mock
	private NuveiMerchantConfigurationModel secondMerchantConfigurationModelMock;

	@Test
	public void getCurrentConfiguration_shouldReturnMerchantConfigurationFromCurrentBaseSite() {
		when(baseSiteServiceMock.getCurrentBaseSite()).thenReturn(baseSiteModelMock);
		when(baseSiteModelMock.getNuveiMerchantConfiguration()).thenReturn(nuveiMerchantConfigurationModelMock);

		final NuveiMerchantConfigurationModel result = testObj.getCurrentConfiguration();

		assertThat(nuveiMerchantConfigurationModelMock).isEqualTo(result);
	}

	@Test
	public void getCurrentConfiguration_shouldRaiseIllegalStateExceptionWhenBaseSiteIsNull() {
		when(baseSiteServiceMock.getCurrentBaseSite()).thenReturn(null);

		assertThatThrownBy(() -> testObj.getCurrentConfiguration())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NULL_BASE_SITE_OR_WITHOUT_MERCHANT);
	}

	@Test
	public void getCurrentConfiguration_shouldRaiseIllegalStateExceptionWhenMerchantConfigurationIsNull1() {
		when(baseSiteServiceMock.getCurrentBaseSite()).thenReturn(baseSiteModelMock);
		when(baseSiteModelMock.getNuveiMerchantConfiguration()).thenReturn(null);

		assertThatThrownBy(() -> testObj.getCurrentConfiguration())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NULL_BASE_SITE_OR_WITHOUT_MERCHANT);
	}

	@Test
	public void getAllMerchantConfigurations_ShouldReturnAllMerchantConfigurations() {
		when(merchantConfigurationDaoMock.find()).thenReturn(List.of(nuveiMerchantConfigurationModelMock));

		final List<NuveiMerchantConfigurationModel> result = testObj.getAllMerchantConfigurations();

		assertThat(result).containsExactly(nuveiMerchantConfigurationModelMock);
	}

	@Test
	public void getMerchantConfigurationByMerchantIdAndSiteId_ShouldReturnAllMerchantConfigurations() {
		when(merchantConfigurationDaoMock.find((Map.of(NuveiMerchantConfigurationModel.MERCHANTID, MERCHANT_CONFIG_ID, NuveiMerchantConfigurationModel.MERCHANTSITEID, MERCHANT_SITE_ID))))
				.thenReturn(List.of(nuveiMerchantConfigurationModelMock));

		final NuveiMerchantConfigurationModel result = testObj.getMerchantConfigurationByMerchantIdAndSiteId(MERCHANT_CONFIG_ID, MERCHANT_SITE_ID);

		assertThat(result).isEqualTo(nuveiMerchantConfigurationModelMock);
	}

	@Test
	public void getMerchantConfigurationByMerchantIdAndSiteId__ShouldThrowException_WhenMultipleMerchantConfigurationFound() {
		when(merchantConfigurationDaoMock.find((Map.of(NuveiMerchantConfigurationModel.MERCHANTID, MERCHANT_CONFIG_ID, NuveiMerchantConfigurationModel.MERCHANTSITEID, MERCHANT_SITE_ID))))
				.thenReturn(List.of(nuveiMerchantConfigurationModelMock, secondMerchantConfigurationModelMock));

		assertThatThrownBy(() -> testObj.getMerchantConfigurationByMerchantIdAndSiteId(MERCHANT_CONFIG_ID, MERCHANT_SITE_ID))
				.isInstanceOf(AmbiguousIdentifierException.class)
				.hasMessage(format(MULTIPLE_MERCHANT_CONFIGURATION_FOUND, MERCHANT_CONFIG_ID, 2));
	}

	@Test
	public void getMerchantConfigurationByMerchantIdAndSiteId__ShouldThrowException_WhenNoMerchantConfigurationFound() {
		when(merchantConfigurationDaoMock.find((Map.of(NuveiMerchantConfigurationModel.MERCHANTID, MERCHANT_CONFIG_ID, NuveiMerchantConfigurationModel.MERCHANTSITEID, MERCHANT_SITE_ID))))
				.thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> testObj.getMerchantConfigurationByMerchantIdAndSiteId(MERCHANT_CONFIG_ID, MERCHANT_SITE_ID))
				.isInstanceOf(UnknownIdentifierException.class)
				.hasMessage(format(MERCHANT_CONFIGURATION_NOT_FOUND, MERCHANT_CONFIG_ID));
	}

	@Test
	public void getMerchantConfigurationByMerchantIdAndSiteId_ShouldThrowException_WhenTheGivenIdIsEmpty() {
		assertThatThrownBy(() -> testObj.getMerchantConfigurationByMerchantIdAndSiteId(StringUtils.EMPTY, MERCHANT_SITE_ID))
				.isInstanceOf(UnknownIdentifierException.class)
				.hasMessage(format(MERCHANT_CONFIGURATION_NOT_FOUND, StringUtils.EMPTY));
	}

	@Test
	public void getMerchantConfigurationByMerchantIdAndSiteId_ShouldThrowException_WhenTheGivenIdIsNull() {
		assertThatThrownBy(() -> testObj.getMerchantConfigurationByMerchantIdAndSiteId(null, MERCHANT_SITE_ID))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(ID_MUST_NOT_BE_NULL);
	}

	@Test
	public void getMerchantConfigurationByMerchantIdAndSiteId_ShouldThrowException_WhenTheGivenSiteIdIsNull() {
		assertThatThrownBy(() -> testObj.getMerchantConfigurationByMerchantIdAndSiteId(MERCHANT_CONFIG_ID, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(SITE_ID_MUST_NOT_BE_NULL);
	}
}
