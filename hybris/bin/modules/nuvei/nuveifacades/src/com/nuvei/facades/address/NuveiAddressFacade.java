package com.nuvei.facades.address;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;

/**
 * Handles the address operations for nuvei
 */
public interface NuveiAddressFacade {

    /**
     * Set billing address to the session cart (payment address)
     *
     * @param addressData the address data
     */
    void setCartBillingAddress(final AddressData addressData);

    /**
     * Create billing address and set into the session cart (payment address)
     *
     * @param addressData the address data
     */
    void createAndSetCartBillingAddress(final AddressData addressData);

    /**
     * Gets the delivery address for the given id
     *
     * @param addressId the address identifier
     * @param cartModel the cart
     * @return {@link AddressModel} the billing address for the id
     */
    AddressModel getDeliveryAddressModelForCode(final String addressId, final CartModel cartModel);

    /**
     * Create AddressModel from AddressData and set CartModel as parent
     *
     * @param addressData the address data
     * @param cartModel   the cart model
     * @return {@link AddressModel} the billing address for the data
     */
    AddressModel createBillingAddressModel(final AddressData addressData, final CartModel cartModel);
}
