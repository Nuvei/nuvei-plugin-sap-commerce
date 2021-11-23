package com.nuvei.order.actions;

import com.nuvei.strategy.NuveiStrategyExecutor;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Set;

@SuppressWarnings("common-java:DuplicatedBlocks")
public class NuveiCheckPaymentStatusAction extends AbstractAction<OrderProcessModel> {
    private static final Logger LOG = LogManager.getLogger(NuveiCheckPaymentStatusAction.class);

    private final NuveiStrategyExecutor<OrderModel, String> strategyExecutor;
    protected static final String OK = "OK";
    protected static final String NOK = "NOK";
    protected static final String WAIT = "WAIT";

    /**
     * Default constructor for {@link NuveiCheckPaymentStatusAction}
     *
     * @param strategyExecutor injected
     */
    public NuveiCheckPaymentStatusAction(final NuveiStrategyExecutor<OrderModel, String> strategyExecutor) {
        this.strategyExecutor = strategyExecutor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final OrderProcessModel businessProcessModel) {
        final OrderModel order = businessProcessModel.getOrder();

        if (Objects.isNull(order)) {
            LOG.warn("The order process with ID {} has no order associated", businessProcessModel.getCode());
            return NOK;
        }

        LOG.info("Executing action NuveiCheckPaymentStatusAction for order {}", businessProcessModel.getCode());

        return strategyExecutor.execute(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getTransitions() {
        return AbstractAction.createTransitions(OK, NOK, WAIT);
    }
}
