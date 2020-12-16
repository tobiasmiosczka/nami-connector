package nami.connector.namitypes;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class NamiSchulung {

    @SerializedName("entries_vstgTag")
    private LocalDateTime date;
    @SerializedName("entries_veranstalter")
    private String veranstalter;
    @SerializedName("entries_vstgName")
    private String veranstaltungsname;
    @SerializedName("entries_baustein")
    private NamiBaustein baustein;
    private Integer id;
    private String descriptor;
    @SerializedName("entries_id")
    private Integer entriesId;
    private String representedClass;
    @SerializedName("entries_mitglied")
    private String entriesMitglied;

    public NamiBaustein getBaustein() {
        return baustein;
    }

    public LocalDateTime getDate() {
        return date;
    }
}