package com.nuvei.addon.validators;

import com.nuvei.facades.beans.NuveiSDKResponseData;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Provides methods to validate the Nuvei response in payment step
 */
public interface NuveiCheckoutStepsValidator {

    /**
     * Will check the response result received from Nuvei
     *
     * @param redirectAttributes   Needed to add the {@link GlobalMessages} when the response has a invalid result
     * @param nuveiSDKResponseData The Nuvei response, obtained as response from the webSDK
     * @return false if there is no errors in nuveiSDKResponseData, true otherwise
     */
    boolean validateCheckoutSDKResponse(RedirectAttributes redirectAttributes,
                                        NuveiSDKResponseData nuveiSDKResponseData);

}
