package nami.connector;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class CollectionUtil {

    public static <T, C extends Collection<T>> List<T> flatMap(Collection<C> lists) {
        return lists.stream().flatMap(Collection::stream).toList();
    }

    public static <T> List<T> merge(Collection<T> collection1, Collection<T> collection2) {
        return Stream.concat(collection1.stream(), collection2.stream()).toList();
    }

    public static <T> CompletableFuture<List<T>> sequence(Collection<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .collect(toList()));
    }
}
