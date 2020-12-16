package nami.connector;

import org.junit.jupiter.api.Test;

class NamiURIBuilderTest {

    @Test
    public void test() {
        NamiURIBuilder namiURIBuilder = new NamiURIBuilder(
                NamiServer.getLiveserver(),
                NamiURIBuilder.URL_NAMI_SEARCH,
                true);
        System.out.println(namiURIBuilder.build());
    }

}