package estg.ipp.pt.sd_grupo6_client.models;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String nome;
    private String email;
    private String password;
    private String role;
    private List<String> lines;

    public User(String nome, String email, String password){
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.role = null;
        this.lines = new ArrayList<>();
    }
    public User(String nome, String email, String role, String password){
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.role = role;
        this.lines = new ArrayList<>();
    }
    public User(String nome, String email, String role, String password, List<String> linhes){
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.password = password;
        this.lines = linhes;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getLines() {
        return lines;
    }

    public void addLine(String linhe) {
        this.lines.add(linhe);
    }

    public void removeLine(String line) {
        this.lines.remove(line);
    }

    public static User StringJsonToUser(String passengerJson){
        Gson g = new Gson();
        JsonObject passengerJsonObject = g.fromJson(passengerJson, JsonObject.class);

        String nome = passengerJsonObject.get("Name").getAsString();
        String email = passengerJsonObject.get("Email").getAsString();
        String role = passengerJsonObject.get("Role").getAsString();

        User passenger = new User(nome, email, role, null);

        JsonArray lines = g.fromJson(passengerJsonObject.get("Lines"), JsonArray.class);

        for (int i = 0; i < lines.size(); i++){
            passenger.addLine(lines.get(i).getAsString());
        }

        return passenger;

    }
}
