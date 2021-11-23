package com.nuvei.services.order.impl;

import de.hybris.platform.commerceservices.order.impl.CommerceCartFactory;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.model.ModelService;

/**
 * This class overrides {@link CommerceCartFactory} to ensure the clientUniqueId of a {@link CartModel} is set
 */
public class NuveiCartFactory extends CommerceCartFactory {

    private final ModelService modelService;

    public NuveiCartFactory(ModelService modelService) {
        this.modelService = modelService;
    }

    /**
     * Ensures the creation of the cart generates a clientUniqueId for Nuvei
     *
     * @return the {@link CartModel} with clientUniqueId set
     */
    @Override
    public CartModel createCart() {
        CartModel cartModel = superCreateCart();
        cartModel.setClientUniqueId(generateClientUniqueId(cartModel));
        modelService.save(cartModel);

        return cartModel;
    }

    /**
     * Method that actually generates the clientUniqueId based on the code of the {@link CartModel}
     *
     * @param cartModel the cartModel generated
     * @return the clientUnique id based on the code of the {@link CartModel}
     */
    protected String generateClientUniqueId(CartModel cartModel) {
        return cartModel.getCode();
    }

    /**
     * This method is only used to help the unit testing
     *
     * @return the {@link CartModel} generated in the parent class
     */
    protected CartModel superCreateCart() {
        return super.createCart();
    }
}
