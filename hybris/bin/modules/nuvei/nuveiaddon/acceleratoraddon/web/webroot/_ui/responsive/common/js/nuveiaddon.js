ACC.nuveiaddon = {

    _autoload: [
        "bindUseDeliveryAddress",
        "bindSubmitBillingAddressPostForm",
        "bindBillingAddressForm"
    ],

    spinner: $("<img>").attr("src", ACC.config.commonResourcePath + "/images/spinner.gif"),

    bindUseDeliveryAddress: function () {
        $('#nuveiUseDeliveryAddress').on('change', function () {
            if ($('#nuveiUseDeliveryAddress').is(":checked")) {
                var options = {'countryIsoCode': $('#useDeliveryAddressData').data('countryisocode'), 'useDeliveryAddress': true};
                ACC.nuveiaddon.enableAddressForm();
                ACC.nuveiaddon.displayBillingAddressForm(options, ACC.nuveiaddon.useDeliveryAddressSelected());
                ACC.nuveiaddon.disableAddressForm();
            } else {
                ACC.nuveiaddon.clearAddressForm();
                ACC.nuveiaddon.enableAddressForm();
            }
        });

        if ($('#nuveiUseDeliveryAddress').is(":checked")) {
            var options = {'countryIsoCode': $('#useDeliveryAddressData').data('countryisocode'), 'useDeliveryAddress': true};
            ACC.nuveiaddon.enableAddressForm();
            ACC.nuveiaddon.displayBillingAddressForm(options, ACC.nuveiaddon.useDeliveryAddressSelected());
            ACC.nuveiaddon.disableAddressForm();
        }
    },

    bindSubmitBillingAddressPostForm: function () {
        $("#nextToPaymentMethod").click(
            function (event) {
                event.preventDefault();
                ACC.common.blockFormAndShowProcessingMessage($(this));
                $("#nuveiBillingAddressForm").filter(":hidden").remove();
                ACC.nuveiaddon.enableAddressForm();
                $("#billingAddressPostForm").submit();
            }
        );
    },

    isEmpty: function (obj) {
        if (typeof obj == 'undefined' || obj === null || obj === '') return true;
        return false;
    },

    disableAddressForm: function () {
        $('input[id^="address\\."]').prop('disabled', true);
        $('select[id^="address\\."]').prop('disabled', true);
    },

    enableAddressForm: function () {
        $('input[id^="address\\."]').prop('disabled', false);
        $('select[id^="address\\."]').prop('disabled', false);
    },

    clearAddressForm: function () {
        $('input[id^="address\\."]').val("");
        $('select[id^="address\\."]').val("");
    },

    useDeliveryAddressSelected: function () {
        if ($('#nuveiUseDeliveryAddress').is(":checked")) {
            var countryIsoCode = $('#address\\.country').val($('#useDeliveryAddressData').data('countryisocode')).val();
            if(ACC.nuveiaddon.isEmpty(countryIsoCode)) {
                $('#nuveiUseDeliveryAddress').click();
                $('#nuveiUseDeliveryAddress').parent().hide();
            }
            else {
                ACC.nuveiaddon.disableAddressForm();
            }
        }
        else {
            ACC.nuveiaddon.clearAddressForm();
            ACC.nuveiaddon.enableAddressForm();
        }
    },

    bindBillingAddressForm: function () {
        $('#billingCountrySelector :input').on("change", function () {
            var countrySelection = $(this).val();
            var options = {
                'countryIsoCode': countrySelection,
                'useDeliveryAddress': false
            };
            ACC.nuveiaddon.displayBillingAddressForm(options);
        })
    },

    displayBillingAddressForm: function (options, callback) {
        $.ajax({
            url: ACC.config.encodedContextPath + '/checkout/multi/nuvei/billing-address/prepare-billing-address-form',
            async: false,
            data: options,
            dataType: "html",
            beforeSend: function () {
                $('#nuveiBillingAddressForm').html(ACC.nuveiaddon.spinner);
            }
        }).done(function (data) {
            $("#nuveiBillingAddressForm").html(data);
            if (typeof callback == 'function') {
                callback.call();
            }
        });
    }
}
