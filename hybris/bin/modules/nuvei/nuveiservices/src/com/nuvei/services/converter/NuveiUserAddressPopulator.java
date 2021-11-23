package com.nuvei.services.converter;

import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.safecharge.model.UserAddress;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.C2LItemModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Populates the {@link AddressModel} into {@link UserAddress}
 */
public class NuveiUserAddressPopulator implements Populator<AddressModel, UserAddress> {
    protected final NuveiMerchantConfigurationService nuveiMerchantConfigurationService;

    /**
     * Default constructor
     *
     * @param nuveiMerchantConfigurationService
     */
    public NuveiUserAddressPopulator(final NuveiMerchantConfigurationService nuveiMerchantConfigurationService) {
        this.nuveiMerchantConfigurationService = nuveiMerchantConfigurationService;
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public void populate(final AddressModel source, final UserAddress target) throws ConversionException {
        target.setZip(source.getPostalcode());
        target.setCell(source.getCellphone());
        target.setCity(source.getTown());
        prepopulateCardholderName(source, target);
        target.setPhone(source.getPhone1());
        target.setEmail(source.getEmail());
        Optional.ofNullable(source.getCountry())
                .map(C2LItemModel::getIsocode)
                .ifPresent(target::setCountry);
        Optional.ofNullable(source.getRegion())
                .map(RegionModel::getIsocodeShort)
                .ifPresent(target::setState);
        target.setAddress(Stream.of(source.getLine1(), source.getLine2())
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(StringUtils.SPACE)));
    }

    /**
     * Pre-populates the cardholder name on the call to nuvei in case the current configuration on {@link NuveiMerchantConfigurationModel} is true
     *
     * @param source the {@link AddressModel}
     * @param target the {@link UserAddress}
     */
    protected void prepopulateCardholderName(final AddressModel source, final UserAddress target) {
        final NuveiMerchantConfigurationModel currentConfiguration = nuveiMerchantConfigurationService.getCurrentConfiguration();
        if (Boolean.TRUE.equals(currentConfiguration.getPrePopulateFullName())) {
            target.setFirstName(source.getFirstname());
            target.setLastName(source.getLastname());
        }
    }
}
