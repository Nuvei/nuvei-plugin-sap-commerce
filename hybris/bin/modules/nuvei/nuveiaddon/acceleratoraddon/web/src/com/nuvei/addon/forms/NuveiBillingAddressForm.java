package com.nuvei.addon.forms;

import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;

public class NuveiBillingAddressForm extends AddressForm {
    private Boolean useDeliveryAddress;

    public Boolean getUseDeliveryAddress() {
        return useDeliveryAddress;
    }

    public void setUseDeliveryAddress(final Boolean useDeliveryAddress) {
        this.useDeliveryAddress = useDeliveryAddress;
    }
}
