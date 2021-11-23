package com.nuvei.facades.openorder;

import com.safecharge.exception.SafechargeException;

/**
 * Provides methods to handle openOrder related operations
 */
public interface NuveiOpenOrderFacade {

    /**
     * This method should be used to create request for openOrder endpoint in Safecharge's API
     *
     * @return Session token if the request is success, otherwise throws a {@link SafechargeException}
     * @throws SafechargeException if there are request related problems
     */
    String requestOpenOrder() throws SafechargeException;

}
