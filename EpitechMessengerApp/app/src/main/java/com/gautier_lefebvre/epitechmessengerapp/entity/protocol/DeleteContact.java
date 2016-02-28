package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import com.gautier_lefebvre.epitechmessengerapp.activity.HomeActivity;

import org.json.JSONObject;

public class DeleteContact {
    public static class Request implements IRequest {
        public static final String route = "secure_route";
        public static final String method = "delete_contact";

        public int userId;
        public String auth_token;
        public int contactId;

        public HomeActivity activity;

        @Override
        public String getRoute() {
            return DeleteContact.Request.route + "/" + this.userId;
        }

        @Override
        public void fillJSON(JSONObject root) throws Exception {
            root.put("method", DeleteContact.Request.method);

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

        @Override
        public void fillFromJSON(JSONObject root) throws Exception {
            this.success = root.getBoolean("success");

            if (this.success) {
                this.userId = root.getInt("id");
            } else {
                this.reason = root.getString("reason");
            }
        }
    }
}
