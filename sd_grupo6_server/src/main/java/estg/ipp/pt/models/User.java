package estg.ipp.pt.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class User {
    private String nome;
    private String email;
    private String password;
    private String role;
    private List<String> lines;

    public User() {
    }

    public User(String nome, String email, String password) {
        this.nome = nome;
        this.email = email;
        this.password = password;
    }

    public User(String nome, String email, String password, List<String> lines) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.lines = lines;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public String toJsonString() {
        JsonArray linesJsonArray = new JsonArray();

        for (String userLine : lines) {
            linesJsonArray.add(userLine);
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Name", this.getNome());
        jsonObject.addProperty("Email", this.getEmail());
        jsonObject.addProperty("Role", this.getRole());
        jsonObject.add("Lines", linesJsonArray);

        return jsonObject.toString();
    }
}
