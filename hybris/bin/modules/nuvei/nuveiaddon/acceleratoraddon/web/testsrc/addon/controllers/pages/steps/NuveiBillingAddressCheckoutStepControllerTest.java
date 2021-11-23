package addon.controllers.pages.steps;

import com.nuvei.addon.controllers.NuveiaddonControllerConstants;
import com.nuvei.addon.controllers.pages.steps.NuveiBillingAddressCheckoutStepController;
import com.nuvei.addon.forms.NuveiBillingAddressForm;
import com.nuvei.facades.address.NuveiAddressFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.acceleratorservices.config.SiteConfigService;
import de.hybris.platform.acceleratorservices.storefront.util.PageTitleResolver;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.Breadcrumb;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutGroup;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.AddressValidator;
import de.hybris.platform.acceleratorstorefrontcommons.util.AddressDataUtil;
import de.hybris.platform.cms2.data.PagePreviewCriteriaData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.cms2.servicelayer.services.CMSPageService;
import de.hybris.platform.cms2.servicelayer.services.CMSPreviewService;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.commerceservices.enums.CountryType;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiBillingAddressCheckoutStepControllerTest {

    private static final String CHECKOUT_MULTI_PAYMENT_METHOD_ADD_PAYMENT_DETAILS_BILLING_ADDRESS = "checkout.multi.paymentMethod.addPaymentDetails.billingAddress";
    private static final String BILLING_ADDRESS_FORM = "billingAddressForm";
    private static final String COUNTRY_ISO_CODE = "countryIsoCode";
    private static final String BILLING_ADDRESS = "billing-address";
    private static final String REGION_ISO_CODE = "regionIsoCode";
    private static final String KEY = "nuveiBillingAddressPage";
    private static final String PREVIOUS_STEP = "previousStep";
    private static final String CURRENT_STEP = "currentStep";
    private static final String OMS_ENABLED = "oms.enabled";
    private static final String DESCRIPTION = "description";
    private static final String NEXT_STEP = "nextStep";
    private static final String KEYWORDS = "keywords";
    private static final String REGIONS = "regions";
    private static final String COUNTRY = "country";
    private static final String TITLE = "title";
    private static final String GROUP = "group";

    @InjectMocks
    private NuveiBillingAddressCheckoutStepController testObj;

    @Mock
    private AcceleratorCheckoutFacade checkoutFacadeMock;
    @Mock
    private I18NFacade i18NFacadeMock;
    @Mock
    private CMSPageService cmsPageServiceMock;
    @Mock
    private CMSPreviewService cmsPreviewServiceMock;
    @Mock
    private RedirectAttributes redirectAttributesMock;
    @Mock
    private SiteConfigService siteConfigServiceMock;
    @Mock
    private PageTitleResolver pageTitleResolverMock;
    @Mock
    private AddressDataUtil addressDataUtilMock;
    @Mock
    private NuveiAddressFacade nuveiAddressFacadeMock;
    @Mock
    private AddressValidator addressValidator;

    @Mock
    private Model modelMock;
    @Mock
    private AddressData addressDataMock;
    @Mock
    private CountryData countryData1Mock, countryData2Mock;
    @Mock
    private CartData cartDataMock;
    @Mock
    private RegionData regionDataMock, regionDataTwoMock;
    @Mock
    private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;
    @Mock
    private Breadcrumb breadcrumbMock;
    @Mock
    private ContentPageModel contentPageModelMock;
    @Mock
    private PagePreviewCriteriaData pagePreviewCriteriaDataMock;
    @Mock
    private CheckoutGroup checkoutGroupMock;
    @Mock
    private Map<String, CheckoutGroup> mapGroupStub;
    @Mock
    private CheckoutStep checkoutStep;
    @Mock
    private NuveiBillingAddressForm billingAddressFormMock;
    @Mock
    private BindingResult bindingResultMock;

    private final Map<String, CheckoutStep> mapStepStub = new HashMap<>();

    private List<RegionData> regionDataMockList;

    @Captor
    private ArgumentCaptor<NuveiBillingAddressForm> billingAddressFormCaptor;

    @Before
    public void setUp() throws Exception {
        when(checkoutFacadeMock.getCheckoutCart()).thenReturn(cartDataMock);
        when(checkoutFacadeMock.getCheckoutCart().getDeliveryAddress()).thenReturn(addressDataMock);
        when(checkoutFacadeMock.getCountries(CountryType.SHIPPING)).thenReturn(List.of(countryData1Mock, countryData2Mock));
        when(checkoutFacadeMock.getCountries(CountryType.BILLING)).thenReturn(List.of(countryData1Mock, countryData2Mock));
        when(checkoutFacadeMock.isExpressCheckoutAllowedForCart()).thenReturn(Boolean.TRUE);
        when(checkoutFacadeMock.isTaxEstimationEnabledForCart()).thenReturn(Boolean.TRUE);
        when(siteConfigServiceMock.getBoolean(OMS_ENABLED, false)).thenReturn(Boolean.TRUE);
        when(resourceBreadcrumbBuilder.getBreadcrumbs(CHECKOUT_MULTI_PAYMENT_METHOD_ADD_PAYMENT_DETAILS_BILLING_ADDRESS))
                .thenReturn(Collections.singletonList(breadcrumbMock));
        when(cmsPreviewServiceMock.getPagePreviewCriteria()).thenReturn(pagePreviewCriteriaDataMock);
        when(cmsPageServiceMock.getPageForLabelOrId(KEY, cmsPreviewServiceMock.getPagePreviewCriteria())).thenReturn(contentPageModelMock);
        when(contentPageModelMock.getKeywords()).thenReturn(KEYWORDS);
        when(contentPageModelMock.getDescription()).thenReturn(DESCRIPTION);
        when(contentPageModelMock.getDescription()).thenReturn(DESCRIPTION);
        when(contentPageModelMock.getTitle()).thenReturn(TITLE);
        when(pageTitleResolverMock.resolveContentPageTitle(contentPageModelMock.getTitle())).thenReturn(TITLE);
        when(checkoutGroupMock.getCheckoutStepMap()).thenReturn(mapStepStub);
        mapStepStub.put(BILLING_ADDRESS, checkoutStep);
        when(checkoutFacadeMock.getCheckoutFlowGroupForCheckout()).thenReturn(GROUP);
        when(mapGroupStub.get(GROUP)).thenReturn(checkoutGroupMock);
        when(checkoutStep.previousStep()).thenReturn(PREVIOUS_STEP);
        when(checkoutStep.currentStep()).thenReturn(CURRENT_STEP);
        when(checkoutStep.nextStep()).thenReturn(NEXT_STEP);
        regionDataMockList = List.of(this.regionDataMock, regionDataTwoMock);
    }

    @Test
    public void enterStep_shouldReturnBillingAddressPage_whenHasErrors() throws CMSItemNotFoundException {
        final String result = testObj.enterStep(modelMock, redirectAttributesMock);

        assertThat(result).isEqualTo(NuveiaddonControllerConstants.Views.Pages.MultiStepCheckout.BillingAddressPage);
    }

    @Test
    public void add_ShouldReturnBillingAddressPage_WhenBindingResultHasErrors() throws CMSItemNotFoundException {
        when(bindingResultMock.hasErrors()).thenReturn(Boolean.TRUE);
        when(billingAddressFormMock.getUseDeliveryAddress()).thenReturn(Boolean.FALSE);
        doNothing().when(addressValidator).validate(billingAddressFormMock, bindingResultMock);

        final String result = testObj.add(modelMock, billingAddressFormMock, bindingResultMock);

        assertThat(result).isEqualTo(NuveiaddonControllerConstants.Views.Pages.MultiStepCheckout.BillingAddressPage);
    }

    @Test
    public void add_ShouldReturnNextStep_WhenBindingResultHasNoErrorsAndUseDeliveryAddressIsTrue() throws CMSItemNotFoundException {
        when(billingAddressFormMock.getUseDeliveryAddress()).thenReturn(Boolean.TRUE);
        doNothing().when(addressValidator).validate(billingAddressFormMock, bindingResultMock);

        final String result = testObj.add(modelMock, billingAddressFormMock, bindingResultMock);

        assertThat(result).isEqualTo(NEXT_STEP);
    }

    @Test
    public void add_ShouldReturnNextStep_WhenBindingResultHasNoErrorsUseDeliveryAddressIsFalse() throws CMSItemNotFoundException {
        when(bindingResultMock.hasErrors()).thenReturn(Boolean.FALSE);
        when(billingAddressFormMock.getUseDeliveryAddress()).thenReturn(Boolean.FALSE);

        final String result = testObj.add(modelMock, billingAddressFormMock, bindingResultMock);

        assertThat(result).isEqualTo(NEXT_STEP);
    }

    @Test
    public void getCountryAddressForm_ShouldFillForm_WhenUseDeliveryAddressIsTrue() {
        when(i18NFacadeMock.getCountryForIsocode(COUNTRY_ISO_CODE)).thenReturn(countryData1Mock);
        when(addressDataMock.getCountry()).thenReturn(countryData1Mock);
        when(addressDataMock.getCountry().getIsocode()).thenReturn(COUNTRY_ISO_CODE);
        when(addressDataMock.getRegion()).thenReturn(regionDataMock);
        when(addressDataMock.getRegion().getIsocode()).thenReturn(REGION_ISO_CODE);

        when(i18NFacadeMock.getRegionsForCountryIso(COUNTRY_ISO_CODE)).thenReturn(regionDataMockList);
        doNothing().when(addressDataUtilMock).convert(addressDataMock, billingAddressFormMock);

        final String result = testObj.getCountryAddressForm(COUNTRY_ISO_CODE, Boolean.TRUE, modelMock);

        verify(modelMock).addAttribute(eq(BILLING_ADDRESS_FORM), billingAddressFormCaptor.capture());
        final NuveiBillingAddressForm billingAddressForm = billingAddressFormCaptor.getValue();
        verify(addressDataUtilMock).convert(addressDataMock, billingAddressForm);
        verify(modelMock, times(2)).addAttribute(REGIONS, regionDataMockList);
        verify(modelMock, times(2)).addAttribute(COUNTRY, COUNTRY_ISO_CODE);

        assertThat(result).isEqualTo(NuveiaddonControllerConstants.Views.Fragments.Checkout.BillingAddressForm);
    }

    @Test
    public void getCountryAddressForm_ShouldFillForm_WhenUseDeliveryAddressIsFalse() {
        when(i18NFacadeMock.getCountryForIsocode(COUNTRY_ISO_CODE)).thenReturn(countryData1Mock);
        when(addressDataMock.getCountry()).thenReturn(countryData1Mock);
        when(addressDataMock.getCountry().getIsocode()).thenReturn(COUNTRY_ISO_CODE);
        when(addressDataMock.getRegion()).thenReturn(regionDataMock);
        when(addressDataMock.getRegion().getIsocode()).thenReturn(REGION_ISO_CODE);

        when(i18NFacadeMock.getRegionsForCountryIso(COUNTRY_ISO_CODE)).thenReturn(regionDataMockList);
        doNothing().when(addressDataUtilMock).convert(addressDataMock, billingAddressFormMock);

        final String result = testObj.getCountryAddressForm(COUNTRY_ISO_CODE, Boolean.FALSE, modelMock);

        verify(modelMock).addAttribute(eq(BILLING_ADDRESS_FORM), billingAddressFormCaptor.capture());
        verify(modelMock).addAttribute(REGIONS, regionDataMockList);
        verify(modelMock).addAttribute(COUNTRY, COUNTRY_ISO_CODE);

        assertThat(result).isEqualTo(NuveiaddonControllerConstants.Views.Fragments.Checkout.BillingAddressForm);
    }

    @Test
    public void getCountryAddressForm_ShouldFillForm_WhenCountryIsoIsEmpty() {
        when(i18NFacadeMock.getCountryForIsocode(COUNTRY_ISO_CODE)).thenReturn(countryData1Mock);
        when(addressDataMock.getCountry()).thenReturn(countryData1Mock);
        when(addressDataMock.getCountry().getIsocode()).thenReturn(COUNTRY_ISO_CODE);
        when(addressDataMock.getRegion()).thenReturn(regionDataMock);
        when(addressDataMock.getRegion().getIsocode()).thenReturn(REGION_ISO_CODE);

        when(i18NFacadeMock.getRegionsForCountryIso(COUNTRY_ISO_CODE)).thenReturn(regionDataMockList);
        doNothing().when(addressDataUtilMock).convert(addressDataMock, billingAddressFormMock);

        final String result = testObj.getCountryAddressForm(StringUtils.EMPTY, Boolean.TRUE, modelMock);

        verify(modelMock).addAttribute(eq(BILLING_ADDRESS_FORM), billingAddressFormCaptor.capture());
        final NuveiBillingAddressForm billingAddressForm = billingAddressFormCaptor.getValue();
        verify(addressDataUtilMock).convert(addressDataMock, billingAddressForm);
        verify(modelMock).addAttribute(REGIONS, regionDataMockList);
        verify(modelMock).addAttribute(COUNTRY, COUNTRY_ISO_CODE);

        assertThat(result).isEqualTo(NuveiaddonControllerConstants.Views.Fragments.Checkout.BillingAddressForm);
    }

    @Test
    public void getCountryAddressForm_ShouldNotCallAddressUtilConvert_WhenCheckoutCartIsNull() {
        when(i18NFacadeMock.getCountryForIsocode(COUNTRY_ISO_CODE)).thenReturn(countryData1Mock);
        when(checkoutFacadeMock.getCheckoutCart()).thenReturn(null);

        when(i18NFacadeMock.getRegionsForCountryIso(COUNTRY_ISO_CODE)).thenReturn(regionDataMockList);

        testObj.getCountryAddressForm(COUNTRY_ISO_CODE, Boolean.TRUE, modelMock);

        verifyZeroInteractions(addressDataUtilMock);
        verify(modelMock).addAttribute(eq(BILLING_ADDRESS_FORM), any(NuveiBillingAddressForm.class));
        verify(modelMock).addAttribute(REGIONS, regionDataMockList);
        verify(modelMock).addAttribute(COUNTRY, COUNTRY_ISO_CODE);
    }
}
