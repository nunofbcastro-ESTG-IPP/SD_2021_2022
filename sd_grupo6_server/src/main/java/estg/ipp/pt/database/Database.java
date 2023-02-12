package estg.ipp.pt.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertOneResult;
import estg.ipp.pt.models.Line;
import estg.ipp.pt.models.Report;
import estg.ipp.pt.models.Notification;
import estg.ipp.pt.env.MyEnv;
import estg.ipp.pt.models.User;
import estg.ipp.pt.utlis.Password;
import estg.ipp.pt.utlis.SynchronizedArrayList;
import estg.ipp.pt.utlis.Validations;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {
    private static final String mongo_uri = MyEnv.get("MONGO_URI");
    private static final String database_name = MyEnv.get("DATABASE_NAME");
    private SynchronizedArrayList<Notification> notificationsList;


    static {
        final Logger LOGGER = LoggerFactory.getLogger(Database.class);
        LOGGER.debug("Connected to MongoDB");
        LOGGER.error("Logging an Error");
    }

    public Database() {
    }

    public Database(SynchronizedArrayList<Notification> notificationsList) {
        this.notificationsList = notificationsList;
    }

    private MongoClient getMongoClient() {
        ConnectionString connectionString = new ConnectionString(mongo_uri);

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(pojoCodecRegistry)
                .build();

        return MongoClients.create(clientSettings);
    }

    private MongoDatabase getMongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase(database_name);
    }

    /**
     * Inserir uma linha na base de dados para poder ser utilizada no sistema. A linha fica inativa, o host e a porta são nulos
     *
     * @param lineName nome da linha a ser adicionada
     * @return true caso a linha seja inserida com sucesso, caso contrário, false
     */
    public boolean insertLines(String lineName) {
        if (!Validations.lineNameValidation(lineName)) {
            return false;
        }

        boolean isSuccessful;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.LinesCollection);
        collection.createIndex(Indexes.ascending("name"), new IndexOptions().unique(true));

        try {
            collection.insertOne(
                    new Document()
                            .append("_id", new ObjectId())
                            .append("name", lineName)
                            .append("isActive", false)
                            .append("port", null)
                            .append("host", null)
            );

            isSuccessful = true;
        } catch (MongoException e) {
            isSuccessful = false;
        }

        mongoClient.close();

        return isSuccessful;
    }

    /**
     * Alterar os estados de todas as linhas do sistema: true linha ativo, false linha inativa
     *
     * @param status novo estado das linhas
     * @return true caso os estados sejam alterados com sucesso, caso contrário, false
     */
    public boolean changeAllLineStatus(Boolean status) {
        boolean isSuccessful;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.LinesCollection);
        try {
            Bson update = Updates.set("isActive", status);
            collection.updateMany(new Document(), update);

            isSuccessful = true;
        } catch (MongoException me) {
            isSuccessful = false;
        }

        return isSuccessful;
    }

    /**
     * Obter todas as linhas que o sistema possui
     *
     * @return Uma lista que com as linhas do sistema
     */
    public List<Line> getLinesList() {
        List<Line> lines = new ArrayList<>();

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Line> collection = database.getCollection(DatabaseCollectionsNames.LinesCollection, Line.class);

        try {
            for (Line line : collection.find()) {
                lines.add(line);
            }
        } catch (Exception ignored) {

        }

        mongoClient.close();
        return lines;
    }

    /**
     * Obter todos os utilizadores do sistema
     *
     * @return Uma lista com os utilizadores do sistema
     */
    public List<User> getUsersList() {
        List<User> users = new ArrayList<>();

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<User> collection = database.getCollection(DatabaseCollectionsNames.UsersCollection, User.class);

        try {
            for (User user : collection.find()) {
                users.add(user);
            }
        } catch (Exception ignored) {
        }

        mongoClient.close();
        return users;
    }

    /**
     * Alterar o cargo do utilizador: Passenger ou Manager
     *
     * @param email     email utilizado para identificar o utilizador
     * @param isManager novo cargo do utilizador
     * @return true caso o cargo seja alterado com sucesso, caso contrário, false
     */
    public boolean changeRoleUser(String email, boolean isManager) {
        boolean isSuccessful;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.UsersCollection);
        try {
            Bson filter = Filters.eq("email", email);
            List<Bson> update = new ArrayList<>();
            update.add(Updates.set("lines", new ArrayList<>()));
            if (isManager) {
                update.add(Updates.set("role", "Manager"));
            } else {
                update.add(Updates.set("role", "Passenger"));
            }

            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.AFTER);
            Document result = collection.findOneAndUpdate(filter, update, options);

            isSuccessful = true;
        } catch (MongoException me) {
            isSuccessful = false;
        }

        mongoClient.close();

        return isSuccessful;
    }

    /**
     * Alterar o estado de uma linha: true linha ativo, false linha inativa
     *
     * @param lineName nome da linha a ser alterada
     * @param status   novo estado da linha
     * @return String com os dados da linha ou null em caso de erro
     */
    public String changeLineStatus(String lineName, Boolean status) {
        String resutl;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.LinesCollection);
        try {
            Bson filter = Filters.eq("name", lineName);
            Bson update = Updates.set("isActive", status);

            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.AFTER);
            Document result = collection.findOneAndUpdate(filter, update, options);

            System.out.println("Sucesso! Documento alterado: " + result.toJson());

            resutl = result.toJson();
        } catch (MongoException me) {
            resutl = null;
            System.err.println("Não foi possível atualizar a linha devido a um erro: " + me);
        }
        mongoClient.close();

        return resutl;
    }

    /**
     * Alterar a linha do gestor local
     *
     * @param email    email do gestor local a ser alterado
     * @param lineName nome da linha a ser inserida
     * @return true caso a linha do gestor seja alterada com sucesso, caso contrário, false
     */
    public boolean changeManagerLine(String email, String lineName) {
        boolean isSuccessful;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.UsersCollection);
        try {
            long count = collection.countDocuments(and(eq("role", "Manager"), in("lines", lineName)));
            if (count != 0) {
                throw new Exception("A linha já possui um gestor local.");
            }

            Bson filter = Filters.eq("email", email);
            List<String> lines = new ArrayList<>();
            lines.add(lineName);
            Bson update = Updates.set("lines", lines);

            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.AFTER);
            Document result = collection.findOneAndUpdate(filter, update, options);

            isSuccessful = true;
        } catch (Exception me) {
            isSuccessful = false;
        }

        mongoClient.close();

        return isSuccessful;
    }

    /**
     * Remover as linhas de um utilizador
     *
     * @param email email do utilizador a ser alterado
     * @return true caso as linhas sejam removidas com sucesso, caso contrário, false
     */
    public boolean removeUserLines(String email) {
        boolean isSuccessful;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.UsersCollection);
        try {
            Bson filter = Filters.eq("email", email);
            List<String> lines = new ArrayList<>();
            Bson update = Updates.set("lines", lines);

            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
            Document result = collection.findOneAndUpdate(filter, update, options);

            isSuccessful = true;
        } catch (MongoException me) {
            isSuccessful = false;
        }

        mongoClient.close();

        return isSuccessful;
    }

    /**
     * Obter um utilizador
     *
     * @param email email que identifica o utilizador
     * @return Utilizador com o email enviado por parâmetro ou null em caso de erro
     */
    public User getUser(String email) {
        User passenger;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<User> collection = database.getCollection(DatabaseCollectionsNames.UsersCollection, User.class);
        try {
            Document filterDoc = new Document();
            filterDoc.put("email", email.toLowerCase());
            passenger = collection.find(filterDoc).first();

            if (passenger == null) {
                throw new Exception("Não foi possível encontrar utilizador");
            }
        } catch (Exception ex) {
            System.err.println(ex);
            passenger = null;
        }

        mongoClient.close();

        return passenger;
    }

    /**
     * Efetuar login de um utilizador
     *
     * @param email    email que identifica o utilizador
     * @param password palavra-passe do utilizador
     * @return Um utilizador caso seja encontrado, caso contrário, null
     */
    public String loginUser(String email, String password) {
        try {
            if (!Validations.emailValidation(email)) {
                throw new Exception("Email inválido");
            }

            if (!Validations.passwordValidation(password)) {
                throw new Exception("Password Inválida");
            }

            User passenger = getUser(email);

            if (!Password.VerifyPassword(passenger.getPassword(), password)) {
                throw new Exception("Password Inválida");
            }

            return passenger.toJsonString();
        } catch (Exception ex) {
            System.err.println(ex);
            return null;
        }
    }

    /**
     * Efetuar o registo de um utilizador
     *
     * @param user Utilizador a ser registado
     * @return Um utilizador caso seja registado, caso contrário, null
     */
    public String registerUser(User user) {
        String resultString;
        MongoClient mongoClient = null;
        try {
            if (!Validations.nameValidation(user.getNome())) {
                throw new Exception("Nome inválido");
            }

            if (!Validations.emailValidation(user.getEmail())) {
                throw new Exception("Email inválido");
            }

            if (!Validations.passwordValidation(user.getPassword())) {
                throw new Exception("Password inválida");
            }

            mongoClient = getMongoClient();
            MongoDatabase database = getMongoDatabase(mongoClient);

            MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.UsersCollection);

            InsertOneResult result = collection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("nome", user.getNome())
                    .append("email", user.getEmail().toLowerCase())
                    .append("password", Password.HashPassword(user.getPassword()))
                    .append("role", "Passenger")
                    .append("lines", user.getLines()));

            System.out.println("Sucesso! Id do documento inserido: " + result.getInsertedId());

            resultString = getUser(user.getEmail()).toJsonString();
        } catch (Exception me) {
            System.err.println("Não foi possível inserir o utilizador devido a um erro: " + me);
            resultString = null;
        }

        if (mongoClient != null) {
            mongoClient.close();
        }

        return resultString;
    }

    /**
     * Inserir notificação de um utilizador
     *
     * @param report Notificação a ser inserida
     * @return true caso a notificação seja inserida com sucesso, caso contrário, false
     */
    public Boolean insertNotification(Notification report) {
        boolean isSuccessful;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.ReportsLineCollection);
        try {
            InsertOneResult result = collection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("notificationDate", report.getNotificationDate())
                    .append("userEmail", report.getUserEmail())
                    .append("message", report.getMessage())
                    .append("line", report.getLine()));

            System.out.println("Sucesso! Id do documento inserido: " + result.getInsertedId());
            notificationsList.list();
            notificationsList.add(report);

            isSuccessful = true;
        } catch (MongoException me) {
            System.err.println("Não foi possível inserir devido a um erro: " + me);
            isSuccessful = false;
        }

        mongoClient.close();

        return isSuccessful;
    }

    /**
     * Obter histórico de notificações de uma linha
     *
     * @param linhaName nome da linha a ser pesquisada
     * @return Uma lista de notificações em String ou null em caso de erro
     */
    public String getNotificationsFromLine(String linhaName) {
        String result;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Notification> collection = database.getCollection(DatabaseCollectionsNames.ReportsLineCollection, Notification.class);
        try {
            Document filterDoc = new Document();

            filterDoc.put("line", linhaName);

            Iterable<Notification> notifications = collection.find(filterDoc);

            JsonArray notificationsArray = new JsonArray();
            for (Notification notification : notifications) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("NotificationDate", notification.getNotificationDate());
                jsonObject.addProperty("UserEmail", notification.getUserEmail());
                jsonObject.addProperty("Message", notification.getMessage());
                jsonObject.addProperty("Line", notification.getLine());

                notificationsArray.add(jsonObject);
            }

            result = notificationsArray.toString();
        } catch (Exception ex) {
            System.err.println(ex);
            result = null;
        }

        mongoClient.close();

        return result;
    }

    /**
     * Obter uma linha através do nome
     *
     * @param lineName nome da linha a ser pesquisada
     * @return String com os dados da linha ou null em caso de erro
     */
    public String getLine(String lineName) {
        String result;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);
        MongoCollection<Line> collection = database.getCollection(DatabaseCollectionsNames.LinesCollection, Line.class);
        //Criar index unique para o nome
        collection.createIndex(Indexes.ascending("name"), new IndexOptions().unique(true));
        try {
            Document filterDoc = new Document();
            filterDoc.put("name", lineName);
            Line line = collection.find(filterDoc).first();

            if (line == null) {
                throw new Exception("Não existe nenhuma linha com o nome " + lineName);
            }

            System.out.println("Sucesso! Linha encontrada: " + line.toJsonObject());

            result = line.toJsonObject().toString();
        } catch (Exception ex) {
            System.err.println(ex);
            result = null;
        }

        mongoClient.close();

        return result;
    }

    /**
     * Obter todas as linhas do sistema
     *
     * @return Uma lista de linhas em String ou null em caso de erro
     */
    public String getLines() {
        String result;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Line> collection = database.getCollection(DatabaseCollectionsNames.LinesCollection, Line.class);
        //Criar index unique para o nome
        collection.createIndex(Indexes.ascending("name"), new IndexOptions().unique(true));
        try {
            JsonArray linhas = new JsonArray();

            FindIterable<Line> lines = collection.find();
            for (Line line : lines) {
                linhas.add(line.toJsonObject());
            }

            if (linhas.isEmpty()) {
                throw new Exception("Não foi possível encontrar linhas");
            }

            result = linhas.toString();
        } catch (Exception ex) {
            System.err.println(ex);
            result = null;
        }

        mongoClient.close();

        return result;
    }

    /**
     * Alterar o host e port de uma determinada linha
     *
     * @param email email do gestor local para obter a sua linha
     * @param host  novo host da linha
     * @param port  novo port da linha
     * @return Uma linha caso seja alterada, caso contrário, null
     */
    public String changeLineHostPort(String email, String host, Integer port) {
        if (host == null || port == null || port < 0 || port > 65535) {
            return null;
        }

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.LinesCollection);
        try {
            User user = getUser(email);
            if (user.getRole().equals("Manager") && user.getLines().size() == 1) {
                Bson filter = Filters.eq("name", user.getLines().get(0));
                List<Bson> update = new ArrayList<>();
                update.add(Updates.set("port", port));
                update.add(Updates.set("host", host));

                FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
                Document result = collection.findOneAndUpdate(filter, update, options);

                return result.toJson();
            }
        } catch (MongoException me) {
            System.err.println("Não foi possível atualizar a linha devido a um erro: " + me);
        }

        mongoClient.close();

        return null;
    }

    /**
     * Inserir uma linha a um determinado passageiro
     *
     * @param email    email que identifica o passageiro
     * @param lineName nome da linha a ser adicionada
     * @return Um utilizador caso seja alterado, caso contrário, null
     */
    public String insertPassengerLine(String email, String lineName) {
        String resultString;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.UsersCollection);
        try {
            Bson filter = Filters.eq("email", email);
            Bson update = Updates.push("lines", lineName);

            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.AFTER);
            Document result = collection.findOneAndUpdate(filter, update, options);

            System.out.println("Sucesso! Linha adicionada ao utilizador: " + result.toJson());

            resultString = result.toJson();
        } catch (MongoException me) {
            System.err.println("Não foi possível adicionar a linha devido a um erro: " + me);
            resultString = null;
        }

        mongoClient.close();

        return resultString;
    }

    /**
     * Remover uma linha de um determinado passageiro
     *
     * @param email    email que identifica o passageiro
     * @param lineName nome da linha a ser removida
     * @return Um utilizador caso seja alterado, caso contrário, null
     */
    public String removePassengerLine(String email, String lineName) {
        String resultString;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.UsersCollection);
        try {
            Bson filter = Filters.eq("email", email);
            Bson update = Updates.pull("lines", lineName);

            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.AFTER);
            Document result = collection.findOneAndUpdate(filter, update, options);

            System.out.println("Sucesso! Linha removida ao utilizador: " + result.toJson());

            resultString = result.toJson();
        } catch (MongoException me) {
            System.err.println("Não foi possível remover a linha devido a um erro: " + me);
            resultString = null;
        }

        mongoClient.close();

        return resultString;
    }

    /**
     * Inserir um relatório
     *
     * @param report relatório a ser inserido
     * @return true caso o relatório seja inserida com sucesso, caso contrário, false
     */
    public Boolean insertReport(Report report) {
        boolean isSuccessful;

        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = getMongoDatabase(mongoClient);

        MongoCollection<Document> collection = database.getCollection(DatabaseCollectionsNames.ReportsManagerCollection);
        try {
            InsertOneResult result = collection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("reportDate", report.getReportDate())
                    .append("line", report.getLine())
                    .append("totalWarnings", report.getNumWarnings())
                    .append("totalPassengersWarned", report.getNumPassengersWarned()));

            System.out.println("Sucesso! Relatório inserido: " + result.getInsertedId());

            isSuccessful = true;
        } catch (MongoException me) {
            System.err.println("Não foi posível inserir o relatório devido a um erro: " + me);
            isSuccessful = false;
        }

        mongoClient.close();

        return isSuccessful;
    }
}
