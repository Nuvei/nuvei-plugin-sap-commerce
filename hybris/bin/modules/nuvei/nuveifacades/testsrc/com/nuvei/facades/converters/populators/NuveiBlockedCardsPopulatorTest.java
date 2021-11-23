package com.nuvei.facades.converters.populators;

import com.nuvei.facades.beans.NuveiBlockedCardsData;
import com.nuvei.services.enums.NuveiBrand;
import com.nuvei.services.enums.NuveiCardProduct;
import com.nuvei.services.enums.NuveiCardType;
import com.nuvei.services.model.NuveiBlockedCardsModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CountryModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiBlockedCardsPopulatorTest {

    private static final String COUNTRY_ISO_CODE = "countryIsoCode";

    final NuveiBlockedCardsModel source = new NuveiBlockedCardsModel();
    final NuveiBlockedCardsData target = new NuveiBlockedCardsData();

    @InjectMocks
    private NuveiBlockedCardsPopulator testObj;

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceIsNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetIsNull_ShouldThrowException() {
        testObj.populate(source, null);
    }

    @Test
    public void populate_ShouldPopulateBlockedCards() {
        final CountryModel countryModel = new CountryModel();

        countryModel.setIsocode(COUNTRY_ISO_CODE);
        source.setBrand(List.of(NuveiBrand.DANKORT));
        source.setCardProduct(List.of(NuveiCardProduct.CREDIT));
        source.setCardType(List.of(NuveiCardType.CONSUMER));
        source.setCountry(List.of(countryModel));

        testObj.populate(source, target);

        assertThat(target.getBrands()).isEqualTo(List.of(NuveiBrand.DANKORT.getCode()));
        assertThat(target.getCardProducts()).isEqualTo(List.of(NuveiCardProduct.CREDIT.getCode()));
        assertThat(target.getCardTypes()).isEqualTo(List.of(NuveiCardType.CONSUMER.getCode()));
        assertThat(target.getCountries()).isEqualTo(List.of(COUNTRY_ISO_CODE));
    }
}
