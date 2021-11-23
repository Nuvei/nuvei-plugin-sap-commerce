package com.nuvei.services.jobs;

import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.model.NuveiMerchantSyncCronjobModel;
import com.nuvei.services.payments.NuveiFilteringPaymentMethodsService;
import com.safecharge.exception.SafechargeException;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Synchronizes the Nuvei merchant configuration given by the {@link NuveiMerchantSyncCronjobModel} if the attribute is
 * empty o null will retrieve all stored in the database and process them
 */
public class NuveiPaymentMethodSyncJob extends AbstractJobPerformable<NuveiMerchantSyncCronjobModel> {

    /**
     * Logger for this class
     */
    private static final Logger logger = LogManager.getLogger(NuveiPaymentMethodSyncJob.class.getName());


    private static final String NOT_ABLE_TO_RETRIEVE_PAYMENT_METHODS = "Not able to retrieve payment methods: ";

    private final NuveiFilteringPaymentMethodsService nuveiFilteringPaymentMethodsService;
    private final NuveiMerchantConfigurationService nuveiMerchantConfigurationService;

    public NuveiPaymentMethodSyncJob(final NuveiFilteringPaymentMethodsService nuveiFilteringPaymentMethodsService,
                                     final NuveiMerchantConfigurationService nuveiMerchantConfigurationService) {
        this.nuveiFilteringPaymentMethodsService = nuveiFilteringPaymentMethodsService;
        this.nuveiMerchantConfigurationService = nuveiMerchantConfigurationService;
    }

    @Override
    public PerformResult perform(final NuveiMerchantSyncCronjobModel nuveiMerchantSyncCronjobModel) {
        if (logger.isDebugEnabled()) {
            logger.debug("perform(NuveiMerchantSyncCronjobModel) - start");
        }

        if (CollectionUtils.isNotEmpty(nuveiMerchantSyncCronjobModel.getNuveiMerchantConfigurations())){
            processMerchants(nuveiMerchantSyncCronjobModel.getNuveiMerchantConfigurations());
        } else {
            processMerchants(nuveiMerchantConfigurationService.getAllMerchantConfigurations());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("perform(NuveiMerchantSyncCronjobModel) - end");
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    protected void processMerchants(final List<NuveiMerchantConfigurationModel> nuveiMerchantConfiguration) {
        for (final NuveiMerchantConfigurationModel merchant : nuveiMerchantConfiguration) {
            try {
                nuveiFilteringPaymentMethodsService.synchFilteringPaymentMethodsForMerchant(merchant);
            } catch (final SafechargeException e) {
                logger.error(NOT_ABLE_TO_RETRIEVE_PAYMENT_METHODS, e);
            }
        }
    }
}
