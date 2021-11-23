package com.nuvei.services.handlers;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.exceptions.AttributeNotSupportedException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class SerializableToJsonAttributeHandlerTest {
    private static final String ATTRIBUTE_NAME = "attributeName";
    private static final String SERIALIZABLE_STRINGIFIED = "{\"id\": \"0001\"}";
    private static final String ANY_ATTRIBUTE = "whatever";

    private SerializableToJsonAttributeHandler testObj;

    @Mock
    private ItemModel itemModelMock;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private Object nonSerializableObjectMock;

    private SerializableObject serializableObject = new SerializableObject();

    @Before
    public void setUp() {
        testObj = spy(new SerializableToJsonAttributeHandler(ATTRIBUTE_NAME, modelServiceMock));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowAnExceptionWhenSetterIsCalled() {
        testObj.set(itemModelMock, ANY_ATTRIBUTE);
    }

    @Test(expected = AttributeNotSupportedException.class)
    public void shouldThrowExceptionWhenAttributeDoesNotExistsOnItem() {
        when(modelServiceMock.getAttributeValue(itemModelMock, ATTRIBUTE_NAME))
                .thenThrow(new AttributeNotSupportedException("Attribute Not supported", ATTRIBUTE_NAME));

        testObj.get(itemModelMock);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowAnExceptionIfAttributeObjectDoesNotImplementsSerializable() {
        when(modelServiceMock.getAttributeValue(itemModelMock, ATTRIBUTE_NAME)).thenReturn(nonSerializableObjectMock);

        testObj.get(itemModelMock);
    }

    @Test
    public void shouldReturnAnEmptyStringWhenAttributeIsEmptyOrNull() {
        when(modelServiceMock.getAttributeValue(itemModelMock, ATTRIBUTE_NAME)).thenReturn(null);

        final String result = testObj.get(itemModelMock);

        assertThat(result).isNullOrEmpty();
    }

    @Test
    public void shouldCallStringifyWhenObjectIsSerializableAndReturnStringifiedVersionOfTheObject() {
        when(modelServiceMock.getAttributeValue(itemModelMock, ATTRIBUTE_NAME)).thenReturn(serializableObject);
        when(testObj.stringify(serializableObject)).thenReturn(SERIALIZABLE_STRINGIFIED);

        final String result = testObj.get(itemModelMock);

        assertThat(result).isEqualTo(SERIALIZABLE_STRINGIFIED);
    }

    public static class SerializableObject implements Serializable {
    }
}
