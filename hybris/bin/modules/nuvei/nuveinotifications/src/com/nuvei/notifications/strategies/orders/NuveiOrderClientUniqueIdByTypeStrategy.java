package com.nuvei.notifications.strategies.orders;

import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import com.nuvei.services.service.NuveiOrderService;
import com.nuvei.strategy.NuveiStrategy;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Find {@link AbstractOrderModel} by default {@link NuveiTransactionType}
 */
public class NuveiOrderClientUniqueIdByTypeStrategy implements NuveiStrategy<Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel>, AbstractOrderModel> {

    private static final Logger LOG = LogManager.getLogger(NuveiOrderClientUniqueIdByTypeStrategy.class);

    protected final NuveiOrderService nuveiOrderService;

    public NuveiOrderClientUniqueIdByTypeStrategy(final NuveiOrderService nuveiOrderService) {
        this.nuveiOrderService = nuveiOrderService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractOrderModel execute(final Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel> source) {
        LOG.info("Executing strategy  {}", NuveiOrderClientUniqueIdByTypeStrategy.class.getName());
        return nuveiOrderService.findAbstractOrderModelByClientUniqueId(source.getRight().getMerchantUniqueId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final Pair<NuveiTransactionType, NuveiDirectMerchantNotificationModel> source) {
        return NuveiTransactionType.AUTH.equals(source.getLeft()) ||
                NuveiTransactionType.SALE.equals(source.getLeft());
    }
}
