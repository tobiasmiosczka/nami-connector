package nami.connector;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import nami.connector.exception.NamiException;
import nami.connector.httpclient.NamiHttpClient;
import nami.connector.httpclient.impl.NativeJava11NamiHttpClient;
import nami.connector.namitypes.*;
import nami.connector.uri.NamiUriFactory;

public class NamiConnector {

    private final NamiHttpClient httpClient;
    private final NamiServer server;
    private final NamiUriFactory uriFactory;

    private static final int INITIAL_LIMIT = 1000;

    public NamiConnector(NamiServer server) {
        this.server = server;
        this.httpClient = new NativeJava11NamiHttpClient();
        this.uriFactory = new NamiUriFactory(server);
    }

    public void login(String username, String password) throws NamiException {
        httpClient.login(server, username, password);
    }

    public CompletableFuture<List<NamiMitglied>> getAllResults(NamiSearchedValues searchedValues) {
        return getSearchResult(searchedValues, INITIAL_LIMIT, 1, 0);
    }

    public CompletableFuture<NamiMitglied> getMitgliedById(int id) {
        return this.httpClient.getSingle(uriFactory.namiMitglieder(id), NamiMitglied.class);
    }

    public CompletableFuture<List<NamiSchulung>> getTrainingsByUser(int userId) {
        return httpClient.getList(uriFactory.namiSchulungen(userId), NamiSchulung.class);
    }

    public CompletableFuture<Map<NamiBaustein, NamiSchulung>> getLatestTrainingsByUser(int userId) {
        return getTrainingsByUser(userId)
                .thenApply(NamiUtil::reduceToLatest);
    }

    public CompletableFuture<List<NamiEnum>> getTaetigkeiten() {
        return httpClient.getList(uriFactory.namiTaetigkeiten(), NamiEnum.class);
    }

    public CompletableFuture<List<NamiEnum>> getUntergliederungen() {
        return httpClient.getList(uriFactory.namiUntergliederungen(), NamiEnum.class);
    }

    public CompletableFuture<List<NamiMitglied>> getMitgliederFromGruppierung(int gruppierungsnummer) {
        return httpClient.getList(uriFactory.memberFromGroup(gruppierungsnummer), NamiMitglied.class);
    }

    public CompletableFuture<List<NamiTaetigkeitAssignment>> getTaetigkeiten(int id) {
        return httpClient.getList(uriFactory.namiTaetigkeiten(id), NamiTaetigkeitAssignment.class);
    }

    public CompletableFuture<NamiGruppierung> addChildGruppierungen(NamiGruppierung group) {
        if (group.getEbene() == NamiEbene.STAMM) {
            group.setChildren(new LinkedList<>());
            return CompletableFuture.completedFuture(group);
        }
        return httpClient.getList(uriFactory.childGroups(group.getId()), NamiGruppierung.class)
                .thenCompose(children -> allOf(children.stream().map(this::addChildGruppierungen).collect(Collectors.toList())))
                .thenApply(cc -> {
                    group.setChildren(cc);
                    return group;
                });
    }

    public CompletableFuture<NamiGruppierung> getRootGruppierung() {
        return getRootGruppierungWithoutChildren().thenCompose(this::addChildGruppierungen);
    }

    public CompletableFuture<List<NamiGruppierung>> getGruppierungenFromUser() {
        return getGruppierungenFromUser(-1);
    }

    public CompletableFuture<List<NamiGruppierung>> getGruppierungenFromUser(int id) {
        return httpClient.getList(uriFactory.groupsByUser(id), NamiGruppierung.class)
                .thenCompose(list -> allOf(list.stream().map(e -> getGruppierungenFromUser(e.getId())).toList())
                        .thenApply(lists -> lists.stream().flatMap(Collection::stream).toList()));
    }

    public CompletableFuture<Optional<NamiGruppierung>> getGruppierung(int groupNumber) throws NamiException {
        return getRootGruppierung()
                .thenApply(e -> e.findGruppierung(groupNumber));
    }

    public CompletableFuture<NamiTaetigkeitAssignment> getTaetigkeit(int personId, int taetigkeitId) {
        return httpClient.getSingle(uriFactory.taetigkeitByPersonIdAndTeatigkeitId(personId, taetigkeitId), NamiTaetigkeitAssignment.class);
    }

    private CompletableFuture<NamiGruppierung> getRootGruppierungWithoutChildren() {
        return httpClient.getList(uriFactory.rootGroupWithoutChildren(), NamiGruppierung.class)
                .thenApply(e -> {
                    NamiGruppierung rootGrp = e.get(0);
                    rootGrp.setChildren(null);
                    return rootGrp;
                });
    }

    private CompletableFuture<List<NamiMitglied>> getSearchResult(NamiSearchedValues searchedValues, int limit, int page, int start) {
        return httpClient.getList(uriFactory.namiSearch(limit, page, start, searchedValues), NamiMitglied.class);
    }

    public static <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
        return CompletableFuture
                .allOf(futuresList.toArray(new CompletableFuture[0]))
                .thenApply(v -> futuresList.stream().map(CompletableFuture::join).toList());
    }
}
