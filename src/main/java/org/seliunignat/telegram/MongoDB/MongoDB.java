package org.seliunignat.telegram.MongoDB;

import com.github.realzimboguy.ewelink.api.model.devices.Do;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcernException;
import com.mongodb.client.*;
import org.bson.Document;
import org.seliunignat.telegram.Device.BlynkDevice;
import org.seliunignat.telegram.User.UserOfBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MongoDB {

    private Map<String, String> ENV = System.getenv();
    private static MongoClientSettings settings;
    private static MongoClient mongoClient;

    private static MongoDatabase database;

    private static MongoCollection<Document> usersCollection;
    private static MongoCollection<Document> blynkDevicesCollection;
    private static MongoCollection<Document> eweLinkDevicesCollection;

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

    public static UserOfBot saveUser(UserOfBot user) {
        try {
            usersCollection.insertOne(user.toDocument());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return UserOfBot.documentToUser(Objects.requireNonNull(
                usersCollection.find(new Document("userId", user.getUserId())).first()));
    }

    public static UserOfBot updateUser(UserOfBot userUpdating) {
        try {
            usersCollection.updateOne(new Document("_id", userUpdating.get_id()), new Document("$set", userUpdating.toDocument()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return UserOfBot.documentToUser(Objects.requireNonNull(
                usersCollection.find(new Document("userId", userUpdating.getUserId())).first()));
    }

    public static UserOfBot getUserByUserId(Long userId) {
        return UserOfBot.documentToUser(Objects.requireNonNull(
                usersCollection.find(new Document("userId", userId)).first()));
    }

    public static List<UserOfBot> getAllUsers() {
        List<UserOfBot> users = new ArrayList<>();
        MongoCursor<Document> cursor = usersCollection.find().iterator();
        while (cursor.hasNext()) {
            users.add(UserOfBot.documentToUser(cursor.next()));
        }
        return users;
    }

    public BlynkDevice saveBlynkDevice(BlynkDevice blynkDevice) {
        try {
            blynkDevicesCollection.insertOne(blynkDevice.toDocument());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return BlynkDevice.documentToBlynkDevice(Objects.requireNonNull(
                blynkDevicesCollection.find(new Document("deviceId", blynkDevice.getDeviceId())).first()));
    }

}
