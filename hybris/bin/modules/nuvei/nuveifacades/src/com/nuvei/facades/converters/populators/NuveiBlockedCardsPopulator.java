package com.nuvei.facades.converters.populators;

import com.nuvei.facades.beans.NuveiBlockedCardsData;
import com.nuvei.services.enums.NuveiBrand;
import com.nuvei.services.enums.NuveiCardProduct;
import com.nuvei.services.enums.NuveiCardType;
import com.nuvei.services.model.NuveiBlockedCardsModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the {@link NuveiBlockedCardsModel} into {@link NuveiBlockedCardsData}
 */
public class NuveiBlockedCardsPopulator implements Populator<NuveiBlockedCardsModel, NuveiBlockedCardsData> {

    @Override
    public void populate(final NuveiBlockedCardsModel source,
                         final NuveiBlockedCardsData target) throws ConversionException {

        validateParameterNotNull(source, "NuveiBlockedCardsModel cannot be null.");
        validateParameterNotNull(target, "NuveiBlockedCardsData cannot be null.");

        target.setBrands(Stream.ofNullable(source.getBrand())
                .flatMap((Collection::stream))
                .map(NuveiBrand::getCode)
                .collect(Collectors.toList()));
        target.setCardProducts(Stream.ofNullable(source.getCardProduct())
                .flatMap((Collection::stream))
                .map(NuveiCardProduct::getCode)
                .collect(Collectors.toList()));
        target.setCardTypes(Stream.ofNullable(source.getCardType())
                .flatMap((Collection::stream))
                .map(NuveiCardType::getCode)
                .collect(Collectors.toList()));
        target.setCountries(Stream.ofNullable(source.getCountry())
                .flatMap((Collection::stream))
                .map(CountryModel::getIsocode)
                .collect(Collectors.toList()));
    }
}
