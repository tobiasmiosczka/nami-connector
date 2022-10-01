package nami.connector;

import nami.connector.namitypes.NamiBaustein;
import nami.connector.namitypes.NamiSchulung;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NamiUtil {

    public static Map<NamiBaustein, NamiSchulung> reduceToLatest(final Collection<NamiSchulung> trainings) {
        return trainings.stream().collect(Collectors.toMap(NamiSchulung::getBaustein, Function.identity()));
    }

}
