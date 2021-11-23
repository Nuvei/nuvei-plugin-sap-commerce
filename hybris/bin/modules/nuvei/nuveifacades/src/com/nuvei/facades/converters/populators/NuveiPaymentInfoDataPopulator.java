package com.nuvei.facades.converters.populators;

import com.nuvei.services.model.NuveiPaymentInfoModel;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Optional;

/**
 * Converts the {@link NuveiPaymentInfoModel} to {@link CCPaymentInfoData}
 */
public class NuveiPaymentInfoDataPopulator implements Populator<NuveiPaymentInfoModel, CCPaymentInfoData> {

    protected final Converter<AddressModel, AddressData> addressConverter;

    /**
     * Default constructor for {@link NuveiPaymentInfoDataPopulator}
     *
     * @param addressConverter injected
     */
    public NuveiPaymentInfoDataPopulator(final Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }

    @Override
    public void populate(NuveiPaymentInfoModel source, CCPaymentInfoData target) throws ConversionException {
        target.setId(source.getPk().toString());
        target.setCardNumber(source.getMaskedCardNumber());
        target.setExpiryMonth(source.getExpMonth());
        target.setExpiryYear(source.getExpYear());

        Optional.ofNullable(source.getBillingAddress())
                .map(addressConverter::convert)
                .ifPresent(target::setBillingAddress);
    }
}
