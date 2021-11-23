
package com.nuvei.order.actions;

import com.nuvei.strategy.NuveiStrategyExecutor;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Set;

/**
 * Action to check if the order process has an order with valid payment transaction entry
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NuveiCheckAuthorizeOrderPaymentAction extends AbstractAction<OrderProcessModel> {

    private static final Logger LOG = LogManager.getLogger(NuveiCheckAuthorizeOrderPaymentAction.class);
    protected static final String OK = "OK";
    protected static final String NOK = "NOK";
    protected static final String WAIT = "WAIT";

    private final NuveiStrategyExecutor<OrderModel, String> strategyExecutor;

    /**
     * Default constructor for {@link NuveiCheckAuthorizeOrderPaymentAction}
     *
     * @param strategyExecutor injected
     */
    public NuveiCheckAuthorizeOrderPaymentAction(final NuveiStrategyExecutor<OrderModel, String> strategyExecutor) {
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

        LOG.info("Executing action NuveiCheckAuthorizeOrderPaymentAction for order {}", businessProcessModel.getCode());

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
