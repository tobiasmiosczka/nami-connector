package nami.connector.namitypes;

import java.time.LocalDate;

public class NamiHalbjahr implements Comparable<NamiHalbjahr> {

    private final int halbjahr;
    private final int jahr;


    public NamiHalbjahr(int halbjahr, int jahr) {
        this.halbjahr = halbjahr;
        this.jahr = jahr;
    }

    public NamiHalbjahr(LocalDate date) {
        if (date.getMonth().getValue() <= 5)
            halbjahr = 1;
         else
            halbjahr = 2;
        jahr = date.getYear();
    }

    @Override
    public int compareTo(NamiHalbjahr o) {
        if (this.jahr > o.jahr) {
            return 1;
        }
        if (this.jahr < o.jahr) {
            return -1;
        }
        return Integer.compare(this.halbjahr, o.halbjahr);
    }

    @Override
    public String toString() {
        return halbjahr + "/" + jahr;
    }
}
