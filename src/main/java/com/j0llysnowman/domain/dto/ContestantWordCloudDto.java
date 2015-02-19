package com.j0llysnowman.domain.dto;

import java.util.List;

import org.json.JSONObject;

/**
 * Created by dsumera on 2/18/15.
 */
public class ContestantWordCloudDto {

    private String contestant;

    private List<JSONObject> wordCloud;

    public String getContestant() {
        return contestant;
    }

    public void setContestant(String contestant) {
        this.contestant = contestant;
    }

    public List<JSONObject> getWordCloud() {
        return wordCloud;
    }

    public void setWordCloud(List<JSONObject> wordCloud) {
        this.wordCloud = wordCloud;
    }
}
