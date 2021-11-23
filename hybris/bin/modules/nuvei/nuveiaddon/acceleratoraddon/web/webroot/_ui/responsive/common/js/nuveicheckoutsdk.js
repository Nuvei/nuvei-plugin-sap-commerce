ACC.nuveicheckoutsdk = {

    _autoload : [
        [ "openSdkIframe", $(".container_for_checkout").length !== 0 ]
    ],

    openSdkIframe: function () {
        $(document).ready(function () {
            var container = $(".container_for_checkout");
            var data = JSON.parse(JSON.stringify(container.data("checkoutrequestdata")));
            container.removeAttr('data-checkoutrequestdata');
            var ignorePM = [];
            var onResult = function (result){
                $("#checkoutSDKResponse").attr("value", JSON.stringify(result));
                $("#checkoutSDKResponseForm").submit();
            };
            data["onResult"] = onResult;
            data["renderTo"] = '.container_for_checkout';
            data["showResponseMessage"] = 'false';
            checkout(data);
        });
    },
}
