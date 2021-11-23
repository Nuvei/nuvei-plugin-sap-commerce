package com.nuvei.notifications.strategies.paymenttypes;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import com.nuvei.notifications.strategies.NuveiAbstractTriggerEventOrderProcessStrategy;
import com.nuvei.services.service.NuveiOrderService;
import de.hybris.platform.core.enums.OrderStatus;
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
public class NuveiTriggerEventOrderProcessVoidStrategy extends NuveiAbstractTriggerEventOrderProcessStrategy {

    protected final NuveiOrderService nuveiOrderService;

    /**
     * {@inheritDoc}
     */
    protected NuveiTriggerEventOrderProcessVoidStrategy(final BusinessProcessService businessProcessService,
                                                        final ProcessDefinitionDao processDefinitionDao,
                                                        final NuveiOrderService nuveiOrderService) {
        super(LogManager.getLogger(NuveiTriggerEventOrderProcessVoidStrategy.class), businessProcessService, processDefinitionDao);
        this.nuveiOrderService = nuveiOrderService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean execute(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        log.info("Executing strategy  {}", NuveiTriggerEventOrderProcessVoidStrategy.class.getName());

        final Boolean result = super.triggerEvent(source.getRight(), source.getLeft().getType().getCode(), StringUtils.EMPTY);

        if (Boolean.FALSE.equals(result)) {
            return nuveiOrderService.requestCancelOrder(source.getRight());
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        return PaymentTransactionType.VOID.equals(source.getLeft().getType()) && !OrderStatus.VOIDED.equals(source.getRight().getStatus());
    }
}
