package com.nuvei.facades.checkoutsdk.impl;

import com.google.common.collect.Lists;
import com.nuvei.facades.beans.NuveiBlockedCardsData;
import com.nuvei.facades.beans.NuveiCheckoutSDKRequestData;
import com.nuvei.facades.checkoutsdk.NuveiCheckoutSDKRequestBuilder;
import com.nuvei.services.enums.NuveiEnv;
import com.nuvei.services.enums.NuveiFilterType;
import com.nuvei.services.enums.NuveiLogLevel;
import com.nuvei.services.enums.NuveiPayButton;
import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiBlockedCardsModel;
import com.nuvei.services.model.NuveiI18nLabelModel;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.model.NuveiPaymentMethodModel;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.core.model.c2l.C2LItemModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.assertj.core.util.Preconditions.checkArgument;

public class DefaultNuveiCheckoutSDKRequestBuilder implements NuveiCheckoutSDKRequestBuilder {

    private static final String USER_UID_CAN_NEITHER_BE_NULL_NOR_EMPTY = "User uid can neither be null nor empty";
    private static final String CURRENCY_ISO_CAN_NEITHER_BE_NULL_NOR_EMPTY = "CurrencyIso can neither be null nor empty";
    private static final String COUNTRY_ISO_CAN_NEITHER_BE_NULL_NOR_EMPTY = "CountryIso can neither be null nor empty";
    private static final String VALUE_TOKEN = "VALUE_";

    private final UserFacade userFacade;
    private final CartService cartService;
    private final CommerceCommonI18NService commerceCommonI18NService;
    private final NuveiMerchantConfigurationService nuveiMerchantConfigurationService;
    private final Converter<NuveiBlockedCardsModel, NuveiBlockedCardsData> nuveiBlockedCardsConverter;

    public DefaultNuveiCheckoutSDKRequestBuilder(final UserFacade userFacade,
                                                 final CartService cartService,
                                                 final CommerceCommonI18NService commerceCommonI18NService,
                                                 final NuveiMerchantConfigurationService nuveiMerchantConfigurationService,
                                                 final Converter<NuveiBlockedCardsModel, NuveiBlockedCardsData> nuveiBlockedCardsConverter) {
        this.userFacade = userFacade;
        this.cartService = cartService;
        this.commerceCommonI18NService = commerceCommonI18NService;
        this.nuveiBlockedCardsConverter = nuveiBlockedCardsConverter;
        this.nuveiMerchantConfigurationService = nuveiMerchantConfigurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NuveiCheckoutSDKRequestData getCheckoutSDKRequestData(final String sessionToken) {
        final NuveiCheckoutSDKRequestData requestData = new NuveiCheckoutSDKRequestData();

        populateMerchantData(requestData);

        final CartModel sessionCart = cartService.getSessionCart();
        final AddressModel paymentAddress = sessionCart.getPaymentAddress();
        final Double amount = sessionCart.getTotalPrice();
        final String fullName = getFullName(paymentAddress);
        final String countryIso = getCartCountryIsoCode(paymentAddress);
        final String userUid = getCartUserUid(sessionCart);
        final String currencyIsoCode = getCartCurrencyIsoCode(sessionCart);

        checkArgument(isNotBlank(userUid), USER_UID_CAN_NEITHER_BE_NULL_NOR_EMPTY);
        checkArgument(isNotBlank(countryIso), COUNTRY_ISO_CAN_NEITHER_BE_NULL_NOR_EMPTY);
        checkArgument(isNotBlank(currencyIsoCode), CURRENCY_ISO_CAN_NEITHER_BE_NULL_NOR_EMPTY);

        requestData.setCurrency(currencyIsoCode);
        prePopulateCardholderName(requestData, fullName);
        requestData.setUserTokenId(userUid);
        requestData.setCountry(countryIso);
        requestData.setAmount(String.valueOf(amount));
        requestData.setSessionToken(sessionToken);
        requestData.setLocale(getCurrentLocale());

        return requestData;
    }

    /**
     * Pre-populates the cardholder name on the call to nuvei in case the current configuration on {@link NuveiMerchantConfigurationModel} is true
     *
     * @param requestData the {@link NuveiCheckoutSDKRequestData}
     * @param fullName    the fullname
     */
    protected void prePopulateCardholderName(final NuveiCheckoutSDKRequestData requestData, final String fullName) {
        final NuveiMerchantConfigurationModel currentConfiguration = nuveiMerchantConfigurationService.getCurrentConfiguration();
        if (Boolean.TRUE.equals(currentConfiguration.getPrePopulateFullName())) {
            requestData.setFullName(fullName);
        }
    }

    /**
     * Populates the data from the current storefront merchant configuration into the given {@link NuveiCheckoutSDKRequestData}
     *
     * @param requestData The requestData with the information from the merchant configuration populated
     */
    protected void populateMerchantData(final NuveiCheckoutSDKRequestData requestData) {
        final NuveiMerchantConfigurationModel merchantConfig
                = nuveiMerchantConfigurationService.getCurrentConfiguration();
        final List<List<String>> blockedCardsCartesianProducts = getBlockCardsCartesianProduct(merchantConfig);

        requestData.setMerchantId(merchantConfig.getMerchantId());
        requestData.setMerchantSiteId(merchantConfig.getMerchantSiteId());
        requestData.setSavePM(userFacade.isAnonymousUser() ? Boolean.FALSE : merchantConfig.getSavePM());
        requestData.setI18n(getLabels(merchantConfig));
        requestData.setAlwaysCollectCvv(merchantConfig.getAlwaysCollectCvv());
        requestData.setBlockCards(blockedCardsCartesianProducts);
        Optional.ofNullable(merchantConfig.getPayButton())
                .map(NuveiPayButton::getCode)
                .ifPresent(requestData::setPayButton);
        Optional.ofNullable(merchantConfig.getEnv())
                .map(NuveiEnv::getCode)
                .ifPresent(requestData::setEnv);
        Optional.ofNullable(merchantConfig.getLogLevel())
                .map(NuveiLogLevel::getCode)
                .map(this::removeValueTokenFromLogLevel)
                .ifPresent(requestData::setLogLevel);
        setAllowOrDenyList(requestData, merchantConfig);
    }

    /**
     * Will return the cartesian product calculated with the lists of values taken from card brand, product, type and country
     *
     * @param merchantConfig the merchant config to calculate the cartesian product
     * @return a list which contains all the permutations generated with the given lists
     */
    protected List<List<String>> getBlockCardsCartesianProduct(final NuveiMerchantConfigurationModel merchantConfig) {
        final List<NuveiBlockedCardsData> blockedCards = nuveiBlockedCardsConverter.convertAll(merchantConfig.getBlockedCards());
        final List<List<String>> blockedCardsCartesianProducts = new ArrayList<>();
        blockedCards.stream().forEach(blockedCard -> {
            final List<List<String>> currentSet = new ArrayList<>();
            Optional.ofNullable(blockedCard.getBrands())
                    .filter(CollectionUtils::isNotEmpty)
                    .ifPresent(currentSet::add);
            Optional.ofNullable(blockedCard.getCardProducts())
                    .filter(CollectionUtils::isNotEmpty)
                    .ifPresent(currentSet::add);
            Optional.ofNullable(blockedCard.getCardTypes())
                    .filter(CollectionUtils::isNotEmpty)
                    .ifPresent(currentSet::add);
            Optional.ofNullable(blockedCard.getCountries())
                    .filter(CollectionUtils::isNotEmpty)
                    .ifPresent(currentSet::add);
            blockedCardsCartesianProducts.addAll(new ArrayList<>(Lists.cartesianProduct(currentSet)));
        });
        return blockedCardsCartesianProducts;
    }

    /**
     * Replaces the given logLevel string, removing the value token (VALUE_)
     *
     * @param logLevel The log level string to replace
     * @return The replaced log level string
     */
    protected String removeValueTokenFromLogLevel(final String logLevel) {
        return StringUtils.isNotEmpty(logLevel) ? logLevel.replace(VALUE_TOKEN, StringUtils.EMPTY) : logLevel;
    }

    /**
     * Gets the translations labels from the given {@link NuveiMerchantConfigurationModel}
     *
     * @param nuveiMerchantConfig The merchant configuration to obtain the labels from
     * @return The labels
     */
    protected Map<String, String> getLabels(final NuveiMerchantConfigurationModel nuveiMerchantConfig) {
        return nuveiMerchantConfig.getLabels()
                .stream()
                .collect(Collectors.toMap(NuveiI18nLabelModel::getKey, NuveiI18nLabelModel::getValue));
    }

    /**
     * Sets the allow or deny list to the requestData based on the FilterType attribute of the merchantConfig
     *
     * @param nuveiCheckoutSDKRequestData The Nuvei checkoutSDKRequestData
     * @param nuveiMerchantConfig         The NuveiMerchantConfiguration
     */
    protected void setAllowOrDenyList(final NuveiCheckoutSDKRequestData nuveiCheckoutSDKRequestData,
                                      final NuveiMerchantConfigurationModel nuveiMerchantConfig) {
        if (NuveiFilterType.ALLOWLIST.equals(nuveiMerchantConfig.getFilterType())) {
            nuveiCheckoutSDKRequestData.setPmWhitelist(nuveiMerchantConfig.getAllowListPaymentMethods()
                    .stream()
                    .map(NuveiPaymentMethodModel::getId)
                    .collect(Collectors.toList())
            );
        } else if (NuveiFilterType.DENYLIST.equals(nuveiMerchantConfig.getFilterType())) {
            nuveiCheckoutSDKRequestData.setPmBlacklist(nuveiMerchantConfig.getDenyListPaymentMethods()
                    .stream()
                    .map(NuveiPaymentMethodModel::getId)
                    .collect(Collectors.toList())
            );
        }
    }

    /**
     * Gets the current language ISO Code or an empty string if not set
     *
     * @return The current language ISO code if present, null string otherwise
     */
    protected String getCurrentLocale() {
        return Optional.ofNullable(commerceCommonI18NService.getCurrentLanguage())
                .map(C2LItemModel::getIsocode)
                .orElse(null);
    }

    /**
     * Gets currency from the session cart
     *
     * @param sessionCart The session cart
     * @return The session cart currency ISO code if present, null string otherwise
     */
    protected String getCartCurrencyIsoCode(final CartModel sessionCart) {
        return Optional.ofNullable(sessionCart.getCurrency())
                .map(C2LItemModel::getIsocode)
                .orElse(null);
    }

    /**
     * Gets the user uid from the session cart
     *
     * @param sessionCart The session cart
     * @return The session cart user uid if present, null string otherwise
     */
    protected String getCartUserUid(final CartModel sessionCart) {
        return Optional.ofNullable(sessionCart.getUser())
                .filter(CustomerModel.class::isInstance)
                .map(CustomerModel.class::cast)
                .map(CustomerModel::getCustomerID)
                .orElse(null);
    }

    /**
     * Gets country iso code of the billing address from the session cart
     *
     * @param billingAddress The billing address from the session cart
     * @return The session cart country iso if present, null otherwise
     */
    protected String getCartCountryIsoCode(final AddressModel billingAddress) {
        return Optional.ofNullable(billingAddress)
                .map(AddressModel::getCountry)
                .map(C2LItemModel::getIsocode)
                .orElse(null);
    }

    /**
     * Gets the full name of the billing address from the session cart
     *
     * @param billingAddress The session cart billing address
     * @return The billing address full name if present, empty string otherwise
     */
    protected String getFullName(final AddressModel billingAddress) {
        final StringBuilder fullName = new StringBuilder(Optional.ofNullable(billingAddress.getFirstname())
                .orElse(StringUtils.EMPTY));
        if (StringUtils.isNotBlank(billingAddress.getLastname()) && StringUtils.isNotBlank(billingAddress.getFirstname())) {
            fullName.append(StringUtils.SPACE);
        }
        if (StringUtils.isNotBlank(billingAddress.getLastname())) {
            fullName.append(billingAddress.getLastname());
        }
        return fullName.toString();
    }
}
