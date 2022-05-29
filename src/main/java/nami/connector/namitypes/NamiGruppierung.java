package nami.connector.namitypes;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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

    public String getGroupId() {
        // Die Gruppierungsnummer muss aus der Beschreibung ausgelesen werden,
        // da sie nicht zwangsweise mit der ID übereinstimmt. Bei den meisten
        // Gruppierungen stimmen sie überein, aber eben nicht bei allen. Das ist
        // halt eine Merkwürdigkeit in NaMi, für die wir hier einen Workaround
        // brauchen.
        Matcher match = GROUP_NUMBER_PATTERN.matcher(descriptor);
        if (!match.find())
            throw new IllegalArgumentException("Could not find Gruppierungsnummer in descriptior: " + descriptor);
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
        String groupId = getGroupId();
        NamiEbene thisE = NamiEbene.getFromGruppierungId(groupId);
        if (thisE.compareTo(targetE) < 0)
            return null;
        if (thisE.compareTo(targetE) == 0)
            return groupId;
        return fillZeroes(groupId.substring(0, targetE.getSignificantChars()), 6);
    }

    private static String fillZeroes(String number, int length) {
        StringBuilder result = new StringBuilder(number);
        while (result.length() < length)
            result.append("0");
        return result.toString();
    }

    public NamiEbene getEbene() {
        return NamiEbene.getFromGruppierungId(getGroupId());
    }

    public Optional<NamiGruppierung> findGruppierung(int groupNumber) {
        if (id == groupNumber)
            return Optional.of(this);
        return children.stream()
                .map(e -> e.findGruppierung(groupNumber))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
