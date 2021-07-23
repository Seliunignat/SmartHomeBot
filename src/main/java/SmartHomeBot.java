import com.github.realzimboguy.ewelink.api.EweLink;
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

    public SmartHomeBot(Map<String, String> ENV, Map<String, String> commands_and_requests_for_blynk_devices, List<Long> listOfAdmins, EweLink eweLink, Map<String, Map.Entry<String, String>> commands_id_status_for_ewelink_devices) {
        this.ENV = ENV;
        this.COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES = commands_and_requests_for_blynk_devices;
        this.listOfAdmins = listOfAdmins;
        this.ewelink = eweLink;
        this.COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES = commands_id_status_for_ewelink_devices;
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
        if (message != null && message.hasText()) {
            try {
                System.out.print(formatForDateNow.format(dateNow) + " ");
                System.out.println(message.getFrom().getFirstName() + " " + message.getFrom().getLastName()
                        + " (@" + message.getFrom().getUserName() + ", id: "
                        + message.getFrom().getId() + ")" + ": " + message.getText());

                if (COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.get(message.getText()) != null && listOfAdmins.contains(message.getFrom().getId())) {
                    Client client = ClientBuilder.newClient();
                    try (Response response = client.target("http://blynk-cloud.com/" + ENV.get("BLYNK_AUTH_TOKEN") + COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.get(message.getText()))
                            .request(MediaType.TEXT_PLAIN_TYPE)
                            .get()) {

                        System.out.println("status: " + response.getStatus());
                        System.out.println("headers: " + response.getHeaders());
                        System.out.println("body:" + response.readEntity(String.class));
                    } catch (Exception exception) {
                        System.out.println(exception.getMessage());
                    }
                } else if (COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.get(message.getText()) != null && listOfAdmins.contains(message.getFrom().getId())) {
                    try {
                        ewelink.setDeviceStatus(COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.get(message.getText()).getKey(), COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.get(message.getText()).getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                SendMessage sendMessage = new SendMessage(Long.toString(message.getChatId()), "Вы написали: " + message.getText());
                setButtons(sendMessage);
                execute(sendMessage);

                switch (message.getText()) {
//                    case "Статус гирлянды":
//                    {
//                        Client client = ClientBuilder.newClient();
//                        Response response = client.target("http://blynk-cloud.com/" + blynkAuthToken + COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.get(message.getText()))
//                                .request(MediaType.TEXT_PLAIN_TYPE)
//                                .get();
//
//                        System.out.println("status: " + response.getStatus());
//                        System.out.println("headers: " + response.getHeaders());
//                        System.out.println("body:" + response.readEntity(String.class));
//                        break;
//                    }
//                    case "Включить гирлянду":
//                    {
//                        Client client = ClientBuilder.newClient();
//                        Response response = client.target("http://blynk-cloud.com/" + blynkAuthToken + COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.get(message.getText()))
//                                .request(MediaType.TEXT_PLAIN_TYPE)
//                                .get();
//
//                        System.out.println("status: " + response.getStatus());
//                        System.out.println("headers: " + response.getHeaders());
//                        System.out.println("body:" + response.readEntity(String.class));
//                        break;
//                    }
                    default:
                        break;
                }

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void setButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRowList = new ArrayList<KeyboardRow>();
        for (String command :
                COMMANDS_AND_REQUESTS_FOR_BLYNK_DEVICES.keySet()) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(new KeyboardButton(command));
            keyboardRowList.add(keyboardRow);
        }
        for (String command :
                COMMANDS_ID_STATUS_FOR_EWELINK_DEVICES.keySet()) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(new KeyboardButton(command));
            keyboardRowList.add(keyboardRow);
        }
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }
}
