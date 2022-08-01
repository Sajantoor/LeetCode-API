package com.leetcode.leetcodeapi.utilities;

import java.util.Map;

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

        for (Map.Entry<String, String> entry : this.variables.entrySet()) {
            variables.append(String.format("\"%s\": \"%s\"", entry.getKey(), entry.getValue()));
        }

        return String.format("{\n \"query\": \"%s\",\n  \"variables\": {\n %s \n}\n}", query, variables);
    }
}
