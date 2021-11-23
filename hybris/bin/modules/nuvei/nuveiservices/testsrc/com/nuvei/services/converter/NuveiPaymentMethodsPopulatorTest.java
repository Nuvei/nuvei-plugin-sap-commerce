package com.nuvei.services.converter;

import com.nuvei.services.model.NuveiPaymentMethodModel;
import com.safecharge.model.LocalizationMessage;
import com.safecharge.model.PaymentMethod;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.core.model.c2l.LanguageModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiPaymentMethodsPopulatorTest {

    private static final String EN_ISO_CODE = "en";
    private static final String DE_ISO_CODE = "de";
    private static final String FR_ISO_CODE = "fr";
    private static final String EN_MESSAGE = "enMessage";
    private static final String DE_MESSAGE = "deMessage";
    private static final String FR_MESSAGE = "frMessage";

    @InjectMocks
    private NuveiPaymentMethodsPopulator testObj;

    @Mock
    protected CommerceCommonI18NService commerceCommonI18NServiceMock;

    @Mock
    private LanguageModel enLanguageMock, frLanguageMock;

    @Before
    public void setUp() {
        when(commerceCommonI18NServiceMock.getAllLanguages()).thenReturn(List.of(enLanguageMock, frLanguageMock));
        when(enLanguageMock.getIsocode()).thenReturn(EN_ISO_CODE);
        when(frLanguageMock.getIsocode()).thenReturn(DE_ISO_CODE);
    }

    @Test
    public void populate_ShouldPopulateAllFields() {
        final PaymentMethod source = new PaymentMethod();

        final LocalizationMessage enLocalizationMessage = new LocalizationMessage();
        enLocalizationMessage.setLanguage(EN_ISO_CODE);
        enLocalizationMessage.setMessage(EN_MESSAGE);

        final LocalizationMessage deLocalizationMessage = new LocalizationMessage();
        deLocalizationMessage.setLanguage(DE_ISO_CODE);
        deLocalizationMessage.setMessage(DE_MESSAGE);

        final LocalizationMessage frLocalizationMessage = new LocalizationMessage();
        frLocalizationMessage.setLanguage(FR_ISO_CODE);
        frLocalizationMessage.setMessage(FR_MESSAGE);

        source.setPaymentMethodDisplayName(List.of(enLocalizationMessage, deLocalizationMessage, frLocalizationMessage));

        final NuveiPaymentMethodModel target = new NuveiPaymentMethodModel();

        testObj.populate(source, target);

        assertThat(target.getDisplayName(Locale.ENGLISH)).isEqualTo(EN_MESSAGE);
        assertThat(target.getDisplayName(Locale.GERMAN)).isEqualTo(DE_MESSAGE);
        assertThat(target.getDisplayName(Locale.FRENCH)).isNull();
    }
}
