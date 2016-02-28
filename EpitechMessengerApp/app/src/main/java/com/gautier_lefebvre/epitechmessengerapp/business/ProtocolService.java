package com.gautier_lefebvre.epitechmessengerapp.business;

import com.gautier_lefebvre.epitechmessengerapp.entity.ApplicationData;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.GetServerKey;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.IRequest;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.IResponse;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.SignIn;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.SignUp;
import com.gautier_lefebvre.epitechmessengerapp.exception.HTTPRequestFailedException;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProtocolService {
    private static ProtocolService _instance = new ProtocolService();
    public static ProtocolService getInstance() {
        return _instance;
    }
    private ProtocolService() {}

    /**
     * hostname of the HTTP server
     */
    public final String hostname = "10.0.2.2";

    /**
     * port of the HTTP server
     */
    public final int port = 8000;

    /**
     * @param path the route
     * @param method the HTTP verb
     * @param body the body, encoded in base64
     * @return the body of the HTTP response
     * @throws HTTPRequestFailedException
     */
    String sendEncryptedRequest(String path, String method, String body) throws HTTPRequestFailedException {
        if (body == null) {
            return this.sendEncryptedRequest(path, method);
        }

        String url = String.format("http://%s:%d/%s", this.hostname, this.port, path);
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .method(
                            method,
                            RequestBody.create(
                                    MediaType.parse("text/plain; charset=utf-8"),
                                    body.getBytes("utf-8")))
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }
    }

    /**
     * @param path the route
     * @param method the HTTP verb
     * @return the body of the HTTP response
     * @throws HTTPRequestFailedException
     */
    String sendEncryptedRequest(String path, String method) throws HTTPRequestFailedException {
        String url = String.format("http://%s:%d/%s", this.hostname, this.port, path);
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .method(method, null)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }
    }

    /**
     * @param path the route
     * @param method the http verb
     * @param body the body, in JSON
     * @return the body of the HTTP response
     * @throws HTTPRequestFailedException
     */
    String sendClearRequest(String path, String method, String body) throws HTTPRequestFailedException {
        if (body == null) {
            return this.sendClearRequest(path, method);
        }

        String url = String.format("http://%s:%d/%s", this.hostname, this.port, path);
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .method(
                            method,
                            RequestBody.create(
                                    MediaType.parse("application/json; charset=utf-8"),
                                    body.getBytes("utf-8")))
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }
    }

    /**
     * @param path the route
     * @param method the http verb
     * @return the body of the HTTP request
     * @throws HTTPRequestFailedException
     */
    String sendClearRequest(String path, String method) throws HTTPRequestFailedException {
        String url = String.format("http://%s:%d/%s", this.hostname, this.port, path);
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .method(method, null)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }
    }


    /**
     * @return the HTTP response
     * @throws HTTPRequestFailedException
     */
    public GetServerKey.Response getServerKey() throws HTTPRequestFailedException {
        GetServerKey.Response response = new GetServerKey.Response();
        String jsonResponseStr = this.sendClearRequest(GetServerKey.Request.route, "GET");

        try {
            JSONObject root = new JSONObject(jsonResponseStr);
            response.key = root.getString("key");
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }

        return response;
    }

    /**
     * Sends a request and fills the response
     * @param request the request to send
     * @param response the response to fill
     * @throws HTTPRequestFailedException
     */
    public void sendEncryptedRequest(IRequest request, IResponse response) throws HTTPRequestFailedException {
        JSONObject requestRoot = new JSONObject();

        try {
            request.fillJSON(requestRoot);
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }

        try {
            String encrypted = MessageService.encryptAES(requestRoot.toString(), ApplicationData.aesKey);

            String responseBody = this.sendEncryptedRequest(
                    request.getRoute(),
                    "POST",
                    encrypted);

            JSONObject responseRoot = new JSONObject(MessageService.decryptAES(responseBody, ApplicationData.aesKey));

            response.fillFromJSON(responseRoot);
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }
    }

    /**
     * @param signUpRequest request filled with user data
     * @return the HTTP response
     * @throws HTTPRequestFailedException
     */
    public SignUp.Response signUp(SignUp.Request signUpRequest) throws HTTPRequestFailedException {
        SignUp.Response response = new SignUp.Response();

        JSONObject obj = new JSONObject();
        try {
            signUpRequest.fillJSON(obj);
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }

        String responseBody;
        try {
            responseBody = this.sendClearRequest(SignUp.Request.route, "POST", obj.toString());
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }

        try {
            JSONObject jsonResponse = new JSONObject(MessageService.decryptAES(responseBody, ApplicationData.aesKey));
            response.fillFromJSON(jsonResponse);
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }

        if (response.success) {
            response.nickname = signUpRequest.nickname;
            response.email = signUpRequest.email;
        }

        return response;
    }

    /**
     * @param signInRequest request filled with user info
     * @return the HTTP response
     * @throws HTTPRequestFailedException
     */
    public SignIn.Response signIn(SignIn.Request signInRequest) throws HTTPRequestFailedException {
        SignIn.Response response = new SignIn.Response();

        JSONObject obj = new JSONObject();
        try {
            signInRequest.fillJSON(obj);
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }

        String responseBody = this.sendClearRequest(SignIn.Request.route, "POST", obj.toString());

        try {
            JSONObject jsonResponse = new JSONObject(MessageService.decryptAES(responseBody, ApplicationData.aesKey));
            response.fillFromJSON(jsonResponse);
        } catch (Exception e) {
            throw new HTTPRequestFailedException();
        }

        if (response.success) {
            response.email = signInRequest.email;
        }

        return response;
    }
}
