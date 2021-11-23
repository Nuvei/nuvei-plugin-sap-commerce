package com.nuvei.backoffice.widgets.actions;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.payments.NuveiFilteringPaymentMethodsService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zul.Messagebox;

import javax.annotation.Resource;
import java.util.LinkedHashSet;

/**
 * Custom backoffice action that triggers the import of the Payment Methods of all the selected merchants
 */
public class NuveiSynchPaymentMethodsByMerchantsAction implements CockpitAction<LinkedHashSet<NuveiMerchantConfigurationModel>, String> {

    private static final Logger LOG = LogManager.getLogger(NuveiSynchPaymentMethodsByMerchantsAction.class);

    private static final String NOT_ABLE_TO_RETRIEVE_PAYMENT_METHODS = "Not able to retrieve payment methods: ";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_TITLE = "nuvei.actionResult.messageBox.failed.title";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_TITLE = "nuvei.actionResult.messageBox.success.title";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_MESSAGE = "nuvei.actionResult.messageBox.failed.message";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_MESSAGE = "nuvei.actionResult.messageBox.success.message";

    @Resource(name = "nuveiFilteringPaymentMethodsService")
    protected NuveiFilteringPaymentMethodsService nuveiFilteringPaymentMethodsService;

    /**
     * Imports of the Payment Methods of all the selected merchants
     *
     * @param actionContext The context of the action
     * @return Success if the import process has no errors. Errors otherwise.
     */
    @Override
    public ActionResult<String> perform(final ActionContext<LinkedHashSet<NuveiMerchantConfigurationModel>> actionContext) {
        try {
            final LinkedHashSet<NuveiMerchantConfigurationModel> merchants = actionContext.getData();
            for (NuveiMerchantConfigurationModel merchant : merchants) {
                nuveiFilteringPaymentMethodsService.synchFilteringPaymentMethodsForMerchant(merchant);
            }
            showMessage(actionContext);
            return new ActionResult<>(ActionResult.SUCCESS);
        } catch (final Exception e) {
            showErrorMessage(actionContext);
            LOG.error(NOT_ABLE_TO_RETRIEVE_PAYMENT_METHODS, e);
            return new ActionResult<>(ActionResult.ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canPerform(final ActionContext<LinkedHashSet<NuveiMerchantConfigurationModel>> ctx) {
        return CollectionUtils.isNotEmpty(ctx.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsConfirmation(final ActionContext<LinkedHashSet<NuveiMerchantConfigurationModel>> ctx) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage(final ActionContext<LinkedHashSet<NuveiMerchantConfigurationModel>> ctx) {
        return null;
    }

    /**
     * Displays a success information message
     *
     * @param actionContext The context of the action
     */
    protected void showMessage(final ActionContext<LinkedHashSet<NuveiMerchantConfigurationModel>> actionContext) {
        Messagebox.show(actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_MESSAGE),
                actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_TITLE), Messagebox.OK, Messagebox.INFORMATION);
    }

    /**
     * Displays a success information message
     *
     * @param actionContext The context of the action
     */
    protected void showErrorMessage(final ActionContext<LinkedHashSet<NuveiMerchantConfigurationModel>> actionContext) {
        Messagebox.show(actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_MESSAGE),
                actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_TITLE), Messagebox.OK, Messagebox.ERROR);
    }
}
