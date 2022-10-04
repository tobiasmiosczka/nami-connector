package nami.connector;

import nami.connector.namitypes.NamiBaustein;
import nami.connector.namitypes.NamiSchulung;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
public class NamiUtil {

    /**
     * Returns the latest Trainings of each Module as a Map.
     * @param trainings collection of trainings to filter
     * @return The latest Trainings of each Module as a Map
     */
    public static Map<NamiBaustein, NamiSchulung> reduceToLatest(final Collection<NamiSchulung> trainings) {
        Map<NamiBaustein, NamiSchulung> result = new HashMap<>();
        for (NamiSchulung training : trainings) {
            NamiBaustein module = training.getBaustein();
            if (!result.containsKey(module) || result.get(module).getDate().isBefore(training.getDate())) {
                result.put(module, training);
            }
        }
        return result;
    }

}
