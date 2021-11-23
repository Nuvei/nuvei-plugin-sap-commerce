package com.nuvei.services.order.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiCartFactoryTest {

    public static final String CART_CODE = "000000001";
    @Spy
    @InjectMocks
    private NuveiCartFactory testObj;

    @Mock
    private ModelService modelService;

    @Test
    public void createCart_shouldSetClientUniqueIdWithCartCodeAndSaveCartModelWhenCartIsCreated() {
        final CartModel cartStub = new CartModel();
        cartStub.setCode(CART_CODE);
        doReturn(cartStub).when(testObj).superCreateCart();

        final CartModel result = testObj.createCart();

        verify(testObj).superCreateCart();
        verify(modelService).save(result);
        assertThat(result.getClientUniqueId()).isEqualTo(CART_CODE);
    }
}
