package com.nuvei.facades.converters.populators;

import com.nuvei.facades.beans.NuveiSDKResponseData;
import com.nuvei.services.model.NuveiPaymentInfoModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the {@link NuveiSDKResponseData} into {@link NuveiPaymentInfoModel}
 */
public class NuveiPaymentInfoReversePopulator implements Populator<NuveiSDKResponseData, NuveiPaymentInfoModel> {

    private static final String TARGET_CAN_NOT_BE_NULL_ERROR_MSG = "NuveiPaymentInfoModel can not be null";
    private static final String SOURCE_CAN_NOT_BE_NULL_ERROR_MSG = "NuveiSDKResponseData can not be null";

    @Override
    public void populate(final NuveiSDKResponseData source,
                         final NuveiPaymentInfoModel target) throws ConversionException {
        validateParameterNotNull(source, SOURCE_CAN_NOT_BE_NULL_ERROR_MSG);
        validateParameterNotNull(target, TARGET_CAN_NOT_BE_NULL_ERROR_MSG);

        Optional.ofNullable(source.getTransactionId()).ifPresent(target::setTransactionId);
        target.setMaskedCardNumber(source.getCcCardNumber());
        target.setExpMonth(source.getCcExpMonth());
        target.setExpYear(source.getCcExpYear());
    }
}
