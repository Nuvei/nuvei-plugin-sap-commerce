package com.nuvei.facades.payment.impl;

import com.nuvei.facades.beans.NuveiSDKResponseData;
import com.nuvei.facades.payment.NuveiPaymentInfoFacade;
import com.nuvei.services.model.NuveiPaymentInfoModel;
import com.nuvei.services.payments.NuveiPaymentInfoService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Optional;

/**
 * {@inheritDoc}
 */
public class DefaultNuveiPaymentInfoFacade implements NuveiPaymentInfoFacade {

    protected final CartService cartService;
    protected final NuveiPaymentInfoService nuveiPaymentInfoService;
    protected final Converter<NuveiSDKResponseData, NuveiPaymentInfoModel> nuveiPaymentInfoModelConverter;

    /**
     * Default constructor for {@link DefaultNuveiPaymentInfoFacade}
     *
     * @param cartService                    injected
     * @param nuveiPaymentInfoService        injected
     * @param nuveiPaymentInfoModelConverter injected
     */
    public DefaultNuveiPaymentInfoFacade(final CartService cartService,
                                         final NuveiPaymentInfoService nuveiPaymentInfoService,
                                         final Converter<NuveiSDKResponseData, NuveiPaymentInfoModel> nuveiPaymentInfoModelConverter) {
        this.cartService = cartService;
        this.nuveiPaymentInfoService = nuveiPaymentInfoService;
        this.nuveiPaymentInfoModelConverter = nuveiPaymentInfoModelConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPaymentInfoToCart(final NuveiSDKResponseData checkoutSDKResponse) {
        if (cartService.hasSessionCart()) {
            final CartModel sessionCart = cartService.getSessionCart();
            Optional.ofNullable(sessionCart.getPaymentInfo())
                    .ifPresent(paymentInfoModel -> nuveiPaymentInfoService.removePaymentInfo(sessionCart));
            final NuveiPaymentInfoModel paymentInfoModel = nuveiPaymentInfoModelConverter.convert(checkoutSDKResponse);
            nuveiPaymentInfoService.createPaymentInfo(paymentInfoModel, sessionCart);
        }
    }
}
