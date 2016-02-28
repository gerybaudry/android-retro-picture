package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import org.json.JSONObject;

public interface IRequest {
    String getRoute();
    void fillJSON(JSONObject root) throws Exception;
}
