package com.j0llysnowman.logic;

import java.util.*;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.j0llysnowman.domain.enums.BachelorContestant;
import com.j0llysnowman.domain.enums.CommonWord;

import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by dsumera on 2/18/15.
 */
@Component
public class WordCloudGenerator {

    @Autowired
    private TwitterService twitterService;

    private static final Logger log = Logger.getLogger(WordCloudGenerator.class);

    public List<JSONObject> generateWeeklyWordCloud(
        String query,
        boolean excludeContestants)
        throws TwitterException {

        List<Status> results = twitterService.searchTweetsFromLastWeek(query);
        LinkedHashMap<String, Integer> wordCloudMap = new LinkedHashMap<String, Integer>();

        for (Status tweet : results) {
            String[] text = tweet.getText().split("\\s+");

            for (String word : text) {
                //                if (word.startsWith("@")) {
                //                    continue;
                //                }

                word = word
                    .toLowerCase()
                    .replaceAll("[.,:;()?!\"\'#-]+", "");

                if (word.length() < 4
                    || query.toLowerCase().contains(word.toLowerCase())
                    || CommonWord.getInstance(word) != null) {
                    continue;
                }

                if (excludeContestants
                    && BachelorContestant.matchesAny(word)) {
                    continue;
                }

                Integer count = wordCloudMap.get(word);
                int incrementedCount = (count != null) ? count + 1 : 1;

                wordCloudMap.put(word, incrementedCount);
            }
        }

        LinkedHashMap<String, Integer> trimmedWordCloudMap = new LinkedHashMap<String, Integer>();

        for (Map.Entry<String, Integer> entry : wordCloudMap.entrySet()) {
            if (entry.getValue() > 5) {
                trimmedWordCloudMap.put(entry.getKey(), entry.getValue());
            }
        }

        trimmedWordCloudMap = sortByValue(trimmedWordCloudMap);

        return convertWordCloudMapToJson(trimmedWordCloudMap);
    }

    private List<JSONObject> convertWordCloudMapToJson(LinkedHashMap<String, Integer> wordCloudMap) {

        List<JSONObject> wordCloudJsonList = new LinkedList<JSONObject>();

        int i = 0;
        for (Map.Entry<String, Integer> entry : wordCloudMap.entrySet()) {
            if (i > 1000) {
                break;
            }

            String text = entry.getKey();
            Integer weight = entry.getValue();

            JSONObject singleObject = new JSONObject();

            try {
                singleObject.put("text", text);
                singleObject.put("weight", weight);
            } catch (JSONException e) {
                log.error(e);
            }

            wordCloudJsonList.add(singleObject);
            i++;
        }

        return wordCloudJsonList;
    }

    private static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {

        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
