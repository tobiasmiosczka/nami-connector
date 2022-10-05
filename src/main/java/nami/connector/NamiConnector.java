package nami.connector;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import nami.connector.exception.NamiException;
import nami.connector.httpclient.NamiHttpClient;
import nami.connector.httpclient.impl.NativeJava11NamiHttpClient;
import nami.connector.namitypes.*;
import nami.connector.uri.NamiUriFactory;

import static java.util.stream.Collectors.toList;

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

    private CompletableFuture<NamiGruppierung> addChildGruppierungen(NamiGruppierung group) {
        if (group.getEbene() == NamiEbene.STAMM) {
            group.setChildren(new LinkedList<>());
            return CompletableFuture.completedFuture(group);
        }
        return httpClient.getList(uriFactory.childGroups(group.getId()), NamiGruppierung.class)
                .thenCompose(children -> sequence(children.stream().map(this::addChildGruppierungen).collect(toList())))
                .thenApply(cc -> {
                    group.setChildren(cc);
                    return group;
                });
    }

    public CompletableFuture<NamiGruppierung> getRootGruppierung() {
        return getRootGruppierungWithoutChildren().thenCompose(this::addChildGruppierungen);
    }

    public CompletableFuture<List<NamiGruppierung>> getAccessibleGroups() {
        return getAccessibleGroupsRecursively(-1);
    }

    private CompletableFuture<List<NamiGruppierung>> getAccessibleGroupsRecursively(int groupId) {
        return httpClient.getList(uriFactory.groupsByUser(groupId), NamiGruppierung.class)
                .thenCompose(list -> sequence(list.stream().map(e -> getAccessibleGroupsRecursively(e.getId())).toList())
                        .thenApply(lists -> lists.stream().flatMap(Collection::stream).toList())
                        .thenApply(l -> Stream.concat(list.stream(), l.stream()).toList()));
    }

    public CompletableFuture<Optional<NamiGruppierung>> getGruppierung(int groupNumber) {
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

    static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .collect(toList()));
    }
}
