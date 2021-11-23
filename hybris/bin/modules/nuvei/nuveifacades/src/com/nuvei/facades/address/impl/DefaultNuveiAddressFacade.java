package com.nuvei.facades.address.impl;

import com.nuvei.facades.address.NuveiAddressFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.springframework.util.Assert;

import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link NuveiAddressFacade}
 */
public class DefaultNuveiAddressFacade implements NuveiAddressFacade {

    protected static final String CART_MODEL_NULL = "CartModel cannot be null";

    protected final CartService cartService;
    protected final ModelService modelService;
    protected final DeliveryService deliveryService;
    protected final Converter<AddressData, AddressModel> addressReverseConverter;

    public DefaultNuveiAddressFacade(final CartService cartService,
                                     final ModelService modelService,
                                     final DeliveryService deliveryService,
                                     final Converter<AddressData, AddressModel> addressReverseConverter) {
        this.cartService = cartService;
        this.modelService = modelService;
        this.deliveryService = deliveryService;
        this.addressReverseConverter = addressReverseConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCartBillingAddress(final AddressData addressData) {
        validateParameterNotNull(addressData, "Address Data cannot be null");

        if (cartService.hasSessionCart()) {
            final CartModel sessionCart = cartService.getSessionCart();

            final AddressModel addressModel = getDeliveryAddressModelForCode(addressData.getId(), sessionCart);
            Optional.ofNullable(addressModel).ifPresent(address -> {
                addressModel.setBillingAddress(Boolean.TRUE);
                sessionCart.setPaymentAddress(addressModel);
                modelService.save(sessionCart);
            });
        } else {
            throw new IllegalArgumentException(CART_MODEL_NULL);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAndSetCartBillingAddress(final AddressData addressData) {
        validateParameterNotNull(addressData, "Address data cannot be null");

        if (cartService.hasSessionCart()) {
            final CartModel sessionCart = cartService.getSessionCart();

            final AddressModel billingAddressModel = createBillingAddressModel(addressData, sessionCart);
            billingAddressModel.setBillingAddress(Boolean.TRUE);
            billingAddressModel.setVisibleInAddressBook(Boolean.FALSE);
            sessionCart.setPaymentAddress(billingAddressModel);

            modelService.save(sessionCart);
        } else {
            throw new IllegalArgumentException(CART_MODEL_NULL);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddressModel getDeliveryAddressModelForCode(final String addressId, final CartModel cartModel) {
        Assert.notNull(addressId, "Parameter addressId cannot be null.");

        if (cartModel != null) {
            return deliveryService.getSupportedDeliveryAddressesForOrder(cartModel, false).stream()
                    .filter(addressModel -> addressId.equals(addressModel.getPk().toString()))
                    .findAny()
                    .orElse(null);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddressModel createBillingAddressModel(final AddressData addressData, final CartModel cartModel) {
        Assert.notNull(addressData, "Parameter addressData cannot be null.");
        Assert.notNull(cartModel, "Parameter cartModel cannot be null.");

        final AddressModel addressModel = modelService.create(AddressModel.class);
        addressModel.setOwner(cartModel);
        addressReverseConverter.convert(addressData, addressModel);

        return addressModel;
    }
}
