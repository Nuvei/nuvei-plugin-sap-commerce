package com.nuvei.addon.controllers;

/**
 */
@SuppressWarnings({"PMD.VariableNamingConventions", "squid:S1214", "squid:S00115"})
public interface NuveiaddonControllerConstants {

    interface Views {
        String _AddonPrefix = "addon:/nuveiaddon/";

        interface Fragments {

            interface Checkout {
                String BillingAddressForm = _AddonPrefix + "fragments/checkout/billingAddressForm";
            }
        }

        interface Pages {

            interface MultiStepCheckout {
                String BillingAddressPage = _AddonPrefix + "pages/checkout/multi/nuveiBillingAddressPage";
                String ChoosePaymentMethodPage = _AddonPrefix + "pages/checkout/multi/nuveiChoosePaymentMethodPage";
            }

        }

    }
}
