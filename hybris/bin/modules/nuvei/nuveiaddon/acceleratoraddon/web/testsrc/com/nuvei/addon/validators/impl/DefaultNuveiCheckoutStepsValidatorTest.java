package com.nuvei.addon.validators.impl;

import com.nuvei.facades.beans.NuveiSDKResponseData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessage;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@RunWith(JUnitParamsRunner.class)
public class DefaultNuveiCheckoutStepsValidatorTest {

    private static final String ERROR = "ERROR";
    private static final String SUCCESS = "SUCCESS";
    private static final String DECLINED = "DECLINED";
    private static final String NUVEI_CHECKOUTSDK_ERROR_PROPERTY_KEY = "nuvei.checkout.error.checkoutsdk.result";
    private static final String APPROVED = "APPROVED";

    private static Object[] checkoutSDKErrorResponseResultConditions() {
        return new Object[]{
                new Object[]{ERROR},
                new Object[]{DECLINED},
                new Object[]{StringUtils.EMPTY}
        };
    }

    private static Object[] checkoutSDKOKResponseResultConditions() {
        return new Object[]{
                new Object[]{SUCCESS},
                new Object[]{APPROVED}
        };
    }

    @InjectMocks
    private DefaultNuveiCheckoutStepsValidator testObj;

    private final RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
    private final NuveiSDKResponseData nuveiSDKResponseData = new NuveiSDKResponseData();

    @Before
    public void setUp() {
        testObj = new DefaultNuveiCheckoutStepsValidator();
        MockitoAnnotations.initMocks(testObj);
    }

    @Test
    @Parameters(method = "checkoutSDKErrorResponseResultConditions")
    public void validateCheckoutSDKResponse_ShouldReturnTrueAndAddErrorMessage_WhenValidationIsNotSuccess(final String responseResult) {
        nuveiSDKResponseData.setResult(responseResult);

        final boolean result = testObj.validateCheckoutSDKResponse(redirectAttributes, nuveiSDKResponseData);
        final boolean hasErrorMessage = ((List<GlobalMessage>) redirectAttributes
                .getFlashAttributes()
                .get(GlobalMessages.ERROR_MESSAGES_HOLDER)).stream()
                .map(GlobalMessage::getCode)
                .anyMatch(NUVEI_CHECKOUTSDK_ERROR_PROPERTY_KEY::equals);

        assertThat(result).isTrue();
        assertThat(hasErrorMessage).isTrue();
    }

    @Test
    @Parameters(method = "checkoutSDKOKResponseResultConditions")
    public void validateCheckoutSDKResponse_ShouldReturnFalse_WhenValidationIsSuccess(final String responseResult) {
        nuveiSDKResponseData.setResult(responseResult);

        final boolean result = testObj.validateCheckoutSDKResponse(redirectAttributes, nuveiSDKResponseData);
        final boolean hasErrorMessages = redirectAttributes
                .getFlashAttributes()
                .containsKey(GlobalMessages.ERROR_MESSAGES_HOLDER);

        assertThat(result).isFalse();
        assertThat(hasErrorMessages).isFalse();
    }
}
