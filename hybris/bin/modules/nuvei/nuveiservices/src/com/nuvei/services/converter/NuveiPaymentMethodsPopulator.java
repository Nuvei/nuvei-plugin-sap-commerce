package com.nuvei.services.converter;

import com.nuvei.services.model.NuveiPaymentMethodModel;
import com.safecharge.model.PaymentMethod;
import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.C2LItemModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Populator that fills data from an {@link PaymentMethod } to a {@link NuveiPaymentMethodModel}
 */
public class NuveiPaymentMethodsPopulator implements Populator<PaymentMethod, NuveiPaymentMethodModel> {

    protected final CommerceCommonI18NService commerceCommonI18NService;

    /**
     * Default constructor for {@link NuveiPaymentMethodsPopulator}
     *
     * @param commerceCommonI18NService injected
     */
    public NuveiPaymentMethodsPopulator(final CommerceCommonI18NService commerceCommonI18NService) {
        this.commerceCommonI18NService = commerceCommonI18NService;
    }

    /**
     * Populates the name and identifier from {@link PaymentMethod} into a {@link NuveiPaymentMethodModel}
     *
     * @param source an {@link PaymentMethod} that contains the information
     * @param target an {@link NuveiPaymentMethodModel} that receives the information
     */
    @Override
    public void populate(final PaymentMethod source, final NuveiPaymentMethodModel target) throws ConversionException {
        target.setId(source.getPaymentMethod());

        final List<String> languages = Stream.ofNullable(commerceCommonI18NService.getAllLanguages())
                .flatMap((Collection::stream))
                .map(C2LItemModel::getIsocode)
                .collect(Collectors.toList());

        Stream.of(source).map(PaymentMethod::getPaymentMethodDisplayName)
                .flatMap((Collection::stream))
                .filter(localizationMessage -> languages.contains(localizationMessage.getLanguage()))
                .forEach(localizationMessage -> target.setDisplayName(localizationMessage.getMessage(),
                        new Locale(localizationMessage.getLanguage()))
                );
    }
}
