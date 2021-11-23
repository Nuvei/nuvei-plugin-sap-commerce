package com.nuvei.addon.validators.impl;

import com.nuvei.addon.validators.NuveiCheckoutStepsValidator;
import com.nuvei.facades.beans.NuveiSDKResponseData;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * {@inheritDoc}
 */
public class DefaultNuveiCheckoutStepsValidator implements NuveiCheckoutStepsValidator {

    protected static final Logger LOG = LogManager.getLogger(DefaultNuveiCheckoutStepsValidator.class);

    private static final String APPROVED = "APPROVED";
    private static final String SUCCESS = "SUCCESS";
    private static final String LOG_ERROR = "Nuvei CheckoutSDK() response result is {}";
    private static final String NUVEI_CHECKOUTSDK_ERROR_PROPERTY_KEY = "nuvei.checkout.error.checkoutsdk.result";
    public static final String THE_RESPONSE_FORMAT_IS_UNKNOWN = "The response format is unknown";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateCheckoutSDKResponse(final RedirectAttributes redirectAttributes,
                                               final NuveiSDKResponseData nuveiSDKResponseData) {
        if (APPROVED.equals(nuveiSDKResponseData.getResult()) || SUCCESS.equals(nuveiSDKResponseData.getResult())) {
            return false;
        }

        LOG.error(LOG_ERROR, StringUtils.isNotEmpty(nuveiSDKResponseData.getResult()) ? nuveiSDKResponseData.getResult() : THE_RESPONSE_FORMAT_IS_UNKNOWN);
        GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, NUVEI_CHECKOUTSDK_ERROR_PROPERTY_KEY);
        return true;
    }
}
