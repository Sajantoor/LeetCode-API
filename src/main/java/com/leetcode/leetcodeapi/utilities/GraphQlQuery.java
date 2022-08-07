package com.leetcode.leetcodeapi.utilities;

import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;

public class GraphQlQuery {
    private String query;
    private Map<String, String> variables;

    public GraphQlQuery(String query) {
        this.query = query;
    }

    public GraphQlQuery(String query, Map<String, String> variables) {
        this.query = query;
        this.variables = variables;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        String query = this.query.replace("\n", "\\n");

        if (variables == null) {
            return String.format("{\"query\": \"%s\"}", query);
        }

        StringBuilder variables = new StringBuilder();

        // TODO: Probably a better way to do this
        for (Map.Entry<String, String> variable : this.variables.entrySet()) {
            variables.append(String.format("\"%s\": \"%s\"", variable.getKey(), variable.getValue()));
            // append comma if not last entry in map
            if (variable.getKey() != this.variables.keySet().toArray()[this.variables.keySet().size() - 1]) {
                variables.append(", ");
            }
        }

        return String.format("{\n \"query\": \"%s\",\n  \"variables\": {\n %s \n}\n}", query, variables);
    }

    public StringEntity getEntity() {
        return new StringEntity(this.toString(), ContentType.APPLICATION_JSON);
    }
}
