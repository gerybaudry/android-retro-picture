package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import com.gautier_lefebvre.epitechmessengerapp.activity.SignUpActivity;
import com.gautier_lefebvre.epitechmessengerapp.business.MessageService;
import com.gautier_lefebvre.epitechmessengerapp.entity.ApplicationData;

import org.json.JSONObject;

import javax.crypto.SecretKey;

public class SignUp {
    static public class Request implements IRequest {
        public static final String route = "sign_up";
        public static final String method = "sign_up";

        public String nickname;
        public String email;
        public String password;
        public SecretKey key;

        public SignUpActivity activity;

        @Override
        public String getRoute() {
            return SignUp.Request.route;
        }

        @Override
        public void fillJSON(JSONObject root) throws Exception {
            root.put("method", SignUp.Request.method);

            JSONObject body = new JSONObject();
            body.put("nickname", MessageService.encryptRSA(this.nickname, ApplicationData.serverKey));
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
        public String authToken;

        public boolean success;
        public String reason;


        @Override
        public void fillFromJSON(JSONObject root) throws Exception {
            this.success = root.getBoolean("success");
            if (this.success) {
                this.userId = root.getInt("id");
                this.authToken = root.getString("auth_token");
            } else {
                this.reason = root.getString("reason");
            }
        }
    }
}
