package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import org.json.JSONObject;

public class EditProfile {
    public static class Request implements IRequest {
        public static final String route = "secure_route";
        public static final String method = "edit_profile";

        public int userId;
        public String auth_token;
        public String nickname;
        public String password;

        @Override
        public String getRoute() {
            return EditProfile.Request.route + "/" + this.userId;
        }

        @Override
        public void fillJSON(JSONObject root) throws Exception {
            root.put("method", EditProfile.Request.method);

            JSONObject body = new JSONObject();
            body.put("auth_token", this.auth_token);
            body.put("nickname", this.nickname);
            body.put("password", this.password);

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
