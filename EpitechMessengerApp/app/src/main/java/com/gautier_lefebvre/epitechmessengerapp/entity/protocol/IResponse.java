package com.gautier_lefebvre.epitechmessengerapp.entity.protocol;

import org.json.JSONObject;

public interface IResponse {
    void fillFromJSON(JSONObject root) throws Exception;
}
