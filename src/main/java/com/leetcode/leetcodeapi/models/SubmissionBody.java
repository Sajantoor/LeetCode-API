package com.leetcode.leetcodeapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

// swagger docs
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Submission body")
public class SubmissionBody {
    @JsonProperty("lang - language")
    private String lang;
    @JsonProperty("typed code - solution code")
    private String typed_code;
    @JsonProperty("question_id - number of the question")
    private int question_id;

    public SubmissionBody() {

    }

    public SubmissionBody(String lang, String typed_code, int question_id) {
        this.lang = lang;
        this.typed_code = typed_code;
        this.question_id = question_id;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getTyped_code() {
        return typed_code;
    }

    public void setTyped_code(String typed_code) {
        this.typed_code = typed_code;
    }

    public int getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(int question_id) {
        this.question_id = question_id;
    }
}
