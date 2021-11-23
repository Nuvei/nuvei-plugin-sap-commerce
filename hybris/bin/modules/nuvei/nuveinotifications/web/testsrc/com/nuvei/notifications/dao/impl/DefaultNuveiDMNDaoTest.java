package com.nuvei.notifications.dao.impl;

import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.nuvei.notifications.dao.impl.DefaultNuveiDMNDao.NUVEI_FIND_OLD_DMN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiDMNDaoTest {

    private static final String NUVEI_CLEAN_JOB_DAYS = "nuvei.clean.notifications.job.days";
    public static final int MAX_TIME_DAYS = 15;

    @InjectMocks
    private DefaultNuveiDMNDao testObj;

    @Mock
    private FlexibleSearchService flexibleSearchServiceMock;
    @Mock
    private SearchResult searchResultMock;
    @Mock
    private ConfigurationService configurationServiceMock;
    @Mock
    private Configuration configurationMock;

    private final NuveiDirectMerchantNotificationModel nuveiDirectMerchantNotificationModel = new NuveiDirectMerchantNotificationModel();

    @Captor
    private ArgumentCaptor<FlexibleSearchQuery> queryArgumentCaptor;

    @Before
    public void setUp() {
        final List<NuveiDirectMerchantNotificationModel> resultMock = List.of(nuveiDirectMerchantNotificationModel);
        when(searchResultMock.getResult()).thenReturn(resultMock);
        when(flexibleSearchServiceMock.search(queryArgumentCaptor.capture())).thenReturn(searchResultMock);
        when(configurationServiceMock.getConfiguration()).thenReturn(configurationMock);
        when(configurationMock.getInt(NUVEI_CLEAN_JOB_DAYS)).thenReturn(MAX_TIME_DAYS);
        this.testObj.setFlexibleSearchService(flexibleSearchServiceMock);
    }

    @Test
    public void findDMNToDelete_ShouldReturnTransactionsToBeDeleted_WhenTransactionsAreOldAndProcessed() {
        final List<NuveiDirectMerchantNotificationModel> result = testObj.findDMNToDelete();

        verify(flexibleSearchServiceMock).search(queryArgumentCaptor.capture());
        final FlexibleSearchQuery queryArgumentCaptorValue = queryArgumentCaptor.getValue();

        assertThat(result).isEqualTo(List.of(nuveiDirectMerchantNotificationModel));
        assertThat(queryArgumentCaptorValue.getQuery()).isEqualTo(NUVEI_FIND_OLD_DMN);
    }
}
