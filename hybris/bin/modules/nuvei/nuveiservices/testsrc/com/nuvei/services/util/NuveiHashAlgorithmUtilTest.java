package com.nuvei.services.util;

import com.nuvei.services.enums.NuveiHashAlgorithm;
import com.safecharge.util.Constants;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiHashAlgorithmUtilTest {

    @Test
    public void getNuveiHashAlgorithmName_WhenTheGivenAlghoritmIsSHA256_ShouldReturnSHA256() {
        final Constants.HashAlgorithm result = NuveiHashAlgorithmUtil.getNuveiHashAlgorithmName(NuveiHashAlgorithm.SHA256);

        assertThat(result).isEqualTo(Constants.HashAlgorithm.SHA256);
    }

    @Test
    public void getNuveiHashAlgorithmName_WhenTheGivenAlghoritmIsMD5_ShouldReturnMD5() {
        final Constants.HashAlgorithm result = NuveiHashAlgorithmUtil.getNuveiHashAlgorithmName(NuveiHashAlgorithm.MD5);

        assertThat(result).isEqualTo(Constants.HashAlgorithm.MD5);
    }

    @Test
    public void getNuveiHashAlgorithmName_WhenTheGivenAlghoritmIsNull_ShouldReturnNull() {
        final Constants.HashAlgorithm result = NuveiHashAlgorithmUtil.getNuveiHashAlgorithmName(null);

        assertThat(result).isNull();
    }
}
