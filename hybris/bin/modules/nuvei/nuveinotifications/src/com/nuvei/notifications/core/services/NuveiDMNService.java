package com.nuvei.notifications.core.services;

import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;

import java.util.List;

/**
 * Provides direct merchant nofitication funcionalities
 */
public interface NuveiDMNService {

    /**
     * Finds unprocessed direct merchant notifications by nuvei transaction type
     *
     * @param nuveiTransactionType
     * @return the list of {@link NuveiDirectMerchantNotificationModel}
     */
    List<NuveiDirectMerchantNotificationModel> getUnprocessedDMNsByType(NuveiTransactionType nuveiTransactionType);

    /**
     * Find the Nuvei Direct Merchant Notifications by pppTransactionId
     *
     * @return the Nuvei Direct Merchant Notifications
     */
    NuveiDirectMerchantNotificationModel findDMNByTypePPPTransactionId(String pppTransactionId);

    /**
     * Find all the Nuvei Direct Merchant Notifications that has been processed and las modified time is before than
     * the date minus an amount of days configured via property 'nuvei.clean.notifications.job.days'
     *
     * @return a list with the Nuvei Direct Merchant Notifications
     */
    List<NuveiDirectMerchantNotificationModel> findDMNToDelete();
}
