package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import com.gautier_lefebvre.epitechmessengerapp.activity.AppActivity;

import org.json.JSONObject;

public class AddContact {
    public static class Request implements IRequest {
        public static final String route = "secure_route";
        public static final String method = "add_contact";

        public int userId;
        public String auth_token;
        public int contactId;

        public AppActivity activity;

        @Override
        public String getRoute() {
            return AddContact.Request.route + "/" + this.userId;
        }

        @Override
        public void fillJSON(JSONObject root) throws Exception {
            root.put("method", AddContact.Request.method);

            JSONObject body = new JSONObject();
            body.put("auth_token", this.auth_token);
            body.put("contact", this.contactId);

            root.put("body", body);
        }
    }

    public static class Response implements IResponse {
        public boolean success;
        public String reason;

        public int userId;
        public String nickname;

        @Override
        public void fillFromJSON(JSONObject root) throws Exception {
            this.success = root.getBoolean("success");

            if (this.success) {
                this.userId = root.getInt("id");
                this.nickname = root.getString("nickname");
            } else {
                this.reason = root.getString("reason");
            }
        }
    }
}
