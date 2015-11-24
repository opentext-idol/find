package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.DispatcherServletConfiguration;
import com.hp.autonomy.frontend.find.core.search.FindDocument;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.api.textindex.query.search.Sort;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DispatcherServletConfiguration.class, AppConfiguration.class})
@TestPropertySource(properties = {"hp.find.persistentState = INMEMORY", "hp.find.home = ./target/test", "find.https.proxyHost = web-proxy.sdc.hpecorp.net", "find.https.proxyPort: 8080", "find.iod.api = https://api.havenondemand.com", "find.hod.sso = https://dev.havenondemand.com/sso.html"})
public class HodDocumentServiceIT {
    private static final String TEST_DIR = "./target/test";

    @BeforeClass
    public static void init() throws IOException {
        System.setProperty("hp.find.home", TEST_DIR);
        final File directory = new File(TEST_DIR);
        FileUtils.forceMkdir(directory);
        FileUtils.copyFileToDirectory(new File("./src/test/resources/config.json"), directory);
    }

    @AfterClass
    public static void destroy() throws IOException {
        FileUtils.forceDelete(new File(TEST_DIR));
    }

    @Autowired
    private DocumentsController documentsController;

    @Test
    public void query() throws HodErrorException {
        final QueryParams queryParams = createSampleQuery();
        final Documents<FindDocument> documents = documentsController.query(queryParams);
        assertThat(documents.getDocuments(), is(not(empty())));
    }

    @Test
    public void queryForPromotions() throws HodErrorException {
        final QueryParams queryParams = createSampleQuery();
        final Documents<FindDocument> documents = documentsController.queryForPromotions(queryParams);
        assertThat(documents.getDocuments(), is(empty())); // TODO: configure this later
    }

    @Test
    public void findSimilar() throws HodErrorException {
        final QueryParams queryParams = createSampleQuery();
        final Documents<FindDocument> documents = documentsController.query(queryParams);
        final List<FindDocument> results = documentsController.findSimilar(documents.getDocuments().get(0).getReference(), new HashSet<>(Arrays.asList(ResourceIdentifier.WIKI_ENG, ResourceIdentifier.NEWS_ENG)));
        assertThat(results, is(not(empty())));
    }

    private QueryParams createSampleQuery() {
        final QueryParams queryParams = new QueryParams();
        queryParams.setText("*");
        queryParams.setMaxResults(50);
        queryParams.setSort(Sort.relevance);
        queryParams.setIndex(Arrays.asList(ResourceIdentifier.WIKI_ENG, ResourceIdentifier.NEWS_ENG));
        return queryParams;
    }
}
