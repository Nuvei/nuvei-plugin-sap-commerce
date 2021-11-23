package com.nuvei.services.openorder.impl;

import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.openorder.NuveiOpenOrderService;
import com.nuvei.services.wrapper.NuveiSafechargeWrapper;
import com.safecharge.exception.SafechargeException;
import com.safecharge.model.UserAddress;
import de.hybris.platform.core.model.c2l.C2LItemModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class DefaultNuveiOpenOrderService implements NuveiOpenOrderService {

    private static final String PAYMENT_ADDRESS_CANT_BE_NULL = "PaymentAddress of session cart %s can't  be null";
    private static final String DELIVERY_ADDRESS_CANT_BE_NULL = "DeliveryAddress of session cart %s can't  be null";
    private static final String USER_UID_CAN_NEITHER_BE_NULL_NOR_EMPTY = "User uid of session cart %s can neither be null nor empty";
    private static final String CURRENCY_ISO_CAN_NEITHER_BE_NULL_NOR_EMPTY = "CurrencyIso of session cart %s can neither be null nor empty";
    private static final String CUSTOMER_CAN_NEITHER_BE_NULL_NOR_NOT_A_CUSTOMER = "Session cart: %s user can neither be null or not be a customer";
    private static final String CUSTOMER_CUSTOMERUID_CAN_NEITHER_BE_NULL_NOR_EMPTY = "Customer customerUid of session cart: %s can neither be null nor empty";

    protected final CartService cartService;
    protected final NuveiMerchantConfigurationService nuveiMerchantConfigurationService;
    protected final Converter<AddressModel, UserAddress> userAddressConverter;

    /**
     * Default constructor for {@link DefaultNuveiOpenOrderService}
     *
     * @param cartService                       injected
     * @param nuveiMerchantConfigurationService injected
     * @param userAddressConverter              injected
     */
    public DefaultNuveiOpenOrderService(final CartService cartService,
                                        final NuveiMerchantConfigurationService nuveiMerchantConfigurationService,
                                        final Converter<AddressModel, UserAddress> userAddressConverter) {
        this.cartService = cartService;
        this.nuveiMerchantConfigurationService = nuveiMerchantConfigurationService;
        this.userAddressConverter = userAddressConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String requestOpenOrder() throws SafechargeException {
        final CartModel sessionCart = cartService.getSessionCart();
        final CustomerModel customerModel = Optional.ofNullable(sessionCart.getUser())
                .filter(CustomerModel.class::isInstance)
                .map(CustomerModel.class::cast)
                .orElseThrow(() -> new IllegalArgumentException(String.format(CUSTOMER_CAN_NEITHER_BE_NULL_NOR_NOT_A_CUSTOMER, sessionCart.getCode())));
        final String customerId = customerModel.getCustomerID();
        final String email = customerModel.getUid();
        final String currencyIso = Optional.ofNullable(sessionCart.getCurrency())
                .map(C2LItemModel::getIsocode)
                .orElse(null);
        final Double totalPrice = sessionCart.getTotalPrice();
        final AddressModel paymentAddress = sessionCart.getPaymentAddress();
        final AddressModel deliveryAddress = sessionCart.getDeliveryAddress();

        checkArgument(isNotBlank(email), String.format(USER_UID_CAN_NEITHER_BE_NULL_NOR_EMPTY, sessionCart.getCode()));
        checkArgument(isNotBlank(customerId), String.format(CUSTOMER_CUSTOMERUID_CAN_NEITHER_BE_NULL_NOR_EMPTY, sessionCart.getCode()));
        checkArgument(isNotBlank(currencyIso), String.format(CURRENCY_ISO_CAN_NEITHER_BE_NULL_NOR_EMPTY, sessionCart.getCode()));
        checkArgument(Objects.nonNull(paymentAddress), String.format(PAYMENT_ADDRESS_CANT_BE_NULL, sessionCart.getCode()));
        checkArgument(Objects.nonNull(deliveryAddress), String.format(DELIVERY_ADDRESS_CANT_BE_NULL, sessionCart.getCode()));

        final UserAddress billingAddress = userAddressConverter.convert(paymentAddress);
        final UserAddress shippingAddress = userAddressConverter.convert(deliveryAddress);
        Optional.ofNullable(billingAddress).ifPresent(address -> address.setEmail(email));
        final NuveiMerchantConfigurationModel currentConfiguration =
                nuveiMerchantConfigurationService.getCurrentConfiguration();
        return executeRequest(currencyIso, customerId, String.valueOf(totalPrice),
                currentConfiguration, billingAddress, shippingAddress, sessionCart.getClientUniqueId());
    }

    /**
     * Executes the openOrder request
     *
     * @param currencyIso           The cart currencyIso
     * @param customerId            The session cart owner uid
     * @param totalPrice            The total price of the cart in String format
     * @param merchantConfiguration The merchant configuration of the current storefront
     * @param billingAddress        The session cart payment address
     * @param shippingAddress       The session cart shipping address
     * @param clientUniqueId        The client unique id
     * @return Session token if the request is success, otherwise throws a {@link SafechargeException}
     * @throws SafechargeException if there are request related problems
     */
    protected String executeRequest(final String currencyIso, final String customerId, final String totalPrice,
                                    final NuveiMerchantConfigurationModel merchantConfiguration,
                                    final UserAddress billingAddress, final UserAddress shippingAddress,
                                    final String clientUniqueId)
            throws SafechargeException {
        final NuveiSafechargeWrapper nuveiSafechargeWrapper = new NuveiSafechargeWrapper(merchantConfiguration);

        return nuveiSafechargeWrapper.openOrder(customerId, currencyIso, totalPrice, billingAddress, shippingAddress, clientUniqueId);
    }
}
