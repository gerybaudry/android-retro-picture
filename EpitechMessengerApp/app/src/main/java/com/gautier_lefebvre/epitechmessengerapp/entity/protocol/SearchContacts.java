package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import com.gautier_lefebvre.epitechmessengerapp.activity.SearchContactsActivity;
import com.gautier_lefebvre.epitechmessengerapp.entity.UserData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchContacts {
    public static class Request implements IRequest {
        public static final String route = "secure_route";
        public static final String method = "search_contacts";

        public int userId;
        public String auth_token;
        public String searchPattern;

        public SearchContactsActivity activity;

        @Override
        public String getRoute() {
            return SearchContacts.Request.route + "/" + this.userId;
        }

        @Override
        public void fillJSON(JSONObject root) throws Exception {
            root.put("method", SearchContacts.Request.method);

            JSONObject body = new JSONObject();
            body.put("auth_token", this.auth_token);
            body.put("search_pattern", this.searchPattern);

            root.put("body", body);
        }
    }

    public static class Response implements IResponse {
        public boolean success;
        public String reason;

        public ArrayList<UserData> users;

        @Override
        public void fillFromJSON(JSONObject root) throws Exception {
            this.success = root.getBoolean("success");

            if (this.success) {
                this.users = new ArrayList<>();

                // deserialize users
                JSONArray usersData = root.getJSONArray("users");
                for (int i = 0 ; i < usersData.length() ; ++i) {
                    JSONObject userDataObj = usersData.getJSONObject(i);
                    UserData userData = new UserData();
                    userData.userId = userDataObj.getInt("id");
                    userData.nickname = userDataObj.getString("nickname");
                    this.users.add(userData);
                }
            } else {
                this.reason = root.getString("reason");
            }
        }
    }
}
