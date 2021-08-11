package org.seliunignat.telegram;
import org.seliunignat.telegram.SmartHomeBot.*;
import org.seliunignat.telegram.MongoDB.*;

import com.github.realzimboguy.ewelink.api.EweLink;
import com.github.realzimboguy.ewelink.api.model.devices.DeviceItem;
import com.github.realzimboguy.ewelink.api.model.devices.Devices;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.*;

public class Main {

    public static Map<String, String> ENV;
    private static Map<String, String> COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES;
    private static Map<String, Map.Entry<String, String>> COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES;
    private static String blynkAuthToken;
    private static List<Long> listOfAdmins;
    private static EweLink eweLink;
    private static Devices devices;
    private static final String DEVICE_STATUS_ON = "on";
    private static final String DEVICE_STATUS_OFF = "off";
    private static MongoDB mongoDB;

    public static void main(String[] args) throws TelegramApiException {
        ENV = System.getenv();
        loadData();
        mongoDB = new MongoDB();
        try {
            loadEwelink();
        } catch (Exception e) {
            e.printStackTrace();
        }


        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(
                    new SmartHomeBot(
                            ENV, COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES,
                            listOfAdmins, eweLink, COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES, mongoDB));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static void loadData(){
        blynkAuthToken = ENV.get("BLYNK_AUTH_TOKEN");
        listOfAdmins = new ArrayList<>();
        listOfAdmins.add(Long.parseLong(ENV.get("ADMIN_ID")));
        COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES = new HashMap<>();
        COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.put("Включить настольную лампу", "/update/V3?value=1");
        COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.put("Выключить настольную лампу", "/update/V3?value=0");
        COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.put("Включить цветную лампу", "/update/V2?value=1");
        COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.put("Выключить цветную лампу", "/update/V2?value=0");
        COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.put("Статус настольной лампы", "/get/V3");
        COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.put("Статус цветной лампы", "/get/V2");
        COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES = new HashMap<>();
        COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.put("Включить свет на террасе",
                new AbstractMap.SimpleEntry<String, String>(ENV.get("DEVICE_TERRACE_ID"), DEVICE_STATUS_ON));
        COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.put("Выключить свет на террасе",
                new AbstractMap.SimpleEntry<>(ENV.get("DEVICE_TERRACE_ID"), DEVICE_STATUS_OFF));
        COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.put("Включить свет на крыльце",
                new AbstractMap.SimpleEntry<>(ENV.get("DEVICE_PORCH_ID"), DEVICE_STATUS_ON));
        COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.put("Выключить свет на крыльце",
                new AbstractMap.SimpleEntry<>(ENV.get("DEVICE_PORCH_ID"), DEVICE_STATUS_OFF));
        COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.put("Включить свет под навесом",
                new AbstractMap.SimpleEntry<>(ENV.get("DEVICE_CANOPY_ID"), DEVICE_STATUS_ON));
        COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.put("Выключить свет под навесом",
                new AbstractMap.SimpleEntry<>(ENV.get("DEVICE_CANOPY_ID"), DEVICE_STATUS_OFF));
        COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.put("Статус террасы",
                new AbstractMap.SimpleEntry<>(ENV.get("DEVICE_TERRACE_ID"), DEVICE_STATUS_OFF));
        COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.put("Статус крыльца",
                new AbstractMap.SimpleEntry<>(ENV.get("DEVICE_PORCH_ID"), DEVICE_STATUS_OFF));
        COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.put("Статус навеса",
                new AbstractMap.SimpleEntry<>(ENV.get("DEVICE_CANOPY_ID"), DEVICE_STATUS_OFF));

    }

    public static void connectMongo(){
        ConnectionString connectionString = new ConnectionString("mongodb+srv://" + ENV.get("MONGO_USER") + ":" + ENV.get("MONGO_PASSWORD") + "@smarthomebotdb.iqsez.mongodb.net/SmartHomeBotDB?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);

        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> toys = database.getCollection("toys");
//        Document toy = new Document("name", "yoyo") .append("ages", new Document("min", 5));
//        ObjectId id = toys.insertOne(toy).getInsertedId().asObjectId().getValue();
//
//        Document yoyo = toys.find(new Document("name", "yoyo")).first();
//        System.out.println(yoyo.toJson());
    }

    private static void loadEwelink() throws Exception {
        eweLink = new EweLink("eu", ENV.get("EWELINK_EMAIL"), ENV.get("EWELINK_PASSWORD"), 60);
        eweLink.login();
        devices = eweLink.getDevices();
        for (DeviceItem devicelist : devices.getDevicelist()) {
            System.out.println(devicelist.getDeviceid());
            System.out.println(devicelist.getName());

            eweLink.getDeviceStatus(devicelist.getDeviceid());
            System.out.println(eweLink.getDeviceStatus(devicelist.getDeviceid()));
        }
    }
}
