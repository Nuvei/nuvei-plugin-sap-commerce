package com.nuvei.services.payments;

import com.nuvei.services.model.NuveiPaymentInfoModel;
import de.hybris.platform.core.model.order.CartModel;

/**
 * Service to handle payment info logic
 */
public interface NuveiPaymentInfoService {

    /**
     * Removes the payment info from the DB and update the cart
     *
     * @param cartModel the current cart
     */
    void removePaymentInfo(CartModel cartModel);

    /**
     * Creates the payment info for given current cart
     *
     * @param paymentInfoModel the payment info to save in the cart
     * @param cartModel        the current cart model
     */
    void createPaymentInfo(NuveiPaymentInfoModel paymentInfoModel, CartModel cartModel);
}
