package com.nuvei.facades.converters.populators;

import com.nuvei.services.model.NuveiPaymentInfoModel;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Optional;

/**
 * Populator that obtains the paymentInfo from {@link AbstractOrderModel} and sets it to the {@link AbstractOrderData}
 */
public class NuveiPaymentInfoOrderPopulator implements Populator<AbstractOrderModel, AbstractOrderData> {

    private final Converter<NuveiPaymentInfoModel, CCPaymentInfoData> nuveiPaymentInfoDataConverter;

    /**
     * Default constructor for {@link NuveiPaymentInfoOrderPopulator}
     *
     * @param nuveiPaymentInfoDataConverter injected
     */
    public NuveiPaymentInfoOrderPopulator(final Converter<NuveiPaymentInfoModel, CCPaymentInfoData> nuveiPaymentInfoDataConverter) {
        this.nuveiPaymentInfoDataConverter = nuveiPaymentInfoDataConverter;
    }

    @Override
    public void populate(final AbstractOrderModel source, final AbstractOrderData target) throws ConversionException {
        Optional.ofNullable(source)
                .map(AbstractOrderModel::getPaymentInfo)
                .filter(NuveiPaymentInfoModel.class::isInstance)
                .map(NuveiPaymentInfoModel.class::cast)
                .map(nuveiPaymentInfoDataConverter::convert)
                .ifPresent(target::setPaymentInfo);
    }
}

