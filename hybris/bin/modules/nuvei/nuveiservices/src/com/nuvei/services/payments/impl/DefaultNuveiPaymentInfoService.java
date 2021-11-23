package com.nuvei.services.payments.impl;

import com.nuvei.services.model.NuveiPaymentInfoModel;
import com.nuvei.services.payments.NuveiPaymentInfoService;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.AddressService;

import java.util.UUID;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * {@inheritDoc}
 */
public class DefaultNuveiPaymentInfoService implements NuveiPaymentInfoService {

    protected static final String CART_CANNOT_BE_NULL_ERROR_MSG = "Cart cannot be null.";
    protected static final String PAYMENT_INFO_CANNOT_BE_NULL_ERROR_MSG = "Payment info model cannot be null";
    protected static final String PAYMENT_ADDRESS_CANNOT_BE_NULL_ERROR_MSG = "Payment Address cannot be null.";

    protected final AddressService addressService;
    protected final ModelService modelService;

    /**
     * Default constructor for {@link DefaultNuveiPaymentInfoService}
     *
     * @param addressService injected
     * @param modelService   injected
     */
    public DefaultNuveiPaymentInfoService(final AddressService addressService,
                                          final ModelService modelService) {
        this.addressService = addressService;
        this.modelService = modelService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePaymentInfo(final CartModel cartModel) {
        validateParameterNotNull(cartModel, CART_CANNOT_BE_NULL_ERROR_MSG);
        validateParameterNotNull(cartModel.getPaymentInfo(), PAYMENT_INFO_CANNOT_BE_NULL_ERROR_MSG);

        modelService.remove(cartModel.getPaymentInfo());
        cartModel.setPaymentInfo(null);
        modelService.save(cartModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPaymentInfo(final NuveiPaymentInfoModel paymentInfoModel, final CartModel cartModel) {
        validateParameterNotNull(paymentInfoModel, PAYMENT_INFO_CANNOT_BE_NULL_ERROR_MSG);
        validateParameterNotNull(cartModel, CART_CANNOT_BE_NULL_ERROR_MSG);

        final UserModel user = cartModel.getUser();
        cloneAndSetBillingAddressFromCart(cartModel, paymentInfoModel);
        paymentInfoModel.setCode(generatePaymentInfoCode(cartModel));
        paymentInfoModel.setUser(user);
        paymentInfoModel.setSaved(false);
        modelService.save(paymentInfoModel);
        cartModel.setPaymentInfo(paymentInfoModel);
        modelService.save(cartModel);
    }

    /**
     * Extra address created for the payment info to don't corrupt the cart once deleting payment info
     *
     * @param cartModel        the cart with source address
     * @param paymentInfoModel payment info to update
     * @return AddressModel the cloned address model
     */
    protected AddressModel cloneAndSetBillingAddressFromCart(final CartModel cartModel, final PaymentInfoModel paymentInfoModel) {
        validateParameterNotNull(cartModel, CART_CANNOT_BE_NULL_ERROR_MSG);
        validateParameterNotNull(cartModel.getPaymentAddress(), PAYMENT_ADDRESS_CANNOT_BE_NULL_ERROR_MSG);

        final AddressModel paymentAddress = cartModel.getPaymentAddress();
        final AddressModel clonedAddress = addressService.cloneAddressForOwner(paymentAddress, paymentInfoModel);
        clonedAddress.setBillingAddress(true);
        clonedAddress.setShippingAddress(false);
        clonedAddress.setOwner(paymentInfoModel);
        paymentInfoModel.setBillingAddress(clonedAddress);
        return clonedAddress;
    }

    /**
     * Generates the payment info code based on the cart model and a random UUID
     *
     * @param cartModel The cart model of the payment info
     * @return The generated payment info code
     */
    protected String generatePaymentInfoCode(final AbstractOrderModel cartModel) {
        return cartModel.getCode() + "_" + UUID.randomUUID();
    }
}
