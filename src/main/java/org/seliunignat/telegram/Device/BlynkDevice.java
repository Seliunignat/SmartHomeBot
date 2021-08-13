package org.seliunignat.telegram.Device;

import org.bson.Document;
import org.bson.types.ObjectId;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class BlynkDevice {
    private static Map<String, String> ENV = System.getenv();
    public static String IP_ADDRESS = ENV.get("IP_ADDRESS");
    public static String AUTH_TOKEN = ENV.get("BLYNK_AUTH_TOKEN");

    private ObjectId _id = null;
    private Long deviceId;
    private String name;
    private String pin;

    public static enum DeviceStatus {
        TurnON ("1"),
        TurnOFF ("0");

        private String status;

        DeviceStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    private BlynkDevice(){

    }

    public BlynkDevice(Long deviceId, String name, String pin) {
        this.deviceId = deviceId;
        this.name = name;
        this.pin = pin;
    }

    public Response update(DeviceStatus deviceStatus) {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://" + IP_ADDRESS +"/" + AUTH_TOKEN + "/update/" + pin + "?value=" + deviceStatus.getStatus())
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        return response;
    }

    public Response getStatus() {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://" + IP_ADDRESS +"/" + AUTH_TOKEN + "/get/" + pin)
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        return response;
    }

    public Document toDocument() {
        Document document = new Document();
        if(_id != null)
            document.append("_id", _id);
        document.append("deviceId", deviceId)
                .append("name", name)
                .append("pin", pin);
        return document;
    }

    public static BlynkDevice documentToBlynkDevice(Document document) {
        BlynkDevice blynkDevice = new BlynkDevice();
        if(document.getObjectId("_id") != null)
            blynkDevice.setObjectId(document.getObjectId("_id"));

        if(document.getLong("deviceId") != null)
            blynkDevice.setDeviceId(document.getLong("deviceId"));

        if(document.getString("name") != null)
            blynkDevice.setName(document.getString("name"));

        if(document.getString("pin") != null)
            blynkDevice.setPin(document.getString("pin"));
        return blynkDevice;
    }

    public ObjectId getObjectId() {
        return _id;
    }

    public void setObjectId(ObjectId _id) {
        this._id = _id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
