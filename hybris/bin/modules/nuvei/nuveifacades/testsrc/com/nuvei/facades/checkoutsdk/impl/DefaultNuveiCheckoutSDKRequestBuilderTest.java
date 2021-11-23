package com.nuvei.facades.checkoutsdk.impl;

import com.nuvei.facades.beans.NuveiBlockedCardsData;
import com.nuvei.facades.beans.NuveiCheckoutSDKRequestData;
import com.nuvei.services.enums.NuveiEnv;
import com.nuvei.services.enums.NuveiFilterType;
import com.nuvei.services.enums.NuveiLogLevel;
import com.nuvei.services.enums.NuveiPayButton;
import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiBlockedCardsModel;
import com.nuvei.services.model.NuveiI18nLabelModel;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.model.NuveiPaymentMethodModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiCheckoutSDKRequestBuilderTest {

    private static final String ZERO = "0";
    private static final String TOTAL_PRICE = "120.0";
    private static final String LAST_NAME = "lastName";
    private static final String FIRST_NAME = "firstName";
    private static final String CUSTOMER_ID = "customerId";
    private static final String FIRST_BRAND = "firstBrand";
    private static final String MERCHANT_ID = "merchantId";
    private static final String SECOND_BRAND = "secondBrand";
    private static final String SESSION_TOKEN = "sessionToken";
    private static final String FIRST_CARD_TYPE = "firstCardType";
    private static final String FIRST_LABEL_KEY = "firstLabelKey";
    private static final String COUNTRY_ISO_CODE = "countryIsoCode";
    private static final String MERCHANT_SITE_ID = "merchantSiteId";
    private static final String SECOND_LABEL_KEY = "secondLabelKey";
    private static final String SECOND_CARD_TYPE = "secondCardType";
    private static final String CURRENCY_ISO_CODE = "currencyIsoCode";
    private static final String LANGUAGE_ISO_CODE = "languageIsoCode";
    private static final String FIRST_LABEL_VALUE = "firstLabelValue";
    private static final String SECOND_LABEL_VALUE = "secondLabelValue";
    private static final String FIRST_CARD_PRODUCT = "firstCardProduct";
    private static final String FIRST_CARD_COUNTRY = "firstCardCountry";
    private static final String DENY_PAYMENT_METHOD = "denyPaymentMethod";
    private static final String SECOND_CARD_COUNTRY = "secondCardCountry";
    private static final String SECOND_CARD_PRODUCT = "secondCardProduct";
    private static final String ALLOWED_PAYMENT_METHOD = "allowedPaymentMethod";
    private static final String EXPECTED_FULL_NAME = FIRST_NAME + StringUtils.SPACE + LAST_NAME;

    private static final double TOTAL_PRICE_NUMBER = 120d;

    @InjectMocks
    private DefaultNuveiCheckoutSDKRequestBuilder testObj;

    @Mock
    private UserFacade userFacade;
    @Mock
    private CartService cartServiceMock;
    @Mock
    private CommerceCommonI18NService commerceCommonI18NServiceMock;
    @Mock
    private NuveiMerchantConfigurationService nuveiMerchantConfigurationServiceMock;
    @Mock
    private Converter<NuveiBlockedCardsModel, NuveiBlockedCardsData> nuveiBlockedCardsConverterMock;

    @Mock
    private UserModel userModelMock;
    @Mock
    private CartModel sessionCartMock;
    @Mock
    private CountryModel countryModelMock;
    @Mock
    private AddressModel paymentAddressMock;
    @Mock
    private CustomerModel customerModelMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private LanguageModel languageModelMock;
    @Mock
    private NuveiI18nLabelModel firstLabelMock, secondLabelMock;
    @Mock
    private NuveiMerchantConfigurationModel currentMerchantConfigurationMock;
    @Mock
    private NuveiBlockedCardsModel firstBlockedCardMock, secondBlockedCardMock;
    @Mock
    private NuveiBlockedCardsData firstBlockedCardDataMock, secondBlockedCardDataMock;
    @Mock
    private NuveiPaymentMethodModel allowNuveiPaymentMethodMock, denyNuveiPaymentMethodMock;

    private final NuveiCheckoutSDKRequestData requestData = new NuveiCheckoutSDKRequestData();
    private final List<List<String>> cartesianResult = List.of(
            List.of(FIRST_BRAND, FIRST_CARD_PRODUCT, FIRST_CARD_TYPE, FIRST_CARD_COUNTRY),
            List.of(SECOND_BRAND, FIRST_CARD_PRODUCT, FIRST_CARD_TYPE, FIRST_CARD_COUNTRY),
            List.of(SECOND_BRAND, SECOND_CARD_PRODUCT, SECOND_CARD_TYPE, SECOND_CARD_COUNTRY));

    @Before
    public void setUp() {

        when(nuveiMerchantConfigurationServiceMock.getCurrentConfiguration())
                .thenReturn(currentMerchantConfigurationMock);
        when(currentMerchantConfigurationMock.getMerchantId()).thenReturn(MERCHANT_ID);
        when(currentMerchantConfigurationMock.getMerchantSiteId()).thenReturn(MERCHANT_SITE_ID);
        when(currentMerchantConfigurationMock.getSavePM()).thenReturn(Boolean.TRUE);
        when(currentMerchantConfigurationMock.getPayButton()).thenReturn(NuveiPayButton.AMOUNTBUTTON);
        when(currentMerchantConfigurationMock.getEnv()).thenReturn(NuveiEnv.INT);
        when(currentMerchantConfigurationMock.getLogLevel()).thenReturn(NuveiLogLevel.VALUE_0);
        when(currentMerchantConfigurationMock.getLabels()).thenReturn(List.of(firstLabelMock, secondLabelMock));
        when(currentMerchantConfigurationMock.getBlockedCards()).thenReturn(List.of(firstBlockedCardMock, secondBlockedCardMock));
        when(currentMerchantConfigurationMock.getAllowListPaymentMethods()).thenReturn(Set.of(allowNuveiPaymentMethodMock));
        when(currentMerchantConfigurationMock.getDenyListPaymentMethods()).thenReturn(Set.of(denyNuveiPaymentMethodMock));
        when(currentMerchantConfigurationMock.getAlwaysCollectCvv()).thenReturn(Boolean.TRUE);
        when(allowNuveiPaymentMethodMock.getId()).thenReturn(ALLOWED_PAYMENT_METHOD);
        when(denyNuveiPaymentMethodMock.getId()).thenReturn(DENY_PAYMENT_METHOD);
        when(cartServiceMock.getSessionCart()).thenReturn(sessionCartMock);
        when(sessionCartMock.getPaymentAddress()).thenReturn(paymentAddressMock);
        when(sessionCartMock.getTotalPrice()).thenReturn(TOTAL_PRICE_NUMBER);
        when(sessionCartMock.getCurrency()).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(CURRENCY_ISO_CODE);
        when(sessionCartMock.getUser()).thenReturn(customerModelMock);
        when(commerceCommonI18NServiceMock.getCurrentLanguage()).thenReturn(languageModelMock);
        when(languageModelMock.getIsocode()).thenReturn(LANGUAGE_ISO_CODE);
        when(customerModelMock.getCustomerID()).thenReturn(CUSTOMER_ID);
        when(paymentAddressMock.getCountry()).thenReturn(countryModelMock);
        when(paymentAddressMock.getFirstname()).thenReturn(FIRST_NAME);
        when(paymentAddressMock.getLastname()).thenReturn(LAST_NAME);
        when(countryModelMock.getIsocode()).thenReturn(COUNTRY_ISO_CODE);
        when(firstLabelMock.getKey()).thenReturn(FIRST_LABEL_KEY);
        when(firstLabelMock.getValue()).thenReturn(FIRST_LABEL_VALUE);
        when(secondLabelMock.getKey()).thenReturn(SECOND_LABEL_KEY);
        when(secondLabelMock.getValue()).thenReturn(SECOND_LABEL_VALUE);
        when(nuveiBlockedCardsConverterMock.convertAll(List.of(firstBlockedCardMock, secondBlockedCardMock)))
                .thenReturn(List.of(firstBlockedCardDataMock, secondBlockedCardDataMock));
        when(firstBlockedCardDataMock.getBrands()).thenReturn(List.of(FIRST_BRAND));
        when(firstBlockedCardDataMock.getCardProducts()).thenReturn(List.of(FIRST_CARD_PRODUCT));
        when(firstBlockedCardDataMock.getCardTypes()).thenReturn(List.of(FIRST_CARD_TYPE));
        when(firstBlockedCardDataMock.getCountries()).thenReturn(List.of(FIRST_CARD_COUNTRY));
        when(secondBlockedCardDataMock.getBrands()).thenReturn(List.of(SECOND_BRAND));
        when(secondBlockedCardDataMock.getCardProducts()).thenReturn(List.of(SECOND_CARD_PRODUCT));
        when(secondBlockedCardDataMock.getCardTypes()).thenReturn(List.of(SECOND_CARD_TYPE));
        when(secondBlockedCardDataMock.getCountries()).thenReturn(List.of(SECOND_CARD_COUNTRY));
        when(userFacade.isAnonymousUser()).thenReturn(Boolean.FALSE);
    }

    @Test
    public void populateMerchantData_ShouldPopulateMerchantDataAndSetSavePmToTrue_WhenUserIsNotAnonymous() {
        when(currentMerchantConfigurationMock.getFilterType()).thenReturn(NuveiFilterType.ALLOWLIST);

        testObj.populateMerchantData(requestData);

        assertThat(requestData.getSavePM()).isTrue();
        assertThat(requestData.getPmBlacklist()).isNull();
        assertThat(requestData.getLogLevel()).isEqualTo(ZERO);
        assertThat(requestData.getMerchantId()).isEqualTo(MERCHANT_ID);
        assertThat(requestData.getEnv()).isEqualTo(NuveiEnv.INT.getCode());
        assertThat(requestData.getAlwaysCollectCvv()).isEqualTo(Boolean.TRUE);
        assertThat(requestData.getMerchantSiteId()).isEqualTo(MERCHANT_SITE_ID);
        assertThat(requestData.getPmWhitelist()).isEqualTo(List.of(ALLOWED_PAYMENT_METHOD));
        assertThat(requestData.getPayButton()).isEqualTo(NuveiPayButton.AMOUNTBUTTON.getCode());
        assertThat(requestData.getI18n())
                .isEqualTo(Map.of(FIRST_LABEL_KEY, FIRST_LABEL_VALUE, SECOND_LABEL_KEY, SECOND_LABEL_VALUE));
    }

    @Test
    public void populateMerchantData_ShouldPopulateMerchantDataAndSetSavePmToFalse_WhenUserIsAnonymous() {
        when(userFacade.isAnonymousUser()).thenReturn(Boolean.TRUE);
        when(currentMerchantConfigurationMock.getFilterType()).thenReturn(NuveiFilterType.ALLOWLIST);

        testObj.populateMerchantData(requestData);

        assertThat(requestData.getSavePM()).isFalse();
        assertThat(requestData.getPmBlacklist()).isNull();
        assertThat(requestData.getLogLevel()).isEqualTo(ZERO);
        assertThat(requestData.getMerchantId()).isEqualTo(MERCHANT_ID);
        assertThat(requestData.getEnv()).isEqualTo(NuveiEnv.INT.getCode());
        assertThat(requestData.getAlwaysCollectCvv()).isEqualTo(Boolean.TRUE);
        assertThat(requestData.getMerchantSiteId()).isEqualTo(MERCHANT_SITE_ID);
        assertThat(requestData.getPmWhitelist()).isEqualTo(List.of(ALLOWED_PAYMENT_METHOD));
        assertThat(requestData.getPayButton()).isEqualTo(NuveiPayButton.AMOUNTBUTTON.getCode());
        assertThat(requestData.getI18n())
                .isEqualTo(Map.of(
                        FIRST_LABEL_KEY, FIRST_LABEL_VALUE,
                        SECOND_LABEL_KEY, SECOND_LABEL_VALUE)
                );
    }

    @Test
    public void getBlockCardsCartesianProduct() {
        when(firstBlockedCardDataMock.getBrands()).thenReturn(List.of(FIRST_BRAND, SECOND_BRAND));
        final List<List<String>> result = testObj.getBlockCardsCartesianProduct(currentMerchantConfigurationMock);
        assertThat(result).isEqualTo(cartesianResult);
    }

    @Test
    public void setAllowOrDenyList_ShouldSetPMBlackList_WhenFilterTypeIsDenyList() {
        when(currentMerchantConfigurationMock.getFilterType()).thenReturn(NuveiFilterType.ALLOWLIST);

        testObj.setAllowOrDenyList(requestData, currentMerchantConfigurationMock);

        assertThat(requestData.getPmBlacklist()).isNull();
        assertThat(requestData.getPmWhitelist()).containsOnly(ALLOWED_PAYMENT_METHOD);
    }

    @Test
    public void setAllowOrDenyList_ShouldSetPMWhiteList_WhenFilterTypeIsAllowList() {
        when(currentMerchantConfigurationMock.getFilterType()).thenReturn(NuveiFilterType.DENYLIST);

        testObj.setAllowOrDenyList(requestData, currentMerchantConfigurationMock);

        assertThat(requestData.getPmBlacklist()).containsOnly(DENY_PAYMENT_METHOD);
        assertThat(requestData.getPmWhitelist()).isNull();
    }

    @Test
    public void setAllowOrDenyList_ShouldNotSetPMBlackListAndPMWhiteList_WhenFilterTypeIsNoFilter() {
        when(currentMerchantConfigurationMock.getFilterType()).thenReturn(NuveiFilterType.NOFILTER);

        testObj.setAllowOrDenyList(requestData, currentMerchantConfigurationMock);

        assertThat(requestData.getPmBlacklist()).isNull();
        assertThat(requestData.getPmWhitelist()).isNull();
    }

    @Test
    public void getCheckoutSDKRequestData_ShouldPopulateCheckoutRequestDataWithFullNameWhenPrepoluateFullNameIsEnabled() {
        when(currentMerchantConfigurationMock.getPrePopulateFullName()).thenReturn(true);

        final NuveiCheckoutSDKRequestData result = testObj.getCheckoutSDKRequestData(SESSION_TOKEN);

        assertThat(result.getAmount()).isEqualTo(TOTAL_PRICE);
        assertThat(result.getUserTokenId()).isEqualTo(CUSTOMER_ID);
        assertThat(result.getCountry()).isEqualTo(COUNTRY_ISO_CODE);
        assertThat(result.getLocale()).isEqualTo(LANGUAGE_ISO_CODE);
        assertThat(result.getSessionToken()).isEqualTo(SESSION_TOKEN);
        assertThat(result.getCurrency()).isEqualTo(CURRENCY_ISO_CODE);
        assertThat(result.getFullName()).isEqualTo(EXPECTED_FULL_NAME);
    }

    @Test
    public void getCheckoutSDKRequestData_ShouldPopulateCheckoutRequestDataWithoutFullNameWhenPrepoluateFullNameIsNotEnabled() {
        final NuveiCheckoutSDKRequestData result = testObj.getCheckoutSDKRequestData(SESSION_TOKEN);

        assertThat(result.getAmount()).isEqualTo(TOTAL_PRICE);
        assertThat(result.getUserTokenId()).isEqualTo(CUSTOMER_ID);
        assertThat(result.getCountry()).isEqualTo(COUNTRY_ISO_CODE);
        assertThat(result.getLocale()).isEqualTo(LANGUAGE_ISO_CODE);
        assertThat(result.getSessionToken()).isEqualTo(SESSION_TOKEN);
        assertThat(result.getCurrency()).isEqualTo(CURRENCY_ISO_CODE);
        assertThat(result.getFullName()).isNullOrEmpty();
    }

    @Test
    public void getCurrentLocale_ShouldReturnCurrentLocaleIsoCode_WhenItIsNotNull() {
        final String result = testObj.getCurrentLocale();

        assertThat(result).isEqualTo(LANGUAGE_ISO_CODE);
    }

    @Test
    public void getCurrentLocale_ShouldReturnNull_WhenCurrentLocaleIsNull() {
        when(commerceCommonI18NServiceMock.getCurrentLanguage()).thenReturn(null);

        final String result = testObj.getCurrentLocale();

        assertThat(result).isNull();
    }

    @Test
    public void getCartCurrencyIsoCode_ShouldReturnCurrencyIsoCode_WhenSessionCartHasCurrency() {
        final String result = testObj.getCartCurrencyIsoCode(sessionCartMock);

        assertThat(result).isEqualTo(CURRENCY_ISO_CODE);
    }

    @Test
    public void getCartCurrencyIsoCode_ShouldReturnNull_WhenSessionCartHasNoCurrency() {
        when(sessionCartMock.getCurrency()).thenReturn(null);

        final String result = testObj.getCartCurrencyIsoCode(sessionCartMock);

        assertThat(result).isNull();
    }

    @Test
    public void getCartUserUid_ShouldReturnSessionCartCustomerID_WhenSessionCartUserIsACustomerAndHasCustomerID() {
        final String result = testObj.getCartUserUid(sessionCartMock);

        assertThat(result).isEqualTo(CUSTOMER_ID);
    }

    @Test
    public void getCartUserUid_ShouldReturnNull_WhenSessionCartUserIsNull() {
        when(sessionCartMock.getUser()).thenReturn(null);

        final String result = testObj.getCartUserUid(sessionCartMock);

        assertThat(result).isNull();
    }

    @Test
    public void getCartUserUid_ShouldReturnNull_WhenSessionCartUserIsNotACustomer() {
        when(sessionCartMock.getUser()).thenReturn(userModelMock);

        final String result = testObj.getCartUserUid(sessionCartMock);

        assertThat(result).isNull();
    }

    @Test
    public void getCartUserUid_ShouldReturnNull_WhenSessionCartUserIsACustomerAndHasNoCustomerID() {
        when(sessionCartMock.getUser()).thenReturn(customerModelMock);
        when(customerModelMock.getCustomerID()).thenReturn(null);

        final String result = testObj.getCartUserUid(sessionCartMock);

        assertThat(result).isNull();
    }

    @Test
    public void getCartCountryIsoCode_ShouldReturnBillingAddressCountryIsoCode_WhenBillingAddressHasCountry() {
        final String result = testObj.getCartCountryIsoCode(paymentAddressMock);

        assertThat(result).isEqualTo(COUNTRY_ISO_CODE);
    }

    @Test
    public void getCartCountryIsoCode_ShouldReturnNull_WhenBillingAddresHasNoCountry() {
        when(paymentAddressMock.getCountry()).thenReturn(null);

        final String result = testObj.getCartCountryIsoCode(paymentAddressMock);

        assertThat(result).isNull();
    }

    @Test
    public void getFullName_ShouldReturnFullName_WhenBillingAddressHasFirstNameAndLastName() {
        final String result = testObj.getFullName(paymentAddressMock);

        assertThat(result).isEqualTo(FIRST_NAME + StringUtils.SPACE + LAST_NAME);
    }

    @Test
    public void getFullName_ShouldReturnLastName_WhenBillingAddressHasNoFirstNameAndHasLastName() {
        when(paymentAddressMock.getFirstname()).thenReturn(null);

        final String result = testObj.getFullName(paymentAddressMock);

        assertThat(result).isEqualTo(LAST_NAME);
    }

    @Test
    public void getFullName_ShouldReturnFistName_WhenBillingAddressHasFirstNameAndHasNoLastName() {
        when(paymentAddressMock.getLastname()).thenReturn(null);

        final String result = testObj.getFullName(paymentAddressMock);

        assertThat(result).isEqualTo(FIRST_NAME);
    }

    @Test
    public void getFullName_ShouldReturnEmptyString_WhenBillingAddressHasNoFisrtNameAndLastName() {
        when(paymentAddressMock.getFirstname()).thenReturn(null);
        when(paymentAddressMock.getLastname()).thenReturn(null);

        final String result = testObj.getFullName(paymentAddressMock);

        assertThat(result).isEqualTo(StringUtils.EMPTY);
    }
}
