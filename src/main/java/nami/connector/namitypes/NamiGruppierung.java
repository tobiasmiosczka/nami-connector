package nami.connector.namitypes;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamiGruppierung {

    private String descriptor;
    private int id;

    private Collection<NamiGruppierung> children;

    private static final Pattern GROUP_NUMBER_PATTERN = Pattern.compile("[\\d]+");

    public String getDescriptor() {
        return descriptor;
    }

    public int getId() {
        return id;
    }

    public String getGruppierungsnummer() {
        // Die Gruppierungsnummer muss aus der Beschreibung ausgelesen werden,
        // da sie nicht zwangsweise mit der ID übereinstimmt. Bei den meisten
        // Gruppierungen stimmen sie überein, aber eben nicht bei allen. Das ist
        // halt eine Merkwürdigkeit in NaMi, für die wir hier einen Workaround
        // brauchen.
        Matcher match = GROUP_NUMBER_PATTERN.matcher(descriptor);
        if (!match.find()) {
            throw new IllegalArgumentException("Could not find Gruppierungsnummer in descriptior: " + descriptor);
        }
        return match.group();
    }

    public Collection<NamiGruppierung> getChildren() {
        return children;
    }

    public void setChildren(Collection<NamiGruppierung> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return descriptor;
    }

    public String getParentId(NamiEbene targetE) {
        // Gruppierungsnummer dieser Gruppierung
        String grpNum = getGruppierungsnummer();
        NamiEbene thisE = NamiEbene.getFromGruppierungId(grpNum);
        if (thisE.compareTo(targetE) < 0) {
            // Es wird eine niedrigere Ebene verlangt
            return null;
        } else if (thisE.compareTo(targetE) == 0) {
            // Es wird die gleiche Ebene verlangt
            return grpNum;
        } else {
            // Es wird eine höhere Ebene verlangt
            StringBuilder result = new StringBuilder(grpNum.substring(0, targetE.getSignificantChars()));
            // Fülle die GruppierungsID rechts mit Nullen auf 6 Stellen auf
            while (result.length() < 6) {
                result.append("0");
            }
            return result.toString();
        }
    }

    public NamiEbene getEbene() {
        return NamiEbene.getFromGruppierungId(id);
    }

    public NamiGruppierung findGruppierung(int gruppierungsnummer) {
        if (id == gruppierungsnummer) {
            return this;
        } else {
            for (NamiGruppierung grp : children) {
                NamiGruppierung res = grp.findGruppierung(gruppierungsnummer);
                if (res != null) {
                    return res;
                }
            }
            return null;
        }
    }
}
