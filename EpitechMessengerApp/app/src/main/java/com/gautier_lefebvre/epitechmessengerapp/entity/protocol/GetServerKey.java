package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import android.app.Activity;

public class GetServerKey {
    static public class Request {
        public static final String route = "server_key";

        public Activity activity;
    }

    static public class Response {
        public String key;
    }
}
