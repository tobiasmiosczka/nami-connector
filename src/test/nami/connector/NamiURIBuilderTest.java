package nami.connector;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

class NamiURIBuilderTest {

    @Test
    public void test() throws UnsupportedEncodingException {
        NamiURIBuilder namiURIBuilder = new NamiURIBuilder(
                NamiServer.getLiveserver(),
                NamiURIBuilder.URL_NAMI_SEARCH,
                true);
        System.out.println(namiURIBuilder.build());
    }

}