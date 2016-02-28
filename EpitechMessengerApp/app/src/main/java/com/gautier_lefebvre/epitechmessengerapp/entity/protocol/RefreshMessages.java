package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import com.gautier_lefebvre.epitechmessengerapp.activity.AppActivity;
import com.gautier_lefebvre.epitechmessengerapp.entity.MessageData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RefreshMessages {
    public static class Request implements IRequest {
        public static final String route = "secure_route";
        public static final String method = "get_pending_messages";

        public int userId;
        public String auth_token;

        public AppActivity activity;

        @Override
        public String getRoute() {
            return RefreshMessages.Request.route + "/" + this.userId;
        }

        @Override
        public void fillJSON(JSONObject root) throws Exception {
            root.put("method", RefreshMessages.Request.method);

            JSONObject body = new JSONObject();
            body.put("auth_token", this.auth_token);

            root.put("body", body);
        }
    }

    public static class Response implements IResponse {
        public boolean success;
        public String reason;

        public ArrayList<MessageData> messages;

        @Override
        public void fillFromJSON(JSONObject root) throws Exception {
            this.success = root.getBoolean("success");

            if (this.success) {
                this.messages = new ArrayList<>();

                // deserialize messages
                JSONArray messagesData = root.getJSONArray("messages");
                for (int i = 0 ; i < messagesData.length() ; ++i) {
                    JSONObject messageDataObj = messagesData.getJSONObject(i);
                    MessageData messageData = new MessageData();
                    messageData.contactId = messageDataObj.getInt("contact");
                    messageData.content = messageDataObj.getString("content");
                    messageData.date = messageDataObj.getString("date");
                    this.messages.add(messageData);
                }
            } else {
                this.reason = root.getString("reason");
            }
        }
    }
}
