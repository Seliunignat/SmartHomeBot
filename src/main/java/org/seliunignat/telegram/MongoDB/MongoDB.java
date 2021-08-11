package org.seliunignat.telegram.MongoDB;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcernException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.seliunignat.telegram.User.UserOfBot;

import java.util.Map;
import java.util.Objects;

public class MongoDB {

    private Map<String, String> ENV = System.getenv();
    private MongoClientSettings settings;
    private MongoClient mongoClient;

    private MongoDatabase database;

    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> blynkDevicesCollection;
    private MongoCollection<Document> eweLinkDevicesCollection;

    ConnectionString connectionString = new ConnectionString("mongodb+srv://" + ENV.get("MONGO_USER") + ":" + ENV.get("MONGO_PASSWORD") + "@smarthomebotdb.iqsez.mongodb.net/SmartHomeBotDB?retryWrites=true&w=majority");

    public MongoDB() {
        settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("SmartHomeBot");

        usersCollection = database.getCollection("users");
        blynkDevicesCollection = database.getCollection("blynkDevices");
        eweLinkDevicesCollection = database.getCollection("eweLinkDevices");
    }

    public MongoClientSettings getSettings() {
        return settings;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<Document> getUsersCollection() {
        return usersCollection;
    }

    public MongoCollection<Document> getBlynkDevicesCollection() {
        return blynkDevicesCollection;
    }

    public MongoCollection<Document> getEweLinkDevicesCollection() {
        return eweLinkDevicesCollection;
    }

    public UserOfBot saveUser(UserOfBot user) {
        try {
            usersCollection.insertOne(user.toDocument());
        } catch (WriteConcernException e) {
            System.out.println(e.getMessage());
        }
        return UserOfBot.documentToUser(Objects.requireNonNull(usersCollection.find(new Document("userId", user.getUserId())).first()));
    }

}
