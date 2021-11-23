package com.nuvei.notifications.strategies;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import com.nuvei.strategy.NuveiStrategy;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * This abstract class triggers order process events based in the {@link PaymentTransactionType}
 */
public abstract class NuveiAbstractTriggerEventOrderProcessStrategy implements NuveiStrategy<Pair<PaymentTransactionEntryModel, OrderModel>, Boolean> {

    private static final String SEPARATOR = "_";

    protected final Logger log;
    protected final BusinessProcessService businessProcessService;
    protected final ProcessDefinitionDao processDefinitionDao;

    /**
     * Default constructor
     *
     * @param log
     * @param businessProcessService
     * @param processDefinitionDao
     */
    protected NuveiAbstractTriggerEventOrderProcessStrategy(final Logger log,
                                                            final BusinessProcessService businessProcessService,
                                                            final ProcessDefinitionDao processDefinitionDao) {
        this.log = log;
        this.businessProcessService = businessProcessService;
        this.processDefinitionDao = processDefinitionDao;
    }

    /**
     * Triggers the events based in the {@link PaymentTransactionType}
     *
     * @param order
     * @param paymentTransactionType
     * @param transitionChoice
     * @return boolean if trigger was successfully
     */
    protected Boolean triggerEvent(final OrderModel order, final String paymentTransactionType, final String transitionChoice) {
        return triggerEvent(order, paymentTransactionType, paymentTransactionType, transitionChoice);
    }

    /**
     * Triggers the events based in the actionName and the {@link PaymentTransactionType}
     *
     * @param order
     * @param waitActionName
     * @param paymentTransactionType
     * @param transitionChoice
     * @return boolean if trigger was successfully
     */
    protected Boolean triggerEvent(final OrderModel order, final String waitActionName, final String paymentTransactionType, final String transitionChoice) {

        final List<BusinessProcessModel> businessProcessModels = processDefinitionDao.findWaitingOrderProcesses(order.getCode(), waitActionName);

        if (businessProcessModels.size() == 1) {
            final BusinessProcessEvent.Builder builder = BusinessProcessEvent.builder(businessProcessModels.get(0).getCode() + SEPARATOR + paymentTransactionType);
            if (!StringUtils.isEmpty(transitionChoice)) {
                builder.withChoice(transitionChoice);
            }
            final BusinessProcessEvent event = builder.build();
            log.info("Triggering event {}", event);

            return businessProcessService.triggerEvent(event);
        }

        return Boolean.FALSE;
    }
}
