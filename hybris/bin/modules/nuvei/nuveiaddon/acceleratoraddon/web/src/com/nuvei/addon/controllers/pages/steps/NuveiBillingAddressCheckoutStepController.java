package com.nuvei.addon.controllers.pages.steps;

import com.nuvei.addon.controllers.NuveiaddonControllerConstants;
import com.nuvei.addon.forms.NuveiBillingAddressForm;
import com.nuvei.facades.address.NuveiAddressFacade;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.util.AddressDataUtil;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Optional;

import static com.nuvei.addon.constants.NuveiaddonWebConstants.NUVEI_BILLING_ADDRESS_CMS_PAGE_LABEL;
import static de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants.SeoRobots.META_ROBOTS;

@Controller
@RequestMapping(value = "/checkout/multi/nuvei/billing-address")
public class NuveiBillingAddressCheckoutStepController extends AbstractCheckoutStepController {
    private static final String BILLING_ADDRESS = "billing-address";
    private static final String CART_DATA_ATTR = "cartData";
    private static final String BILLING_ADDRESS_FORM = "billingAddressForm";
    private static final String COUNTRY_ISO = "countryIso";
    private static final String DELIVERY_ADDRESS = "deliveryAddress";
    private static final String REGIONS = "regions";
    private static final String COUNTRY = "country";
    private static final String ADDRESS_ERROR_FORMENTRY_INVALID = "address.error.formentry.invalid";

    @Resource(name = "addressDataUtil")
    private AddressDataUtil addressDataUtil;
    @Resource(name = "nuveiAddressFacade")
    private NuveiAddressFacade nuveiAddressFacade;

    /**
     * @param model
     * @param redirectAttributes
     * @return returns the result for entering the billing address checkout step
     * @throws CMSItemNotFoundException
     */
    @Override
    @GetMapping(value = "/add")
    @RequireHardLogIn
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = BILLING_ADDRESS)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        final NuveiBillingAddressForm billingAddressForm = new NuveiBillingAddressForm();
        billingAddressForm.setUseDeliveryAddress(Boolean.FALSE);
        model.addAttribute(BILLING_ADDRESS_FORM, billingAddressForm);

        setupAddPaymentPage(model);

        return NuveiaddonControllerConstants.Views.Pages.MultiStepCheckout.BillingAddressPage;
    }

    /**
     * @param model
     * @param billingAddressPostForm
     * @param bindingResult
     * @return returns the result for entering the billing address checkout step
     * @throws CMSItemNotFoundException
     */
    @PostMapping(value = "/add-billing-address")
    @RequireHardLogIn
    public String add(final Model model, final NuveiBillingAddressForm billingAddressPostForm, final BindingResult bindingResult) throws CMSItemNotFoundException {
        setupAddPaymentPage(model);
        populatePageModel(model, billingAddressPostForm);

        if (validateAddressForm(model, billingAddressPostForm, bindingResult)) {
            return NuveiaddonControllerConstants.Views.Pages.MultiStepCheckout.BillingAddressPage;
        }

        try {
            if (Boolean.TRUE.equals(billingAddressPostForm.getUseDeliveryAddress())) {
                nuveiAddressFacade.setCartBillingAddress(getCheckoutFacade().getCheckoutCart().getDeliveryAddress());
            } else {
                nuveiAddressFacade.createAndSetCartBillingAddress(addressDataUtil.convertToAddressData(billingAddressPostForm));
            }
        } catch (final Exception e) {
            GlobalMessages.addErrorMessage(model, ADDRESS_ERROR_FORMENTRY_INVALID);
            return NuveiaddonControllerConstants.Views.Pages.MultiStepCheckout.BillingAddressPage;
        }

        setCheckoutStepLinksForModel(model, getCheckoutStep());

        return getCheckoutStep().nextStep();
    }

    /**
     * @param model
     * @param billingAddressPostForm
     * @param bindingResult
     * @return boolean if errors found
     */
    protected boolean validateAddressForm(final Model model, final NuveiBillingAddressForm billingAddressPostForm, final BindingResult bindingResult) {
        if (Boolean.FALSE.equals(billingAddressPostForm.getUseDeliveryAddress())) {
            getAddressValidator().validate(billingAddressPostForm, bindingResult);
            if (bindingResult.hasErrors()) {
                GlobalMessages.addErrorMessage(model, ADDRESS_ERROR_FORMENTRY_INVALID);
                return true;
            }
        }
        return false;
    }

    /**
     * populate common data
     * @param model
     * @param billingAddressPostForm
     */
    protected void populatePageModel(final Model model, final @Valid NuveiBillingAddressForm billingAddressPostForm) {
        model.addAttribute(BILLING_ADDRESS_FORM, billingAddressPostForm);
        model.addAttribute(COUNTRY_ISO, billingAddressPostForm.getCountryIso());

        if (StringUtils.isNotBlank(billingAddressPostForm.getCountryIso())) {
            model.addAttribute(REGIONS, getI18NFacade().getRegionsForCountryIso(billingAddressPostForm.getCountryIso()));
            model.addAttribute(COUNTRY, billingAddressPostForm.getCountryIso());
        }
    }

    /**
     *
     * @param countryIsoCode
     * @param useDeliveryAddress
     * @param model
     * @return the fragment with form populated
     */
    @GetMapping(value = "/prepare-billing-address-form")
    public String getCountryAddressForm(@RequestParam("countryIsoCode") final String countryIsoCode,
                                        @RequestParam("useDeliveryAddress") final boolean useDeliveryAddress, final Model model) {
        final NuveiBillingAddressForm billingAddressForm = new NuveiBillingAddressForm();
        model.addAttribute(BILLING_ADDRESS_FORM, billingAddressForm);

        if (StringUtils.isNotBlank(countryIsoCode)) {
            populateModelRegionAndCountry(model, countryIsoCode);
        }

        if (useDeliveryAddress) {
            Optional.ofNullable(getCheckoutFacade().getCheckoutCart())
                    .map(CartData::getDeliveryAddress).ifPresent(addressData -> {
                populateModelRegionAndCountry(model, addressData.getCountry().getIsocode());
                addressDataUtil.convert(addressData, billingAddressForm);
            });
        }

        return NuveiaddonControllerConstants.Views.Fragments.Checkout.BillingAddressForm;
    }

    /**
     * populate regions and country
     * @param model
     * @param countryIsoCode
     */
    protected void populateModelRegionAndCountry(final Model model, final String countryIsoCode) {
        model.addAttribute(REGIONS, getI18NFacade().getRegionsForCountryIso(countryIsoCode));
        model.addAttribute(COUNTRY, countryIsoCode);
    }

    /**
     * setup infor for page
     * @param model
     * @throws CMSItemNotFoundException
     */
    protected void setupAddPaymentPage(final Model model) throws CMSItemNotFoundException {
        prepareDataForPage(model);
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.paymentMethod.addPaymentDetails.billingAddress"));
        model.addAttribute(META_ROBOTS, "noindex,nofollow");

        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute(CART_DATA_ATTR, cartData);
        model.addAttribute(DELIVERY_ADDRESS, cartData.getDeliveryAddress());

        final ContentPageModel contentPage = getContentPageForLabelOrId(NUVEI_BILLING_ADDRESS_CMS_PAGE_LABEL);
        setUpMetaDataForContentPage(model, contentPage);
        storeCmsPageInModel(model, contentPage);
        setCheckoutStepLinksForModel(model, getCheckoutStep());
    }

    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().previousStep();
    }

    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().nextStep();
    }

    /**
     * get step for page billing address
     * @return step
     */
    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(BILLING_ADDRESS);
    }
}
