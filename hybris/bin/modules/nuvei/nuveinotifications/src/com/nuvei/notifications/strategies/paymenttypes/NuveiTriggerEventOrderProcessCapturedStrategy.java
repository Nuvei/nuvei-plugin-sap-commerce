package com.nuvei.notifications.strategies.paymenttypes;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import com.nuvei.notifications.strategies.NuveiAbstractTriggerEventOrderProcessStrategy;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.processengine.BusinessProcessService;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

/**
 * Triggers the events for the CAPTURE {@link PaymentTransactionType} transition
 */
public class NuveiTriggerEventOrderProcessCapturedStrategy extends NuveiAbstractTriggerEventOrderProcessStrategy {
    private static final String DMN = "DMN";
    private static final String MANUAL_ORDER_CHECK_CSA = "ManualOrderCheckCSA";

    /**
     * {@inheritDoc}
     */
    protected NuveiTriggerEventOrderProcessCapturedStrategy(final BusinessProcessService businessProcessService,
                                                            final ProcessDefinitionDao processDefinitionDao) {
        super(LogManager.getLogger(NuveiTriggerEventOrderProcessCapturedStrategy.class), businessProcessService, processDefinitionDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean execute(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        log.info("Executing strategy  {}", NuveiTriggerEventOrderProcessCapturedStrategy.class.getName());
        final String paymentTransactionType = source.getLeft().getType().getCode();
        final Boolean result = super.triggerEvent(source.getRight(), paymentTransactionType, DMN);
        if (result.equals(Boolean.FALSE)){
            return super.triggerEvent(source.getRight(), MANUAL_ORDER_CHECK_CSA, paymentTransactionType, DMN);
        }
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        return PaymentTransactionType.CAPTURE.equals(source.getLeft().getType());
    }
}
