package org.jenkinsci.plugins.cloudshell;

import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class HTTPWrapper {




    public static RestResponse ExecuteGet(String url, String token)
    {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.addHeader("Authorization", "Basic " + token);
        HttpResponse response = null;
        int responseCode = 0;
        try {
            response = client.execute(request);
            responseCode = response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line;
        String out = "";
        try {
            while ((line = rd.readLine()) != null) {
                out += line;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new RestResponse(out, responseCode);

    }

    public static RestResponse ExecutePost(String url, String token, String name, String duration)
    {
        duration = "PT" + duration + "M";
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        request.addHeader("Authorization", "Basic " + token);
        request.setHeader("Content-type", "application/json");
        StringEntity params = null;
        try {
            params = new StringEntity("{\"name\":\""+ name +"\",\"duration\":\""+ duration +"\"}");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        request.setEntity(params);
        HttpResponse response = null;
        int responseCode = 0;

        try {
            response = client.execute(request);
            responseCode = response.getStatusLine().getStatusCode();

        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line;
        String out = "";
        try {
            while ((line = rd.readLine()) != null) {
                out += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new RestResponse(out, responseCode);
    }

    public static RestResponse InvokeLogin(String url, String user, String password, String domain) {
        url = url + "/Login";
        HttpClient httpClient = HttpClientBuilder.create().build();
        StringBuilder result = new StringBuilder();
        try {
            HttpPut putRequest = new HttpPut(url);
            putRequest.addHeader("Content-Type", "application/json");
            putRequest.addHeader("Accept", "application/json");
            JSONObject keyArg = new JSONObject();
            keyArg.put("username", user);
            keyArg.put("password", password);
            keyArg.put("domain", domain);
            StringEntity input;
            try {
                input = new StringEntity(keyArg.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw e;
            }
            putRequest.setEntity(input);
            HttpResponse response = httpClient.execute(putRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed to login: "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));
            String output;
            while ((output = br.readLine()) != null) {
                result.append(output);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String out =  result.toString().replace("\"","");
        return new RestResponse(out,200);
    }
}