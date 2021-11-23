package com.nuvei.services.service.impl;

import com.nuvei.services.service.NuveiOrderService;
import de.hybris.platform.basecommerce.enums.CancelReason;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.OrderCancelEntry;
import de.hybris.platform.ordercancel.OrderCancelException;
import de.hybris.platform.ordercancel.OrderCancelRequest;
import de.hybris.platform.ordercancel.OrderCancelService;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link NuveiOrderService}
 */
public class DefaultNuveiOrderService implements NuveiOrderService {

    private static final Logger LOG = LogManager.getLogger(DefaultNuveiOrderService.class);

    private static final String ERROR_VOIDING_ORDER_WITH = "Error voiding order with Id [%s] ";

    protected final GenericDao<AbstractOrderModel> abstractOrderGenericDao;
    protected final OrderCancelService orderCancelService;
    protected final UserService userService;

    /**
     * Default constructor {@link NuveiOrderService}
     *
     * @param abstractOrderGenericDao
     * @param orderCancelService
     * @param userService
     */
    public DefaultNuveiOrderService(final GenericDao<AbstractOrderModel> abstractOrderGenericDao,
                                    final OrderCancelService orderCancelService, final UserService userService) {
        this.abstractOrderGenericDao = abstractOrderGenericDao;
        this.orderCancelService = orderCancelService;
        this.userService = userService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractOrderModel findAbstractOrderModelByClientUniqueId(final String clientUniqueId) {
        return abstractOrderGenericDao.find(Collections.singletonMap(AbstractOrderModel.CLIENTUNIQUEID, clientUniqueId))
                .stream()
                .findAny()
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean requestCancelOrder(final OrderModel orderModel) {

        final List<OrderCancelEntry> orderCancelEntries = new ArrayList<>();

        orderModel.getEntries().forEach(entry ->
                orderCancelEntries.add(new OrderCancelEntry(entry, entry.getQuantity(), StringUtils.EMPTY, CancelReason.OTHER)));

        final OrderCancelRequest orderCancelRequest = new OrderCancelRequest(orderModel, orderCancelEntries);
        try {
            orderCancelService.requestOrderCancel(orderCancelRequest, userService.getAdminUser());
            return true;
        } catch (final OrderCancelException e) {
            LOG.error(String.format(ERROR_VOIDING_ORDER_WITH, orderModel.getCode()), e);
        }

        return false;
    }
}
