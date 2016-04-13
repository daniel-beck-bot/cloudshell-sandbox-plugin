/*   Copyright 2013, MANDIANT, Eric Lordahl
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.jenkinsci.plugins.cloudshell;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Time;

public class CsServer {
	public final String serverAddress;
    public final String user;
    public final String pw;
    public final int port;

	public CsServer(String serverAddress, String user, String pw, int port)
	{
		this.user = user;
		this.pw = pw;
		this.serverAddress = serverAddress;
        this.port = port;
    }
    public String StartBluePrint(AbstractBuild<?,?> build, String BpName, String SbName, String SbDuration, BuildListener listener)
    {
        String token = InvokeLogin(GetBaseUrl(), this.user, this.pw, "Global");
        String url = GetBaseUrl() + "/v1/blueprints/"+ BpName +"/start";

        String result = ExecutePost(url, token, SbName, SbDuration);
        JSONObject j = JSONObject.fromObject(result);
        if (j.toString().contains("errorCategory")) {
            throw new RuntimeException("Failed to start blueprint: " + j);
        }
        try
        {
            listener.getLogger().println("SandBox Created: ");
            String newSb = j.getString("id");
            listener.getLogger().println(newSb);
            WaitForSandBox(newSb, 300);
            return newSb;
        }
        catch (Exception e)
        {
            listener.getLogger().println(j);
        }
        return null;
    }

    public void StopBluePrint(String SbId, BuildListener listener)
    {
        String token = InvokeLogin(GetBaseUrl(), this.user, this.pw, "Global");
        String url = GetBaseUrl() + "/v1/sandboxes/" + SbId + "/stop";

        String result = ExecutePost(url, token, null, null);
        JSONObject j = JSONObject.fromObject(result);

        if (j.toString().contains("errorCategory")) {
            throw new RuntimeException("Failed to stop blueprint: " + j);
        }

        try
        {
            listener.getLogger().println("SandBox Stopped: ");
            listener.getLogger().println(SbId);
        }
        catch (Exception e)
        {
            listener.getLogger().println(j);
        }
    }

    private String GetSandBoxStatus(String sb)
    {
        return SandboxDetails(sb).getString("state");
    }

    private void WaitForSandBox(String sb, int timeoutSec)
    {
        long startTime = System.currentTimeMillis();
        while (!GetSandBoxStatus(sb).equals("Ready") && (System.currentTimeMillis()-startTime) < timeoutSec*1000)
        {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void GetBluePrintDetails(String BpName, BuildListener listener)
    {
        //TODO : Need to query the specific blueprint
        listener.getLogger().println("Get Blueprint By Name");
        String token = InvokeLogin(GetBaseUrl(), this.user, this.pw, "Global");
        String url = GetBaseUrl() + "/v1/blueprints";
        listener.getLogger().println(ExecuteGet(url, token));
    }

    private JSONObject SandboxDetails(String sb)
    {
        String token = InvokeLogin(GetBaseUrl(), this.user, this.pw, "Global");
        String url = GetBaseUrl() + "/v1/sandboxes/" + sb;
        String result = ExecuteGet(url, token);
        JSONObject j = JSONObject.fromObject(result);

        if (j.toString().contains("errorCategory")) {
            throw new RuntimeException("Failed to get sandbox details: " + j);
        }
        return j;
    }

    public void GetSandBoxDetails(String SbId, BuildListener listener)
    {
        listener.getLogger().println("Get SandBox By Id: ");
        listener.getLogger().println(SandboxDetails(SbId));
    }

    public String InvokeLogin(String url, String user, String password, String domain) {
        url = url + "/Login";
        DefaultHttpClient httpClient = new DefaultHttpClient();
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
                return "success";
            }
            putRequest.setEntity(input);
            HttpResponse response = httpClient.execute(putRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
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
        return result.toString().replace("\"","");
    }


    public String ExecuteGet(String url, String token)
    {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        request.addHeader("Authorization", "Basic " + token);
        HttpResponse response = null;
        try {
            response = client.execute(request);
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
                System.out.println(line);
                out += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    public String ExecutePost(String url, String token, String name, String duration)
    {
        duration = "PT" + duration + "M";
        HttpClient client = new DefaultHttpClient();
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
        try {
            response = client.execute(request);
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
                System.out.println(line);
                out += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    private String GetBaseUrl()
    {
        return "http://" + this.serverAddress + ":" + this.port + "/Api";
    }

}
