package com.j0llysnowman.domain.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONObject;

/**
 * Created by dsumera on 2/18/15.
 */
public class BachelorDto {

    private boolean willGenerateWordCloud;

    private boolean willGenerateTopContestants;

    private String query;

    private int count;

    private int resultCeiling;

    private List<String> tweets = new ArrayList<String>();

    private List<JSONObject> wordCloud;

    private LinkedHashMap<String, Integer> remainingContestants;

    private List<ContestantWordCloudDto> contestantWordCloud =
        new ArrayList<ContestantWordCloudDto>();

    private String tweetType;

    public int getResultCeiling() {
        return resultCeiling;
    }

    public void setResultCeiling(int resultCeiling) {
        this.resultCeiling = resultCeiling;
    }

    public List<ContestantWordCloudDto> getContestantWordCloud() {
        return contestantWordCloud;
    }

    public void setContestantWordCloud(List<ContestantWordCloudDto> contestantWordCloud) {
        this.contestantWordCloud = contestantWordCloud;
    }

    public List<JSONObject> getWordCloud() {
        return wordCloud;
    }

    public void setWordCloud(List<JSONObject> wordCloud) {
        this.wordCloud = wordCloud;
    }

    public String getTweetType() {
        return tweetType;
    }

    public void setTweetType(String tweetType) {
        this.tweetType = tweetType;
    }

    public boolean isWillGenerateWordCloud() {
        return willGenerateWordCloud;
    }

    public void setWillGenerateWordCloud(boolean willGenerateWordCloud) {
        this.willGenerateWordCloud = willGenerateWordCloud;
    }

    public boolean isWillGenerateTopContestants() {
        return willGenerateTopContestants;
    }

    public void setWillGenerateTopContestants(boolean willGenerateTopContestants) {
        this.willGenerateTopContestants = willGenerateTopContestants;
    }

    public LinkedHashMap<String, Integer> getRemainingContestants() {
        return remainingContestants;
    }

    public void setRemainingContestants(LinkedHashMap<String, Integer> remainingContestants) {
        this.remainingContestants = remainingContestants;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getTweets() {
        return tweets;
    }

    public void setTweets(List<String> tweets) {
        this.tweets = tweets;
    }
}
