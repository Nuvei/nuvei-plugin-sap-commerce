package com.nuvei.notifications.strategies.paymenttypes;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.notifications.strategies.NuveiAbstractTriggerEventOrderProcessStrategy;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.processengine.BusinessProcessService;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

/**
 * Triggers the events for the rest of the {@link PaymentTransactionType}
 */
public class NuveiAvoidTriggerEventOrderProcessStrategy extends NuveiAbstractTriggerEventOrderProcessStrategy {

    /**
     * {@inheritDoc}
     */
    protected NuveiAvoidTriggerEventOrderProcessStrategy(final BusinessProcessService businessProcessService,
                                                         final ProcessDefinitionDao processDefinitionDao) {
        super(LogManager.getLogger(NuveiAvoidTriggerEventOrderProcessStrategy.class), businessProcessService, processDefinitionDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean execute(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        log.info("Executing strategy  {}", NuveiAvoidTriggerEventOrderProcessStrategy.class.getName());
        log.warn("Avoiding to process order with code [{}] due to the order has the " +
                "status [{}] and the DMN is of type [{}].", source.getRight().getCode(), source.getRight().getStatus().getCode(),
                source.getLeft().getType().getCode());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        return isAlreadyVoidedOrder(source)
                || isPendingTransaction(source);
    }

    protected boolean isPendingTransaction(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        return NuveiTransactionStatus.PENDING.getCode().equals(source.getLeft().getTransactionStatus());
    }

    protected boolean isAlreadyVoidedOrder(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        return PaymentTransactionType.VOID.equals(source.getLeft().getType()) && OrderStatus.VOIDED.equals(source.getRight().getStatus());
    }
}
