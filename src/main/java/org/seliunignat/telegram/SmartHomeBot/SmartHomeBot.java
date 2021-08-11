package org.seliunignat.telegram.SmartHomeBot;
import org.bson.Document;
import org.seliunignat.telegram.MongoDB.*;

import com.github.realzimboguy.ewelink.api.EweLink;
import com.github.realzimboguy.ewelink.api.model.Status;
import org.seliunignat.telegram.User.UserOfBot;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import java.text.SimpleDateFormat;
import java.util.*;

public class SmartHomeBot extends TelegramLongPollingBot {

    private static Map<String, String> ENV;
    private Map<String, String> COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES;
    private Map<String, Map.Entry<String, String>> COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES;
    private List<Long> listOfAdmins;
    private EweLink ewelink;
    private MongoDB mongoDB;

    public SmartHomeBot(Map<String, String> ENV, Map<String, String> commands_and_requests_for_blynk_devices, List<Long> listOfAdmins, EweLink eweLink, Map<String, Map.Entry<String, String>> commands_id_status_for_ewelink_devices, MongoDB receivedMongoDB){
        this.ENV = ENV;
        this.COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES = commands_and_requests_for_blynk_devices;
        this.listOfAdmins = listOfAdmins;
        this.ewelink = eweLink;
        this.COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES = commands_id_status_for_ewelink_devices;
        mongoDB = receivedMongoDB;
        try {
            sendCustomMessage();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return ENV.get("BOT_USERNAME");
    }

    public String getBotToken() {
        return ENV.get("BOT_TOKEN");
    }

    public void onUpdateReceived(Update update) {
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy hh:mm a");
        Message message = update.getMessage();

        if(message != null && message.hasText()){
            try {
                System.out.print(formatForDateNow.format(dateNow) + " ");
                System.out.println(message.getFrom().getFirstName() + " " + message.getFrom().getLastName()
                        + " (@" + message.getFrom().getUserName() + ", id: "
                        + message.getFrom().getId() + ")" + ": " + message.getText());

                UserOfBot user;

                Document foundUserDocument = mongoDB.getUsersCollection().find(new Document("userId", update.getMessage().getFrom().getId())).first();
                if(foundUserDocument != null) {
                    user = UserOfBot.documentToUser(foundUserDocument);
                } else {
                    System.out.println("User wasn't found!");
                    user = mongoDB.saveUser(UserOfBot.telegramUserToUserOfBot(update.getMessage().getFrom()));
                }

                if(COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.get(message.getText()) != null && user.getAdmin()) {
                    if(checkBlynkHardwareConnection()) {
                        //call the method that will work with requests for blynk
                        blynkDeviceSendRequest(message);
                    } else {
                        SendMessage sendMessage = new SendMessage(Long.toString(message.getChatId()), "Устройство офлайн(");
                        setButtons(sendMessage);
                        execute(sendMessage);
                    }
                } else if(COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.get(message.getText()) != null && user.getAdmin()){
                    //call the method that will work with requests for ewelink
                    ewelinkDeviceSendRequest(message);
                }
                //echo message
                SendMessage sendMessage = new SendMessage(Long.toString(message.getChatId()), "Вы написали: " + message.getText());
                setButtons(sendMessage);
                execute(sendMessage);

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendCustomMessage() throws TelegramApiException {
//        SendMessage sendMessage = new SendMessage(, );
//        setButtons(sendMessage);
//        execute(sendMessage);
    }

    public void setButtons(SendMessage sendMessage){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRowList = new ArrayList<KeyboardRow>();
        for (String command : COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.keySet()) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(new KeyboardButton(command));
            keyboardRowList.add(keyboardRow);
        }
        for (String command : COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.keySet()) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(new KeyboardButton(command));
            keyboardRowList.add(keyboardRow);
        }
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    private boolean checkBlynkHardwareConnection() {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://" + ENV.get("IP_ADDRESS") +"/" + ENV.get("BLYNK_AUTH_TOKEN") + "/isHardwareConnected")
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();

        Boolean responseBoolean = response.readEntity(Boolean.class);
        return responseBoolean;
    }

    private void blynkDeviceSendRequest(Message message) throws TelegramApiException {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://" + ENV.get("IP_ADDRESS") +"/" + ENV.get("BLYNK_AUTH_TOKEN") + COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.get(message.getText()))
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();

        String response_entity_string = response.readEntity(String.class);

        System.out.println("status: " + response.getStatus());
        System.out.println("headers: " + response.getHeaders());
        System.out.println("body:" + response_entity_string);

        if(message.getText().toLowerCase().startsWith("статус"))
        {
            //the method that will process th status requests
            statusOfBlynkDeviceSendMessage(message, response_entity_string);
        }

        response.close();
    }

    private void ewelinkDeviceSendRequest(Message message) {
        if(message.getText().toLowerCase().startsWith("статус")) {
            //the method that will process th status requests
            statusOfEwelinkDeviceSendMessage(message);
        } else {
            try {
                ewelink.setDeviceStatus(COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.get(message.getText()).getKey(), COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.get(message.getText()).getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void statusOfBlynkDeviceSendMessage(Message message, String response_entity_string) throws TelegramApiException {
        String status;
        switch (response_entity_string) {
            case "[\"0\"]":
                status = "Выключен(а)";
                break;
            case "[\"1\"]":
                status = "Включен(а)";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + response_entity_string);
        }

        SendMessage sendMessage = new SendMessage(Long.toString(message.getChatId()), status);
        setButtons(sendMessage);
        execute(sendMessage);
    }

    private void statusOfEwelinkDeviceSendMessage(Message message) {
        String statusMessage;
        try {
            Status status = ewelink.getDeviceStatus(COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.get(message.getText()).getKey());
            String switchValue = status.getParams().getSwitchValue();
            switch (switchValue) {
                case "off":
                    statusMessage = "Выключен(а)";
                    break;
                case "on":
                    statusMessage = "Включен(а)";
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + switchValue);
            }

            SendMessage sendMessage = new SendMessage(Long.toString(message.getChatId()), statusMessage);
            setButtons(sendMessage);
            execute(sendMessage);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
