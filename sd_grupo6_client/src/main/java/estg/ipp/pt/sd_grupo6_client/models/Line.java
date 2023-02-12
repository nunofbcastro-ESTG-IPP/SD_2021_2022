package estg.ipp.pt.sd_grupo6_client.models;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import estg.ipp.pt.sd_grupo6_client.Utils.Convert;

import java.util.ArrayList;
import java.util.Objects;

public class Line {
    private String name;
    private Integer port;
    private boolean isActive;
    private String host;
    private Boolean isSubscribed;


    public Line(String name, Integer port, boolean isActive, String host) {
        this.name = name;
        this.port = port;
        this.isActive = isActive;
        this.host = host;
        this.isSubscribed = false;
    }

    public Line(String name, Integer port, boolean isActive, String host, Boolean isSubscribed) {
        this.name = name;
        this.port = port;
        this.isActive = isActive;
        this.host = host;
        this.isSubscribed = isSubscribed;
    }

    public String getNome() {
        return name;
    }

    public void setNome(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean getStatus() {
        return isActive;
    }

    public void setStatus(boolean isActive) {
        this.isActive = isActive;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Boolean getSubscribed() {
        return isSubscribed;
    }

    public void setSubscribed(Boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    public static ArrayList<Line> StringJsonToLines(JsonArray linesJson) {
        Gson g = new Gson();

        ArrayList<Line> lines = new ArrayList<>();

        for (int i = 0; i < linesJson.size(); i++) {
            JsonObject lineJsonObject = g.fromJson(linesJson.get(i), JsonObject.class);
            String name = lineJsonObject.get("Name").getAsString();
            boolean isActive = lineJsonObject.get("IsActive").getAsBoolean();

            Integer port;
            if (!lineJsonObject.get("Port").isJsonNull()) {
                port = Convert.StringToInt(lineJsonObject.get("Port").getAsString(), null);
            }else{
                port = null;
            }

            String host;
            if (!lineJsonObject.get("Host").isJsonNull()) {
                host = lineJsonObject.get("Host").getAsString();
            }else{
                host = null;
            }

            lines.add(new Line(name, port, isActive, host));
        }

        return lines;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line that = (Line) o;
        return Objects.equals(name, that.name);
    }
}
