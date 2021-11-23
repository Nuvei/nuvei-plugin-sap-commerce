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
 * Triggers the events for the SALE {@link PaymentTransactionType} transition
 */
public class NuveiTriggerEventOrderProcessSaleStrategy extends NuveiAbstractTriggerEventOrderProcessStrategy {

    /**
     * {@inheritDoc}
     */
    protected NuveiTriggerEventOrderProcessSaleStrategy(final BusinessProcessService businessProcessService,
                                                        final ProcessDefinitionDao processDefinitionDao) {
        super(LogManager.getLogger(NuveiTriggerEventOrderProcessSaleStrategy.class), businessProcessService, processDefinitionDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean execute(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        log.info("Executing strategy  {}", NuveiTriggerEventOrderProcessSaleStrategy.class.getName());
        return super.triggerEvent(source.getRight(), PaymentTransactionType.AUTHORIZATION.getCode(), StringUtils.EMPTY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        return PaymentTransactionType.SALE.equals(source.getLeft().getType());
    }
}
