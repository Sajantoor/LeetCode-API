package com.leetcode.leetcodeapi.utilities;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.apache.http.entity.ContentType;

public class GraphQlQuery {
    private String query;
    private JSONObject variables;

    public GraphQlQuery(String query) {
        this.query = query;
    }

    public GraphQlQuery(String query, JSONObject variables) {
        this.query = query;
        this.variables = variables;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public JSONObject getVariables() {
        return variables;
    }

    public void setVariables(JSONObject variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        String query = this.query.replace("\n", "\\n");

        if (variables == null) {
            return String.format("{\"query\": \"%s\"}", query);
        }

        String variablesString = variables.toString().replace("\n", "\\n");

        return String.format("{\n \"query\": \"%s\",\n  \"variables\": %s \n}", query, variablesString);
    }

    public StringEntity getEntity() {
        return new StringEntity(this.toString(), ContentType.APPLICATION_JSON);
    }
}
