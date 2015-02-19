package com.j0llysnowman.logic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by dsumera on 2/18/15.
 */
@Component
public class TwitterService {

    private Twitter twitter;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static final int throttleLimit = 450;

    private static final org.apache.log4j.Logger log = Logger.getLogger(TwitterService.class);

    public TwitterService(
        String consumerKey,
        String consumerSecret,
        String accessToken,
        String accessTokenSecret
    ) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey(consumerKey)
            .setOAuthConsumerSecret(consumerSecret)
            .setOAuthAccessToken(accessToken)
            .setOAuthAccessTokenSecret(accessTokenSecret)
            .setApplicationOnlyAuthEnabled(true);

        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }

    public List<Status> searchTweetsFromLastWeek(String originalQueryString) throws TwitterException {

        log.info("searching last week for query=[" + originalQueryString + "]");

        final Date oneWeekAgo = DateUtils.addWeeks(new Date(), -1);
        String dateString = simpleDateFormat.format(oneWeekAgo);
        List<Status> allResults = new ArrayList<Status>();

        Query query = getQuery(originalQueryString);
        query.setResultType(Query.ResultType.recent);
        query.setCount(100);
        query.setSince(dateString);

        while (true) {
            QueryResult results;

            try {
                results = getQueryResult(query);
            } catch (TwitterException e) {
                if (e.exceededRateLimitation()) {
                    log.info("too many results for "
                            + "query=[" + query.getQuery() + "]"
                            + " wait=" + e.getRateLimitStatus().getSecondsUntilReset()
                            + " returning total=" + allResults.size()
                            + " exception=[" + e.getMessage() + "]"
                    );
                    return allResults;
                } else {
                    throw e;
                }
            }

            if (CollectionUtils.isEmpty(results.getTweets())) {
                break;
            }

            allResults.addAll(results.getTweets());

            log.info("gathered"
                + " runningTotal=" + allResults.size()
                + " tweets for query=[" + query.getQuery() + "]");

            // Set the max limit for the next search.
            Status lastStatus = results.getTweets().get(results.getTweets().size() - 1);
            long maxId = lastStatus.getId();
            query.setMaxId(maxId);
        }

        log.info("returned "
            + "total=" + allResults.size()
            + " for query=[" + query.getQuery() + "]");

        return allResults;
    }

    public List<Status> searchTweets(
        String originalQueryString,
        int count,
        Query.ResultType resultType
    ) throws TwitterException {

        Query query = getQuery(originalQueryString);
        query.setCount(count);
        query.setResultType(resultType);

        QueryResult result;

        try {
            result = getQueryResult(query);
        } catch (TwitterException e) {
            if (e.exceededRateLimitation()) {
                pause(e.getRateLimitStatus().getSecondsUntilReset() + 5);

                return searchTweets(
                    originalQueryString,
                    count,
                    resultType
                );
            } else {
                throw e;
            }
        }

        return result.getTweets();
    }

    private QueryResult getQueryResult(Query query) throws TwitterException {
        QueryResult result;
        try {
            twitter.getOAuth2Token();
        } catch (IllegalStateException e) {
            // Do nothing.
        }

        result = twitter.search(query);
        return result;
    }

    private void pause(int seconds) {
        log.warn("hit throttleLimit=" + throttleLimit
            + " pausing for seconds=" + seconds);

        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            // do nothing!
        }
    }

    private Query getQuery(String originalQueryString) {
        String queryString = originalQueryString + " #theBachelor OR #bachelornation -RT";

        try {
            queryString = URLEncoder.encode(queryString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Do nothing cause i'm lazy.
        }

        return new Query(queryString);
    }
}