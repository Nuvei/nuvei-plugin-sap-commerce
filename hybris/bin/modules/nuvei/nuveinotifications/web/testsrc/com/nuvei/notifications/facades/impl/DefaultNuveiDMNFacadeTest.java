package com.nuvei.notifications.facades.impl;

import com.nuvei.notifications.data.NuveiIncomingDMNData;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiDMNFacadeTest {

    private static final String ID = "123456789012";
    @InjectMocks
    private DefaultNuveiDMNFacade testObj;

    @Mock
    private Converter<NuveiIncomingDMNData, NuveiDirectMerchantNotificationModel> nuveiIncomingDMNDataNuveiDirectMerchantNotificationModelConverterMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private KeyGenerator idGenerator;

    @Mock
    private NuveiIncomingDMNData nuveiIncomingDMNDataMock;
    @Mock
    private NuveiDirectMerchantNotificationModel nuveiDMNModelMock;

    @Test
    public void createAndSaveDMN_shouldReturnNuveiDMN() {
        when(nuveiIncomingDMNDataNuveiDirectMerchantNotificationModelConverterMock.convert(nuveiIncomingDMNDataMock)).thenReturn(nuveiDMNModelMock);
        when(idGenerator.generate()).thenReturn(ID);

        final NuveiDirectMerchantNotificationModel result = testObj.createAndSaveDMN(nuveiIncomingDMNDataMock);

        verify(nuveiIncomingDMNDataNuveiDirectMerchantNotificationModelConverterMock).convert(nuveiIncomingDMNDataMock);
        verify(idGenerator).generate();
        verify(modelServiceMock).save(result);
        assertThat(result).isEqualTo(nuveiDMNModelMock);
    }
}
