package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import com.gautier_lefebvre.epitechmessengerapp.activity.ConversationActivity;

import org.json.JSONObject;

public class SendMessage {
    public static class Request implements IRequest {
        public static final String route = "secure_route";
        public static final String method = "send_message";

        public int userId;
        public String auth_token;
        public int contactId;
        public String content;

        public ConversationActivity activity;

        @Override
        public String getRoute() {
            return SendMessage.Request.route + "/" + this.userId;
        }

        @Override
        public void fillJSON(JSONObject root) throws Exception {
            root.put("method", SendMessage.Request.method);

            JSONObject body = new JSONObject();
            body.put("auth_token", this.auth_token);
            body.put("contact", this.contactId);
            body.put("content", this.content);

            root.put("body", body);
        }
    }

    public static class Response implements IResponse {
        public boolean success;
        public String reason;

        @Override
        public void fillFromJSON(JSONObject root) throws Exception {
            this.success = root.getBoolean("success");

            if (!this.success) {
                this.reason = root.getString("reason");
            }
        }
    }
}
