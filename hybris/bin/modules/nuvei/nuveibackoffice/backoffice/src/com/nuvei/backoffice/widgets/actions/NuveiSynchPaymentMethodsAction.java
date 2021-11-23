package com.nuvei.backoffice.widgets.actions;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.payments.NuveiFilteringPaymentMethodsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zul.Messagebox;

import javax.annotation.Resource;

/**
 * Custom backoffice action that triggers the import of the Payment Methods of all the merchants in the system
 */
public class NuveiSynchPaymentMethodsAction implements CockpitAction<Object, Object> {

    private static final Logger LOG = LogManager.getLogger(NuveiSynchPaymentMethodsAction.class);

    private static final String NOT_ABLE_TO_RETRIEVE_PAYMENT_METHODS = "Not able to retrieve payment methods: ";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_TITLE = "nuvei.actionResult.messageBox.failed.title";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_TITLE = "nuvei.actionResult.messageBox.success.title";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_MESSAGE = "nuvei.actionResult.messageBox.failed.message";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_MESSAGE = "nuvei.actionResult.messageBox.success.message";

    @Resource(name = "nuveiMerchantConfigurationService")
    protected NuveiMerchantConfigurationService nuveiMerchantConfigurationService;
    @Resource(name = "nuveiFilteringPaymentMethodsService")
    protected NuveiFilteringPaymentMethodsService nuveiFilteringPaymentMethodsService;

    /**
     * Imports the payment methods of all the merchants in the system
     *
     * @param actionContext The action context
     * @return Success if the import process has no errors. Errors otherwise.
     */
    @Override
    public ActionResult<Object> perform(final ActionContext<Object> actionContext) {
        try {
            for (NuveiMerchantConfigurationModel merchant : nuveiMerchantConfigurationService.getAllMerchantConfigurations()) {
                nuveiFilteringPaymentMethodsService.synchFilteringPaymentMethodsForMerchant(merchant);
            }
            showMessage(actionContext);
            return new ActionResult<>(ActionResult.SUCCESS);
        } catch (Exception e) {
            showErrorMessage(actionContext);
            LOG.error(NOT_ABLE_TO_RETRIEVE_PAYMENT_METHODS, e);
            return new ActionResult<>(ActionResult.ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canPerform(final ActionContext<Object> ctx) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsConfirmation(final ActionContext<Object> ctx) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage(final ActionContext<Object> ctx) {
        return null;
    }

    /**
     * Displays a success information message
     *
     * @param actionContext The context of the action
     */
    protected void showMessage(final ActionContext<Object> actionContext) {
        Messagebox.show(actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_MESSAGE),
                actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_TITLE), Messagebox.OK, Messagebox.INFORMATION);
    }

    /**
     * Displays an error information message
     *
     * @param actionContext The context of the action
     */
    protected void showErrorMessage(final ActionContext<Object> actionContext) {
        Messagebox.show(actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_MESSAGE),
                actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_TITLE), Messagebox.OK, Messagebox.ERROR);
    }
}
