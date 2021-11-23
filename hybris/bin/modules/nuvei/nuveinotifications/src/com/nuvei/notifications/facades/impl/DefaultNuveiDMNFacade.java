package com.nuvei.notifications.facades.impl;

import com.nuvei.notifications.data.NuveiIncomingDMNData;
import com.nuvei.notifications.facades.NuveiDMNFacade;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;

/**
 * Default implementation of {@link NuveiDMNFacade}
 */
public class DefaultNuveiDMNFacade implements NuveiDMNFacade {

    protected final Converter<NuveiIncomingDMNData, NuveiDirectMerchantNotificationModel> nuveiIncomingDMNDataNuveiDirectMerchantNotificationModelConverter;
    protected final ModelService modelService;
    protected final KeyGenerator idGenerator;

    /**
     * Constructor of the facade for injecting the dependencies
     *  @param nuveiIncomingDMNDataNuveiDirectMerchantNotificationModelConverter
     * @param modelService
     * @param idGenerator
     */
    public DefaultNuveiDMNFacade(final Converter<NuveiIncomingDMNData, NuveiDirectMerchantNotificationModel> nuveiIncomingDMNDataNuveiDirectMerchantNotificationModelConverter,
                                 final ModelService modelService, final KeyGenerator idGenerator) {
        this.nuveiIncomingDMNDataNuveiDirectMerchantNotificationModelConverter = nuveiIncomingDMNDataNuveiDirectMerchantNotificationModelConverter;
        this.modelService = modelService;
        this.idGenerator = idGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NuveiDirectMerchantNotificationModel createAndSaveDMN(final NuveiIncomingDMNData nuveiIncomingDMNData) {
        final NuveiDirectMerchantNotificationModel nuveiDirectMerchantNotificationModel =
                nuveiIncomingDMNDataNuveiDirectMerchantNotificationModelConverter.convert(nuveiIncomingDMNData);

        nuveiDirectMerchantNotificationModel.setId(String.valueOf(idGenerator.generate()));

        modelService.save(nuveiDirectMerchantNotificationModel);

        return nuveiDirectMerchantNotificationModel;
    }
}
