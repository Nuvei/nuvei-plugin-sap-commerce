package com.nuvei.notifications.jobs;

import com.nuvei.notifications.core.services.NuveiNotificationsProcessService;
import com.nuvei.notifications.model.NuveiProcessNotificationsCronjobModel;
import com.nuvei.services.enums.NuveiTransactionType;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Job to process the Nuvei Direct Merchant Notifications, it will process all the notifications that belongs to the given
 * types, if the types set is empty, will process all types
 */
public class NuveiProcessNotificationsJob extends AbstractJobPerformable<NuveiProcessNotificationsCronjobModel> {

    private final NuveiNotificationsProcessService nuveiNotificationsProcessService;

    /**
     * Default constructor for {@link NuveiProcessNotificationsJob}
     *
     * @param nuveiNotificationsProcessService injected
     */
    public NuveiProcessNotificationsJob(final NuveiNotificationsProcessService nuveiNotificationsProcessService) {
        this.nuveiNotificationsProcessService = nuveiNotificationsProcessService;
    }


    /**
     * Process the Nuvei Direct Merchant Notifications, it will process all the notifications that belongs to the given
     * types, if the types set is empty, will process all types
     *
     * @param nuveiProcessNotificationsCronjobModel model which contains the set of notification types to process
     * @return the performResult
     */
    @Override
    public PerformResult perform(final NuveiProcessNotificationsCronjobModel nuveiProcessNotificationsCronjobModel) {
        if (CollectionUtils.isEmpty(nuveiProcessNotificationsCronjobModel.getNuveiNotificationTypes())) {
            processNotifications(Arrays.asList(NuveiTransactionType.values()));
        } else {
            processNotifications(nuveiProcessNotificationsCronjobModel.getNuveiNotificationTypes());
        }
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    /**
     * Method that process the Nuvei Direct Merchant Notifications that belongs to the received transaction types
     *
     * @param nuveiTransactionTypeList the list of type to process the notifications
     */
    protected void processNotifications(final List<NuveiTransactionType> nuveiTransactionTypeList) {
        nuveiTransactionTypeList.forEach(nuveiNotificationsProcessService::processNuveiNotifications);
    }
}
