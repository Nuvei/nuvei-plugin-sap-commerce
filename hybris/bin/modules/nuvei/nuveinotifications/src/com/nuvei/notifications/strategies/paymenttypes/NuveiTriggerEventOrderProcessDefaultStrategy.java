package com.nuvei.notifications.strategies.paymenttypes;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import com.nuvei.notifications.strategies.NuveiAbstractTriggerEventOrderProcessStrategy;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.processengine.BusinessProcessService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

/**
 * Triggers the events for the rest of the {@link PaymentTransactionType}
 */
public class NuveiTriggerEventOrderProcessDefaultStrategy extends NuveiAbstractTriggerEventOrderProcessStrategy {

    /**
     * {@inheritDoc}
     */
    protected NuveiTriggerEventOrderProcessDefaultStrategy(final BusinessProcessService businessProcessService,
                                                           final ProcessDefinitionDao processDefinitionDao) {
        super(LogManager.getLogger(NuveiTriggerEventOrderProcessDefaultStrategy.class), businessProcessService, processDefinitionDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean execute(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        log.info("Executing strategy  {}", NuveiTriggerEventOrderProcessDefaultStrategy.class.getName());
        return super.triggerEvent(source.getRight(), source.getLeft().getType().getCode(), StringUtils.EMPTY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        return !PaymentTransactionType.SALE.equals(source.getLeft().getType()) &&
                !PaymentTransactionType.CAPTURE.equals(source.getLeft().getType()) &&
                !PaymentTransactionType.VOID.equals(source.getLeft().getType()) &&
                !PaymentTransactionType.REFUND_FOLLOW_ON.equals(source.getLeft().getType());
    }
}
