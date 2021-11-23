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
import com.safecharge.exception.SafechargeException;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.acceleratorservices.config.SiteConfigService;
import de.hybris.platform.acceleratorservices.storefront.util.PageTitleResolver;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.Breadcrumb;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutGroup;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessage;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.cms2.data.PagePreviewCriteriaData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.cms2.servicelayer.services.CMSPageService;
import de.hybris.platform.cms2.servicelayer.services.CMSPreviewService;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nuvei.addon.constants.NuveiaddonWebConstants.NUVEI_BILLING_ADDRESS_CMS_PAGE_LABEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiPaymentMethodCheckoutStepControllerTest {

    private static final String NEXT = "next";
    private static final String PREVIOUS = "previous";
    private static final String CART_DATA_ATTR = "cartData";
    private static final String APPROVED_VALUE = "APPROVED";
    private static final String CHECKOUT_GROUP = "checkoutGroup";
    private static final String ORDER_DATA_CODE = "orderDataCode";
    private static final String PAYMENT_METHOD = "payment-method";
    private static final String ORDER_DATA_GUID = "orderDataGuid";
    private static final String SESSION_TOKEN_ID = "sessionTokenID";
    private static final String OMS_ENABLED_PROPERTY = "oms.enabled";
    private static final String DELIVERY_ADDRESS_ATTR = "deliveryAddress";
    private static final String CHECKOUT_SDK_RESPONSE = "{\"result\":\"APPROVED\"}";
    private static final String PLACE_ORDER_ERROR_MSG = "checkout.placeOrder.failed";
    private static final String CHECKOUT_SDK_REQUEST_DATA_ATTR = "checkoutSDKRequestData";
    private static final String REDIRECT_URL_ORDER_CONFIRMATION = "redirect:/checkout/orderConfirmation/";
    private static final String PAYMENTS_GENERAL_ERROR = "checkout.multi.paymentMethod.addPaymentDetails.generalError";
    private static final String NUVEIADDON_CHECKOUT_SDK_JS_PROPERTY_KEY = "nuveiaddon.checkout.sdk.js";
    private static final String NUVEIADDON_CHECKOUT_SDK_JS_ATTR = "checkoutSDKjs";
    public static final String CHECKOUT_SDK_JS_URL = "checkoutSDKjsURL";

    @InjectMocks
    private NuveiPaymentMethodCheckoutStepController testObj;

    @Mock
    private CMSPageService cmsPageServiceMock;
    @Mock
    private CMSPreviewService cmsPreviewServiceMock;
    @Mock
    private PageTitleResolver pageTitleResolverMock;
    @Mock
    private SiteConfigService siteConfigServiceMock;
    @Mock
    private AcceleratorCheckoutFacade checkoutFacadeMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigurationService configurationServiceMock;
    @Mock
    private NuveiOpenOrderFacade nuveiOpenOrderFacadeMock;
    @Mock
    private NuveiPaymentInfoFacade nuveiPaymentInfoFacadeMock;
    @Mock
    private ResourceBreadcrumbBuilder resourceBreadcrumbBuilderMock;
    @Mock
    private NuveiCheckoutSDKRequestBuilder nuveiCheckoutSDKRequestBuilderMock;

    @Mock
    private Model modelMock;
    @Mock
    private CartData cartDataMock;
    @Mock
    private OrderData orderDataMock;
    @Mock
    private Breadcrumb breadcrumbMock;
    @Mock
    private AddressData addressDataMock;
    @Mock
    private CheckoutStep checkoutStepMock;
    @Mock
    private CheckoutGroup checkoutGroupMock;
    @Mock
    private ContentPageModel contentPageModelMock;
    @Mock
    private RedirectAttributes redirectAttributesMock;
    @Mock
    private NuveiPayloadsFacade nuveiPayloadsFacadeMock;
    @Mock
    private PagePreviewCriteriaData pagePreviewCriteriaDataMock;
    @Mock
    private Map<String, CheckoutGroup> checkoutFlowGroupMapMock;
    @Mock
    private CheckoutCustomerStrategy checkoutCustomerStrategyMock;
    @Mock
    private NuveiCheckoutStepsValidator nuveiCheckoutStepsValidatorMock;
    @Captor
    private ArgumentCaptor<NuveiSDKResponseData> checkoutSDKResponseDataCaptor;

    private final RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
    private final NuveiCheckoutSDKRequestData nuveiCheckoutSDKRequestDataMock = new NuveiCheckoutSDKRequestData();

    @Before
    public void setUp() throws Exception {
        when(checkoutFacadeMock.getCheckoutCart()).thenReturn(cartDataMock);
        when(cartDataMock.getDeliveryAddress()).thenReturn(addressDataMock);
        when(checkoutStepMock.getTransitions()).thenReturn(new HashMap<>());
        when(checkoutGroupMock.getValidationResultsMap()).thenReturn(new HashMap<>());
        when(nuveiOpenOrderFacadeMock.requestOpenOrder()).thenReturn(SESSION_TOKEN_ID);
        when(checkoutFlowGroupMapMock.get(CHECKOUT_GROUP)).thenReturn(checkoutGroupMock);
        when(checkoutFacadeMock.getCheckoutFlowGroupForCheckout()).thenReturn(CHECKOUT_GROUP);
        when(cmsPreviewServiceMock.getPagePreviewCriteria()).thenReturn(pagePreviewCriteriaDataMock);
        when(checkoutGroupMock.getCheckoutStepMap()).thenReturn(Map.of(PAYMENT_METHOD, checkoutStepMock));
        when(siteConfigServiceMock.getBoolean(OMS_ENABLED_PROPERTY, Boolean.FALSE)).thenReturn(Boolean.TRUE);
        when(nuveiCheckoutSDKRequestBuilderMock.getCheckoutSDKRequestData(SESSION_TOKEN_ID))
                .thenReturn(nuveiCheckoutSDKRequestDataMock);
        when(cmsPageServiceMock.getPageForLabelOrId(NUVEI_BILLING_ADDRESS_CMS_PAGE_LABEL, pagePreviewCriteriaDataMock))
                .thenReturn(contentPageModelMock);
        when(resourceBreadcrumbBuilderMock.getBreadcrumbs(WebConstants.BREADCRUMBS_KEY))
                .thenReturn(List.of(breadcrumbMock));
        when(configurationServiceMock.getConfiguration().getString(NUVEIADDON_CHECKOUT_SDK_JS_PROPERTY_KEY))
                .thenReturn(CHECKOUT_SDK_JS_URL);
    }

    @Test
    public void enterStep_ShouldCallOpenOrderAndRedirectToNextStepWithTheCheckoutSDKRequest_WhenNoExceptionIsThrown()
            throws CMSItemNotFoundException, JsonProcessingException {
        final String result = testObj.enterStep(modelMock, redirectAttributesMock);

        final String requestJson = new ObjectMapper().writeValueAsString(nuveiCheckoutSDKRequestDataMock);

        verify(checkoutFacadeMock).setDeliveryModeIfAvailable();
        verify(modelMock).addAttribute(CART_DATA_ATTR, cartDataMock);
        verify(modelMock).addAttribute(DELIVERY_ADDRESS_ATTR, addressDataMock);
        verify(modelMock).addAttribute(NUVEIADDON_CHECKOUT_SDK_JS_ATTR, CHECKOUT_SDK_JS_URL);
        verify(modelMock).addAttribute(CHECKOUT_SDK_REQUEST_DATA_ATTR,
                new ObjectMapper().writeValueAsString(nuveiCheckoutSDKRequestDataMock));
        verify(nuveiPayloadsFacadeMock).setPaymentRequestPayload(requestJson);

        assertThat(result)
                .isEqualTo(NuveiaddonControllerConstants.Views.Pages.MultiStepCheckout.ChoosePaymentMethodPage);
    }

    @Test
    public void enterStep_ShouldGoBackToPreviousStep_WhenExceptionIsShown()
            throws CMSItemNotFoundException, SafechargeException {
        when(nuveiOpenOrderFacadeMock.requestOpenOrder()).thenThrow(SafechargeException.class);
        when(checkoutStepMock.previousStep()).thenReturn(PREVIOUS);

        final String result = testObj.enterStep(modelMock, redirectAttributes);

        verify(checkoutFacadeMock).setDeliveryModeIfAvailable();
        verify(modelMock).addAttribute(CART_DATA_ATTR, cartDataMock);
        verify(modelMock).addAttribute(DELIVERY_ADDRESS_ATTR, addressDataMock);

        final boolean hasErrorMessage = ((List<GlobalMessage>) redirectAttributes
                .getFlashAttributes()
                .get(GlobalMessages.ERROR_MESSAGES_HOLDER)).stream()
                .map(GlobalMessage::getCode)
                .anyMatch(PAYMENTS_GENERAL_ERROR::equals);

        assertThat(hasErrorMessage).isTrue();
        assertThat(result).isEqualTo(PREVIOUS);
    }

    @Test
    public void back_ShouldReturnToPreviousStep() {
        when(checkoutStepMock.previousStep()).thenReturn(PREVIOUS);

        final String result = testObj.back(redirectAttributesMock);

        assertThat(result).isEqualTo(PREVIOUS);
    }

    @Test
    public void next_ShouldGoToNextStep() {
        when(checkoutStepMock.nextStep()).thenReturn(NEXT);

        final String result = testObj.next(redirectAttributesMock);

        assertThat(result).isEqualTo(NEXT);
    }

    @Test
    public void submitPaymentData_ShouldGoToPreviousStep_WhenValidationFails() {
        when(nuveiCheckoutStepsValidatorMock.validateCheckoutSDKResponse(eq(redirectAttributesMock),
                checkoutSDKResponseDataCaptor.capture())).thenReturn(true);
        when(checkoutStepMock.previousStep()).thenReturn(PREVIOUS);

        final String result = testObj.submitPaymentData(CHECKOUT_SDK_RESPONSE, redirectAttributesMock);

        assertThat(result).isEqualTo(PREVIOUS);
    }

    @Test
    public void submitPaymentData_ShouldPlaceOrderAndShowOrderConfirmationPageWithUserLoggedIn_WhenSuccessAndLoggedUser()
            throws InvalidCartException {
        when(nuveiCheckoutStepsValidatorMock.validateCheckoutSDKResponse(eq(redirectAttributesMock),
                checkoutSDKResponseDataCaptor.capture())).thenReturn(false);
        when(orderDataMock.getGuid()).thenReturn(ORDER_DATA_GUID);
        when(checkoutFacadeMock.placeOrder()).thenReturn(orderDataMock);
        when(checkoutCustomerStrategyMock.isAnonymousCheckout()).thenReturn(Boolean.TRUE);

        final String result = testObj.submitPaymentData(CHECKOUT_SDK_RESPONSE, redirectAttributesMock);

        final NuveiSDKResponseData nuveiSDKResponseData = checkoutSDKResponseDataCaptor.getValue();

        verify(nuveiPayloadsFacadeMock).setPaymentResponsePayload(CHECKOUT_SDK_RESPONSE);
        verify(nuveiPaymentInfoFacadeMock).addPaymentInfoToCart(nuveiSDKResponseData);
        assertThat(nuveiSDKResponseData.getResult()).isEqualTo(APPROVED_VALUE);
        assertThat(result).isEqualTo(REDIRECT_URL_ORDER_CONFIRMATION + ORDER_DATA_GUID);
    }

    @Test
    public void submitPaymentData_ShouldPlaceOrderAndShowOrderConfirmationPageWithAnonymousUser_WhenSuccessAndNotLoggedUser()
            throws InvalidCartException {
        when(nuveiCheckoutStepsValidatorMock.validateCheckoutSDKResponse(eq(redirectAttributesMock),
                checkoutSDKResponseDataCaptor.capture())).thenReturn(false);
        when(checkoutFacadeMock.placeOrder()).thenReturn(orderDataMock);
        when(checkoutCustomerStrategyMock.isAnonymousCheckout()).thenReturn(Boolean.FALSE);
        when(orderDataMock.getCode()).thenReturn(ORDER_DATA_CODE);

        final String result = testObj.submitPaymentData(CHECKOUT_SDK_RESPONSE, redirectAttributesMock);

        final NuveiSDKResponseData nuveiSDKResponseData = checkoutSDKResponseDataCaptor.getValue();

        assertThat(nuveiSDKResponseData.getResult()).isEqualTo(APPROVED_VALUE);
        assertThat(result).isEqualTo(REDIRECT_URL_ORDER_CONFIRMATION + ORDER_DATA_CODE);
    }

    @Test
    public void submitPaymentData_ShouldReturnToPreviousStepAndShowError_WhenExceptionIsThrown()
            throws InvalidCartException {
        when(checkoutStepMock.previousStep()).thenReturn(PREVIOUS);
        when(nuveiCheckoutStepsValidatorMock.validateCheckoutSDKResponse(eq(redirectAttributes),
                checkoutSDKResponseDataCaptor.capture())).thenReturn(false);
        when(checkoutFacadeMock.placeOrder()).thenThrow(InvalidCartException.class);

        final String result = testObj.submitPaymentData(CHECKOUT_SDK_RESPONSE, redirectAttributes);

        final boolean hasErrorMessage = ((List<GlobalMessage>) redirectAttributes
                .getFlashAttributes()
                .get(GlobalMessages.ERROR_MESSAGES_HOLDER)).stream()
                .map(GlobalMessage::getCode)
                .anyMatch(PLACE_ORDER_ERROR_MSG::equals);

        assertThat(hasErrorMessage).isTrue();
        assertThat(result).isEqualTo(PREVIOUS);
    }
}
