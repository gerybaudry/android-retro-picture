package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import com.gautier_lefebvre.epitechmessengerapp.activity.SignInActivity;
import com.gautier_lefebvre.epitechmessengerapp.business.MessageService;
import com.gautier_lefebvre.epitechmessengerapp.entity.ApplicationData;

import org.json.JSONObject;

import javax.crypto.SecretKey;

public class SignIn {
    static public class Request implements IRequest {
        public static final String route = "sign_in";
        public static final String method = "sign_in";

        public String email;
        public String password;
        public SecretKey key;

        public SignInActivity activity;

        @Override
        public String getRoute() {
            return SignIn.Request.route;
        }

        @Override
        public void fillJSON(JSONObject root) throws Exception {
            root.put("method", SignIn.Request.method);

            JSONObject body = new JSONObject();
            body.put("email", MessageService.encryptRSA(this.email, ApplicationData.serverKey));
            body.put("password", MessageService.encryptRSA(this.password, ApplicationData.serverKey));
            body.put("key", MessageService.encryptRSA(this.key.getEncoded(), ApplicationData.serverKey));
            body.put("gcmToken", MessageService.encryptRSA(ApplicationData.gcmRegistrationToken, ApplicationData.serverKey));

            root.put("body", body);
        }
    }

    static public class Response implements IResponse {
        public int userId;
        public String nickname;
        public String email;

        public boolean success;
        public String authToken;
        public String reason;

        @Override
        public void fillFromJSON(JSONObject root) throws Exception {
            this.success = root.getBoolean("success");
            if (this.success) {
                this.userId = root.getInt("id");
                this.nickname = root.getString("nickname");
                this.authToken = root.getString("auth_token");
            } else {
                this.reason = root.getString("reason");
            }
        }
    }
}
