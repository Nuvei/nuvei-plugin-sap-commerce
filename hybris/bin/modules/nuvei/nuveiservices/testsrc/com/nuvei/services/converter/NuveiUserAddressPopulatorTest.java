package com.nuvei.services.converter;

import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.safecharge.model.UserAddress;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiUserAddressPopulatorTest {

    private static final String TOWN = "Town";
    private static final String EMAIL = "Email";
    private static final String LINE_1 = "Line 1";
    private static final String LINE_2 = "Line 2";
    private static final String PHONE_1 = "Phone1";
    private static final String LAST_NAME = "LastName";
    private static final String CELLPHONE = "Cellphone";
    private static final String FIRST_NAME = "FirstName";
    private static final String POSTAL_CODE = "PostalCode";
    private static final String REGION_ISO_CODE = "RegionIsoCode";
    private static final String COUNTRY_ISO_CODE = "CountryIsoCode";

    @InjectMocks
    private NuveiUserAddressPopulator testObj;

    @Mock
    private RegionModel regionModelMock;
    @Mock
    private CountryModel countryModelMock;
    @Mock
    private AddressModel addressModelMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NuveiMerchantConfigurationService nuveiMerchantConfigurationServiceMock;

    private final UserAddress target = new UserAddress();

    @Before
    public void setUp() {
        when(addressModelMock.getTown()).thenReturn(TOWN);
        when(addressModelMock.getEmail()).thenReturn(EMAIL);
        when(addressModelMock.getLine1()).thenReturn(LINE_1);
        when(addressModelMock.getLine2()).thenReturn(LINE_2);
        when(addressModelMock.getPhone1()).thenReturn(PHONE_1);
        when(addressModelMock.getLastname()).thenReturn(LAST_NAME);
        when(addressModelMock.getCellphone()).thenReturn(CELLPHONE);
        when(addressModelMock.getFirstname()).thenReturn(FIRST_NAME);
        when(addressModelMock.getPostalcode()).thenReturn(POSTAL_CODE);
        when(addressModelMock.getPostalcode()).thenReturn(POSTAL_CODE);
        when(addressModelMock.getRegion()).thenReturn(regionModelMock);
        when(addressModelMock.getCountry()).thenReturn(countryModelMock);
        when(countryModelMock.getIsocode()).thenReturn(COUNTRY_ISO_CODE);
        when(regionModelMock.getIsocodeShort()).thenReturn(REGION_ISO_CODE);
    }

    @Test
    public void populate_ShouldPopulateAllFieldsWhenPrepopulateCardHolderNameIsTrue() {
        when(nuveiMerchantConfigurationServiceMock.getCurrentConfiguration().getPrePopulateFullName()).thenReturn(true);

        testObj.populate(addressModelMock, target);

        assertThat(target.getCity()).isEqualTo(TOWN);
        assertThat(target.getEmail()).isEqualTo(EMAIL);
        assertThat(target.getPhone()).isEqualTo(PHONE_1);
        assertThat(target.getCell()).isEqualTo(CELLPHONE);
        assertThat(target.getZip()).isEqualTo(POSTAL_CODE);
        assertThat(target.getLastName()).isEqualTo(LAST_NAME);
        assertThat(target.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(target.getState()).isEqualTo(REGION_ISO_CODE);
        assertThat(target.getAddress()).isEqualTo(LINE_1 + StringUtils.SPACE + LINE_2);
        assertThat(target.getCountry()).isEqualTo(COUNTRY_ISO_CODE);
    }

    @Test
    public void populate_ShouldPopulateAllFieldsButFirstAndLastNameWhenPrepopulateCardHolderNameIsFalse() {
        testObj.populate(addressModelMock, target);

        assertThat(target.getCity()).isEqualTo(TOWN);
        assertThat(target.getEmail()).isEqualTo(EMAIL);
        assertThat(target.getPhone()).isEqualTo(PHONE_1);
        assertThat(target.getCell()).isEqualTo(CELLPHONE);
        assertThat(target.getZip()).isEqualTo(POSTAL_CODE);
        assertThat(target.getLastName()).isNullOrEmpty();
        assertThat(target.getFirstName()).isNullOrEmpty();
        assertThat(target.getState()).isEqualTo(REGION_ISO_CODE);
        assertThat(target.getAddress()).isEqualTo(LINE_1 + StringUtils.SPACE + LINE_2);
        assertThat(target.getCountry()).isEqualTo(COUNTRY_ISO_CODE);
    }
}
