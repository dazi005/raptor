/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.auth;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AuthHttpClient {

  public class ClientException extends Exception {

    private int code = 0;
    private String reason;

    public int getCode() {
      return code;
    }

    public String getReason() {
      return reason;
    }

    @Override
    public String getMessage() {
      if (getCode() > 0) {
        return getCode() + " " + getReason();
      }
      return super.getMessage();
    }

    public ClientException(int code, String reason) {
      this.reason = reason;
      this.code = code;
    }

    public ClientException(Throwable t) {
      super(t);
    }

    public ClientException(String m, Throwable t) {
      super(m, t);
    }

    public ClientException(String m) {
      super(m);
    }

  }

  final private CloseableHttpClient httpclient = HttpClients.createDefault();

  private String checkUrl;
  private String url;

  public AuthHttpClient() {
  }

  public AuthHttpClient(String url) {
    this.url = url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setCheckUrl(String url) {
    this.checkUrl = url;
  }

  public String check(String accessToken, List<NameValuePair> args) throws ClientException {

    HttpPost httpost = new HttpPost(url);

    httpost.setHeader("Authorization", accessToken);

    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(args, Consts.UTF_8);
    httpost.setEntity(entity);

    String response = null;

    try (CloseableHttpResponse httpResponse = httpclient.execute(httpost)) {

      if (httpResponse.getStatusLine().getStatusCode() >= 400) {
        throw new ClientException(
                httpResponse.getStatusLine().getStatusCode(),
                httpResponse.getStatusLine().getReasonPhrase()
        //          "Request error [code: " + httpResponse.getStatusLine().getStatusCode()
        //          + ", reason: "+ httpResponse.getStatusLine().getReasonPhrase() + "]"
        );
      }

      InputStream inputStream = httpResponse.getEntity().getContent();

      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }

      response = result.toString(Consts.UTF_8.name());

    } catch (IOException ex) {
      throw new ClientException(ex);
    }

    return response;
  }

  public boolean sync(String accessToken, String body) throws ClientException {

    HttpPost httpost = new HttpPost(checkUrl);

    httpost.addHeader("Authorization", accessToken);
    httpost.addHeader("Accept", "application/json");
    httpost.addHeader("Content-Type", "application/json");

    StringEntity entity = new StringEntity(body, Consts.UTF_8);
    httpost.setEntity(entity);

    try (CloseableHttpResponse httpResponse = httpclient.execute(httpost)) {

      if (httpResponse.getStatusLine().getStatusCode() >= 400) {
        throw new ClientException(
                httpResponse.getStatusLine().getStatusCode(),
                httpResponse.getStatusLine().getReasonPhrase()
        );
      }

      InputStream inputStream = httpResponse.getEntity().getContent();
      JsonNode json = AuthProvider.mapper.readTree(inputStream);

      if (json.has("result")) {
        return json.get("result").asBoolean();
      }

      throw new IOException("Cannot read sync response: " + json.toString());

    } catch (IOException ex) {
      throw new ClientException(ex);
    }
  }

}
