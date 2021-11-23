package com.nuvei.notifications.core.services.impl;

import com.nuvei.notifications.core.services.NuveiDMNService;
import com.nuvei.notifications.core.services.NuveiNotificationsProcessService;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import com.nuvei.services.util.NuveiPaymentStatusResolver;
import com.nuvei.strategy.NuveiStrategyExecutor;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link NuveiNotificationsProcessService}
 */
public class DefaultNuveiNotificationsProcessService implements NuveiNotificationsProcessService {

    protected static final Logger LOG = LogManager.getLogger(DefaultNuveiNotificationsProcessService.class);

    protected final NuveiDMNService nuveiDMNService;
    protected final NuveiPaymentTransactionService nuveiPaymentTransactionService;
    protected final ModelService modelService;
    protected final NuveiStrategyExecutor<Pair<PaymentTransactionEntryModel, OrderModel>, Boolean> triggerEventOrderProcessStrategyExecutor;
    protected final NuveiStrategyExecutor<Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel>, AbstractOrderModel> orderTransactionTypeStrategyExecutor;

    /**
     * Default constructor for {@link DefaultNuveiNotificationsProcessService}
     *
     * @param nuveiDMNService                          injected
     * @param nuveiPaymentTransactionService           injected
     * @param modelService                             injected
     * @param triggerEventOrderProcessStrategyExecutor injected
     * @param orderTransactionTypeStrategyExecutor     injected
     */
    public DefaultNuveiNotificationsProcessService(final NuveiDMNService nuveiDMNService,
                                                   final NuveiPaymentTransactionService nuveiPaymentTransactionService,
                                                   final ModelService modelService,
                                                   final NuveiStrategyExecutor<Pair<PaymentTransactionEntryModel, OrderModel>, Boolean> triggerEventOrderProcessStrategyExecutor,
                                                   final NuveiStrategyExecutor<Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel>, AbstractOrderModel> orderTransactionTypeStrategyExecutor) {
        this.nuveiDMNService = nuveiDMNService;
        this.nuveiPaymentTransactionService = nuveiPaymentTransactionService;
        this.modelService = modelService;
        this.triggerEventOrderProcessStrategyExecutor = triggerEventOrderProcessStrategyExecutor;
        this.orderTransactionTypeStrategyExecutor = orderTransactionTypeStrategyExecutor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processNuveiNotifications(final NuveiTransactionType nuveiTransactionType) {
        final List<NuveiDirectMerchantNotificationModel> unprocessedDMNsByType = nuveiDMNService.getUnprocessedDMNsByType(nuveiTransactionType);
        final List<NuveiDirectMerchantNotificationModel> processedDMNs = new ArrayList<>();
        LOG.info("Processing {} notifications for type {}.", unprocessedDMNsByType.size(), nuveiTransactionType.getCode());
        for (final NuveiDirectMerchantNotificationModel unprocessedDMN : unprocessedDMNsByType) {
            final AbstractOrderModel abstractOrderModel = orderTransactionTypeStrategyExecutor.execute(Pair.of(nuveiTransactionType, unprocessedDMN));
            Optional.ofNullable(abstractOrderModel).ifPresent(orderModel -> {
                final Boolean processed = handleNotification(nuveiTransactionType, unprocessedDMN, orderModel);
                if (BooleanUtils.isTrue(processed)) {
                    processedDMNs.add(unprocessedDMN);
                    LOG.info("Notification {} of type {} for order {} processed", unprocessedDMN.getId(),
                            unprocessedDMN.getTransactionType(), orderModel.getCode());
                } else {
                    LOG.warn("Notification {} of type {} for order {} no processed", unprocessedDMN.getId(),
                            unprocessedDMN.getTransactionType(), orderModel.getCode());
                }
            });
        }

        LOG.info("Processed {} notifications for type {}.", processedDMNs.size(), nuveiTransactionType.getCode());
        updateDMNsProcessed(processedDMNs);
    }

    /**
     * Handle notification creating a payment entry and trigger event
     *
     * @param nuveiTransactionType the nuvei transaction type
     * @param notificationModel    the Nuvei Direct Merchant NotificationModel
     * @param abstractOrderModel   the abstract order related
     * @return flag true when notification has been triggered
     */
    protected Boolean handleNotification(final NuveiTransactionType nuveiTransactionType,
                                         final NuveiDirectMerchantNotificationModel notificationModel,
                                         final AbstractOrderModel abstractOrderModel) {
        final PaymentTransactionType paymentTransactionType = NuveiPaymentStatusResolver.paymentTransactionTypeResolver(nuveiTransactionType);

        if (abstractOrderModel instanceof OrderModel) {
            final OrderModel order = (OrderModel) abstractOrderModel;
            LOG.info("Processing {} notification for order {}", nuveiTransactionType.getCode(), order.getCode());
            final PaymentTransactionModel paymentTransactionModel = nuveiPaymentTransactionService.
                    findOrCreatePaymentTransaction(notificationModel, order, nuveiTransactionType);
            LOG.info("Payment transaction created for order {}", order.getCode());
            if (paymentTransactionModel != null) {
                final PaymentTransactionEntryModel paymentTransactionEntryModel = nuveiPaymentTransactionService.createPaymentTransactionEntry(paymentTransactionModel,
                        notificationModel, paymentTransactionType);
                return triggerWaitingOrderProcessEvent((OrderModel) abstractOrderModel, paymentTransactionEntryModel);
            } else {
                LOG.error("Payment transaction not exist for order code: [{}].", order.getCode());
            }
        } else {
            LOG.warn("Found cart model with payment id [{}]. Skipping event processing.", notificationModel.getPppTransactionId());
        }

        return false;
    }

    /**
     * Set processed flag to true
     *
     * @param notifications the list of the direct merchant notifications to update
     */
    protected void updateDMNsProcessed(final List<NuveiDirectMerchantNotificationModel> notifications) {
        if (!notifications.isEmpty()) {
            notifications.forEach(notification -> notification.setProcessed(Boolean.TRUE));
            modelService.saveAll(notifications);
        }
    }

    /**
     * Trigger the business process for the wait conditions.
     *
     * @param orderModel             the order model related
     * @param paymentTransactionEntryModel the payment transaction entry
     * @return the flag is businessProcess has been triggered
     */
    protected Boolean triggerWaitingOrderProcessEvent(final OrderModel orderModel, final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return triggerEventOrderProcessStrategyExecutor.execute(Pair.of(paymentTransactionEntryModel, orderModel));
    }
}
