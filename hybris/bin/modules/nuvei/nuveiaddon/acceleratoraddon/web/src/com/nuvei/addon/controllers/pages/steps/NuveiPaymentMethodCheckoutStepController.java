package com.nuvei.addon.controllers.pages.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuvei.addon.controllers.NuveiaddonControllerConstants;
import com.nuvei.addon.validators.NuveiCheckoutStepsValidator;
import com.nuvei.facades.beans.NuveiCheckoutSDKRequestData;
import com.nuvei.facades.beans.NuveiSDKResponseData;
import com.nuvei.facades.checkoutsdk.NuveiCheckoutSDKRequestBuilder;
import com.nuvei.facades.openorder.NuveiOpenOrderFacade;
import com.nuvei.facades.payloads.NuveiPayloadsFacade;
import com.nuvei.facades.payment.NuveiPaymentInfoFacade;
import com.safecharge.exception.SafechargeConfigurationException;
import com.safecharge.exception.SafechargeException;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.order.InvalidCartException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;

import static com.nuvei.addon.constants.NuveiaddonWebConstants.NUVEI_BILLING_ADDRESS_CMS_PAGE_LABEL;
import static de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants.SeoRobots.META_ROBOTS;

/**
 * Web controller to handle a Payment checkout step
 */
@Controller
@RequestMapping(value = "/checkout/multi/nuvei/payment")
public class NuveiPaymentMethodCheckoutStepController extends AbstractCheckoutStepController {

    private static final Logger LOGGER = LogManager.getLogger(NuveiPaymentMethodCheckoutStepController.class);

    private static final String CART_DATA_ATTR = "cartData";
    private static final String PAYMENT_METHOD = "payment-method";
    private static final String DELIVERY_ADDRESS_ATTR = "deliveryAddress";
    private static final String CHECKOUT_SDK_REQUEST_DATA_ATTR = "checkoutSDKRequestData";
    private static final String PAYMENT_DETAILS_GENERAL_ERROR = "checkout.multi.paymentMethod.addPaymentDetails.generalError";
    private static final String NUVEIADDON_CHECKOUT_SDK_JS_PROPERTY_KEY = "nuveiaddon.checkout.sdk.js";
    private static final String NUVEIADDON_CHECKOUT_SDK_JS_ATTR = "checkoutSDKjs";

    @Resource(name = "nuveiPayloadsFacade")
    private NuveiPayloadsFacade nuveiPayloadsFacade;

    @Resource(name = "nuveiOpenOrderFacade")
    private NuveiOpenOrderFacade nuveiOpenOrderFacade;

    @Resource(name = "nuveiPaymentInfoFacade")
    private NuveiPaymentInfoFacade nuveiPaymentInfoFacade;

    @Resource(name = "nuveiCheckoutStepsValidator")
    private NuveiCheckoutStepsValidator nuveiCheckoutStepsValidator;

    @Resource(name = "nuveiCheckoutSDKRequestBuilder")
    private NuveiCheckoutSDKRequestBuilder nuveiCheckoutSDKRequestBuilder;

    /**
     * Returns the select payment method page that displays the Nuvei CheckoutSDK
     *
     * @param model              The model
     * @param redirectAttributes The redirect attributes
     * @return The payment method page if everything is correct, the previous page otherwise
     * @throws CMSItemNotFoundException
     */
    @Override
    @GetMapping(value = "/payment-method")
    @RequireHardLogIn
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = PAYMENT_METHOD)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        getCheckoutFacade().setDeliveryModeIfAvailable();

        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute(CART_DATA_ATTR, cartData);
        model.addAttribute(DELIVERY_ADDRESS_ATTR, cartData.getDeliveryAddress());

        try {
            final String sessionTokenId = nuveiOpenOrderFacade.requestOpenOrder();
            final String checkoutSDKRequestDataJson = getCheckoutSDKRequest(sessionTokenId);
            model.addAttribute(CHECKOUT_SDK_REQUEST_DATA_ATTR, checkoutSDKRequestDataJson);
            model.addAttribute(NUVEIADDON_CHECKOUT_SDK_JS_ATTR, getConfigurationService().getConfiguration().getString(NUVEIADDON_CHECKOUT_SDK_JS_PROPERTY_KEY));
        } catch (final SafechargeException | SafechargeConfigurationException | IllegalArgumentException | JsonProcessingException e) {
            LOGGER.error("Failed to build beginCreateSubscription request", e);
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, PAYMENT_DETAILS_GENERAL_ERROR);
            return getCheckoutStep().previousStep();
        }

        setupAddPaymentPage(model);

        return NuveiaddonControllerConstants.Views.Pages.MultiStepCheckout.ChoosePaymentMethodPage;
    }

    /**
     * Creates the configuration JSON for checkoutSDK request
     *
     * @param sessionTokenId sessionTokenID obtained from  checkoutSDK
     * @return the configuration JSON for checkoutSDK request
     * @throws JsonProcessingException
     */
    protected String getCheckoutSDKRequest(final String sessionTokenId) throws JsonProcessingException {
        final NuveiCheckoutSDKRequestData checkoutSDKRequestData = nuveiCheckoutSDKRequestBuilder.getCheckoutSDKRequestData(sessionTokenId);
        final String checkoutSDKRequestDataJson = new ObjectMapper().writeValueAsString(checkoutSDKRequestData);
        nuveiPayloadsFacade.setPaymentRequestPayload(checkoutSDKRequestDataJson);
        return checkoutSDKRequestDataJson;
    }

    @PostMapping(value = "/submit-payment-data")
    @RequireHardLogIn
    public String submitPaymentData(final String checkoutSDKResponse, final RedirectAttributes redirectAttributes) {
        try {
            nuveiPayloadsFacade.setPaymentResponsePayload(checkoutSDKResponse);
            final NuveiSDKResponseData checkoutSdkResponse = new ObjectMapper().readValue(checkoutSDKResponse, NuveiSDKResponseData.class);
            if (nuveiCheckoutStepsValidator.validateCheckoutSDKResponse(redirectAttributes, checkoutSdkResponse)) {
                return back(redirectAttributes);
            }

            nuveiPaymentInfoFacade.addPaymentInfoToCart(checkoutSdkResponse);

            final OrderData orderData = getCheckoutFacade().placeOrder();
            return redirectToOrderConfirmationPage(orderData);

        } catch (final JsonProcessingException | InvalidCartException e) {
            LOGGER.error("Failed to place Order", e);
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.placeOrder.failed");
            return back(redirectAttributes);
        }
    }

    /**
     * Set up the select payment method step model
     *
     * @param model the model
     * @throws CMSItemNotFoundException
     */
    protected void setupAddPaymentPage(final Model model) throws CMSItemNotFoundException {
        prepareDataForPage(model);
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.paymentMethod.addPaymentDetails.header"));
        model.addAttribute(META_ROBOTS, "noindex,nofollow");

        final ContentPageModel contentPage = getContentPageForLabelOrId(NUVEI_BILLING_ADDRESS_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, contentPage);
        setUpMetaDataForContentPage(model, contentPage);
        setCheckoutStepLinksForModel(model, getCheckoutStep());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().previousStep();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().nextStep();
    }

    /**
     * Gets the checkout step
     *
     * @return The checkout step
     */
    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(PAYMENT_METHOD);
    }
}
