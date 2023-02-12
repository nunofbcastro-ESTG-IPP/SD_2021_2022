package estg.ipp.pt.models;

import com.google.gson.JsonObject;

import java.util.Objects;

public class Line {
    private String name;
    private Boolean isActive;
    private Integer port;
    private String host;

    public Line() {
    }

    public Line(String name, Boolean isActive, Integer port, String host) {
        this.name = name;
        this.isActive = isActive;
        this.port = port;
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Name", name);
        jsonObject.addProperty("Port", port);
        jsonObject.addProperty("IsActive", isActive);
        jsonObject.addProperty("Host", host);

        return jsonObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line that = (Line) o;
        return Objects.equals(name, that.name);
    }
}
