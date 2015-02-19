package com.j0llysnowman.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.j0llysnowman.domain.dto.BachelorDto;
import com.j0llysnowman.domain.dto.ContestantWordCloudDto;
import com.j0llysnowman.domain.enums.BachelorContestant;
import com.j0llysnowman.logic.WordCloudGenerator;
import com.j0llysnowman.logic.TwitterService;

import twitter4j.Query;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by dsumera on 2/18/15.
 */
@Controller
public class BachelorController {

    private static final String pageName = "../jsp/bachelorHome";

    private static final Logger log = Logger.getLogger(BachelorController.class);

    @Autowired
    private TwitterService twitterService;

    @Autowired
    private WordCloudGenerator wordCloudGenerator;

    private List<Integer> countValues;

    private List<String> tweetTypes;

    public BachelorController() {
        countValues = new ArrayList<Integer>(100);
        for (int i = 100; i > 1; i--) {
            countValues.add(i);
        }

        tweetTypes = Arrays.asList(
            Query.ResultType.popular.name(),
            Query.ResultType.recent.name(),
            Query.ResultType.mixed.name()
        );
    }

    @ModelAttribute(value = "countValues")
    public List<Integer> getCountValues() {
        return countValues;
    }

    @ModelAttribute(value = "tweetTypes")
    public List<String> getTweetTypes() {
        return tweetTypes;
    }

    @RequestMapping(value = "/bachelor", method = RequestMethod.GET)
    public String get(
        HttpServletRequest request,
        HttpServletResponse response,
        Model model
    ) {

        model.addAttribute("bachelorDto", new BachelorDto());

        return pageName;
    }

    @RequestMapping(value = "/bachelor", method = RequestMethod.POST)
    public String post(
        HttpServletRequest request,
        HttpServletResponse response,
        Model model,
        BachelorDto dto
    ) throws
        JSONException,
        TwitterException {

        if (StringUtils.isNotBlank(dto.getQuery())) {
            // Populate the dto with a snapshot of individual tweets.
            processIndividualTweets(dto);
        }

        if (dto.isWillGenerateWordCloud()) {
            // Generate a basic word cloud
            dto.setWordCloud(
                wordCloudGenerator.generateWeeklyWordCloud(
                    dto.getQuery(),
                    false
                ));
        }

        if (dto.isWillGenerateTopContestants()) {
            // Figure out who's popular this week.
            generateWeeklyTopContestants(dto);
            //            dto.setContestantWordCloud(
            //                generateContestantWordClouds(
            //                    dto.getQuery()
            //                ));
        }

        model.addAttribute("bachelorDto", dto);

        return pageName;
    }

    private List<ContestantWordCloudDto> generateContestantWordClouds(final String originalQuery)
        throws JSONException, TwitterException {

        final List<ContestantWordCloudDto> contestantWordClouds = new ArrayList<ContestantWordCloudDto>();

        for (final BachelorContestant contestant : BachelorContestant.values()) {

            Thread contestantThread = new Thread() {

                @Override
                public void run() {
                    String query = originalQuery + " " + contestant.name();

                    List<JSONObject> wordCloud = null;
                    try {
                        wordCloud = wordCloudGenerator.generateWeeklyWordCloud(
                            query,
                            true
                        );
                    } catch (TwitterException e) {
                        throw new RuntimeException(e);
                    }

                    ContestantWordCloudDto contestantWordCloud = new ContestantWordCloudDto();

                    contestantWordCloud.setContestant(contestant.name());
                    contestantWordCloud.setWordCloud(wordCloud);

                    contestantWordClouds.add(contestantWordCloud);

                    Thread.currentThread().interrupt();
                }
            };

            contestantThread.start();
        }

        return contestantWordClouds;
    }

    private void generateWeeklyTopContestants(BachelorDto dto) throws TwitterException {

        final LinkedHashMap<String, Integer> contestants = new LinkedHashMap<String, Integer>();
        final Set<Thread> threads = new HashSet<Thread>();

        for (final BachelorContestant bachelorContestant : BachelorContestant.values()) {

            final Thread contestantThread = new Thread() {

                @Override
                public void run() {
                    List<Status> results = null;
                    try {
                        results = twitterService.searchTweetsFromLastWeek(bachelorContestant.name());
                    } catch (TwitterException e) {
                        throw new RuntimeException(e);
                    }
                    contestants.put(bachelorContestant.name(), results.size());

                    threads.add(this);

                    log.info("added "
                        + "numTweets=" + results.size()
                        + " for bachelorette=" + bachelorContestant.name());

                    //                    Thread.currentThread().interrupt();
                }
            };

            contestantThread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        LinkedHashMap<String, Integer> sortedContestants = sortByValue(contestants);

        dto.setRemainingContestants(sortedContestants);
    }

    private void processIndividualTweets(BachelorDto dto) throws TwitterException {

        List<Status> results = twitterService.searchTweets(
            dto.getQuery(),
            dto.getCount(),
            convert(dto.getTweetType())
        );

        for (Status tweet : results) {
            dto.getTweets().add(tweet.getText());
        }

        log.info("added"
            + " numTweets=" + dto.getTweets().size()
            + " for query=" + dto.getQuery());
    }

    private Query.ResultType convert(String type) {

        for (Query.ResultType resultType : Query.ResultType.values()) {
            if (resultType.name().equalsIgnoreCase(type)) {
                return resultType;
            }
        }

        return null;
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
