package org.seliunignat.telegram.User;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.telegram.telegrambots.meta.api.objects.User;

public class UserOfBot {
    private ObjectId _id = null;
    private Long userId = null;
    private String firstName = null;
    private String lastName = null;
    private String username = null;
    private Boolean isAdmin = false;

    public UserOfBot() {
    }

    public UserOfBot(Long userId, String firstName, String lastName, String username) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
    }

    public Document toDocument() {
        Document document = new Document();
        if(_id != null)
            document.append("_id", _id);
        document.append("userId", userId)
                .append("firstName", firstName)
                .append("lastName", lastName)
                .append("username", username)
                .append("isAdmin", isAdmin);
        return document;
    }

    public static UserOfBot documentToUser(Document document) {
        UserOfBot newUserOfBot = new UserOfBot();
        if(document.getObjectId("_id") != null)
            newUserOfBot.setObjectId(document.getObjectId("_id"));

        if(document.getLong("userId") != null)
            newUserOfBot.setUserId(document.getLong("userId"));

        if(document.getString("firstName") != null)
            newUserOfBot.setFirstName(document.getString("firstName"));

        if(document.getString("lastName") != null)
            newUserOfBot.setLastName(document.getString("lastName"));

        if(document.getString("username") != null)
            newUserOfBot.setUsername(document.getString("username"));

        newUserOfBot.setAdmin(document.getBoolean("isAdmin", false));
        return newUserOfBot;
    }

    public static UserOfBot telegramUserToUserOfBot(User user) {
        return new UserOfBot(user.getId(), user.getFirstName(), user.getLastName(), user.getUserName());
    }

    public void setObjectId(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId get_id() {
        return _id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
}
