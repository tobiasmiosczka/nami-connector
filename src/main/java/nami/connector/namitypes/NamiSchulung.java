package nami.connector.namitypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class NamiSchulung {

    @JsonProperty("entries_vstgTag")
    private LocalDateTime date;
    @JsonProperty("entries_veranstalter")
    private String veranstalter;
    @JsonProperty("entries_vstgName")
    private String veranstaltungsname;
    @JsonProperty("entries_baustein")
    private NamiBaustein baustein;
    private Integer id;
    private String descriptor;
    @JsonProperty("entries_id")
    private Integer entriesId;
    private String representedClass;
    @JsonProperty("entries_mitglied")
    private String entriesMitglied;

    public NamiBaustein getBaustein() {
        return baustein;
    }

    public LocalDateTime getDate() {
        return date;
    }
}