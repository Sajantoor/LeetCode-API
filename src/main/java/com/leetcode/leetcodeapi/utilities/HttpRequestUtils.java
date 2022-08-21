package com.leetcode.leetcodeapi.utilities;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpRequestUtils {
    /**
     * 
     * @param httpRequest
     * @return
     * @throws ResponseStatusException
     */
    public static CloseableHttpResponse makeHttpRequest(HttpRequestBase httpRequest, CloseableHttpClient client)
            throws ResponseStatusException {
        try {
            CloseableHttpResponse response = client.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                if (statusCode == 499) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request (499)");
                }

                HttpStatus status = HttpStatus.valueOf(statusCode);
                String message = response.getStatusLine().getReasonPhrase();
                throw new ResponseStatusException(status, message);
            }

            return response;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable" + e.getMessage());
        }
    }

    /**
     * 
     * @param response
     * @return
     */
    public static JsonNode getJsonFromBody(CloseableHttpResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);
        return mapper.readTree(body);
    }

    public static String convertModelToJsonString(Object model) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(model);
    }
}
