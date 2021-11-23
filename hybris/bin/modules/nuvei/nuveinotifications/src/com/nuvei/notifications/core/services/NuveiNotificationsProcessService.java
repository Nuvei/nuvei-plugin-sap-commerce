package com.nuvei.notifications.core.services;

import com.nuvei.services.enums.NuveiTransactionType;

/**
 * Service to handle payment processing
 */
public interface NuveiNotificationsProcessService {

    /**
     * Process notifications by type
     *
     * @param nuveiTransactionType the type of notification to process
     */
    void processNuveiNotifications(NuveiTransactionType nuveiTransactionType);
}
