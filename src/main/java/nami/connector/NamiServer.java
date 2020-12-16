package nami.connector;

public class NamiServer {

    public static NamiServer getTestserver() {
        return new NamiServer("namitest.dpsg.de", false, "ica");
    }

    public static NamiServer getLiveserver() {
        return new NamiServer("nami.dpsg.de", true, "ica");
    }

    final private String namiServer;
    final private boolean useSsl;
    final private String namiDeploy;

    public NamiServer(String namiServer, boolean useSsl, String namiDeploy) {
        this.namiServer = namiServer;
        this.useSsl = useSsl;
        this.namiDeploy = namiDeploy;
    }

    public String getNamiServer() {
        return namiServer;
    }

    public boolean getUseSsl() {
        return useSsl;
    }

    public String getNamiDeploy() {
        return namiDeploy;
    }
}
