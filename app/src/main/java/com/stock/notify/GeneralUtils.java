package com.stock.notify;

import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import yahoofinance.YahooFinance;

public class GeneralUtils {

    private static StringBuffer stat;
    String stockFile = "Data.txt";
    String stocksToBuy = "";

    protected List<String> getWatchList(Context context, String fileName) {
        List<String> list = new ArrayList<String>();
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            InputStream inputStream = context.openFileInput(fileName);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {
                    list.add(receiveString);
                }

                Collections.sort(list);
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(String l : list) {
            System.out.println(l);
        }

        System.out.println("DAL, PYPL, ATVI, SHOP, NFLX, GE, F, GM, GPRO, GWPH, ACBFF, APHQF, CRUS, MNST");
        return list;
    }

    protected boolean hasNewRecommendations(Context context) throws Exception {
        boolean flag = false;
        try {
            for (String stockName : getWatchList(context, "Data.txt")) {
                JSONObject json = getJson(stockName);
                JSONObject json2 = getJson2(stockName);
                Double current;
                try {
                    current = Double.parseDouble(json2.get("03. Latest Price").toString());
                } catch (Exception e) {
                    current = Double.parseDouble(json.get("Open").toString());
                }
                Double yearLow = Double.parseDouble(json.get("YearLow").toString());

                if (current <= yearLow) {
                    System.out.println(stockName + "-> buy");
                    stocksToBuy = stocksToBuy + getStat(stockName);
                    flag = true;
                } else {
                    System.out.println(stockName + "-> don't buy");
                }
            }

            if (flag) {
                updateCache(context, new ArrayList<String>(Arrays.asList(stocksToBuy.split(" "))), "stocksToBuy");
            }
        } catch (Exception e) {
            System.out.println("ERROR -> don't buy");
        }
        return flag;
    }

    public String getStat(String stockName) {
        for (int i = 0; i < 3; i++) {
            try {
                JSONObject json = getJson(stockName);
                JSONObject json2 = getJson2(stockName);
                stat = new StringBuffer();
                stat.append("<br/>  " + json.get("Name") + " (" + stockName + ")<br/>");

                if (!json2.get("03. Latest Price").equals("0.0000")) {
                    stat.append("<br/>  Current = " + json2.get("03. Latest Price").toString().substring(0, json2.get("03. Latest Price").toString().length() - 2));
                } else {
                    stat.append("<br/>  Open = " + json.get("Open"));
                }
                if (!json.get("PreviousClose").equals(null))
                    stat.append("<br/>  Prev Close = " + json.get("PreviousClose"));

                if (!json.get("DaysRange").equals(null))
                    stat.append("<br/>  Day's Range = " + json.get("DaysRange"));

                if (!json.get("YearRange").equals(null))
                    stat.append("<br/>  Year Range = " + json.get("YearRange"));

                if (!json.get("FiftydayMovingAverage").equals(null))
                    stat.append("<br/>  50 Day Moving Avg  = " + json.get("FiftydayMovingAverage"));

                if (!json.get("TwoHundreddayMovingAverage").equals(null))
                    stat.append("<br/>  200 Day Moving Avg = " + json.get("TwoHundreddayMovingAverage"));


                if (StringUtils.hasText(getCommunityRating(stockName)))
                    stat.append("<br/>  Nasdaq = " + getCommunityRating(stockName));

                stat.append("<br/><br/>");
                break;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stat.toString();

    }

    JSONObject getJson(String stockName) throws Exception {
        JSONObject json = new JSONObject();
        for (int i = 1; i <= 3; i++) {
            try {
                ThreadInitializer();
                String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22"
                        + stockName + "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
                System.out.println(url);
                InputStream is = new URL(url).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                StringBuilder sb = new StringBuilder();
                int cp;
                while ((cp = rd.read()) != -1) {
                    sb.append((char) cp);
                }
                String jsonText = sb.toString();
                json = new JSONObject(jsonText).getJSONObject("query").getJSONObject("results").getJSONObject("quote");
                break;
            } catch (Exception e) {
                System.out.println("Error occurred... Retry #" + i);
            }
        }
        return json;
    }

    JSONObject getJson2(String stockName) throws Exception {
        JSONObject json = new JSONObject();
        for (int i = 1; i <= 3; i++) {
            try {
                ThreadInitializer();
                String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + stockName + "&apikey=4U0MFL7Z2QRF8K43";
                System.out.println(url);
                InputStream is = new URL(url).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                StringBuilder sb = new StringBuilder();
                int cp;
                while ((cp = rd.read()) != -1) {
                    sb.append((char) cp);
                }
                String jsonText = sb.toString();
                json = new JSONObject(jsonText).getJSONObject("Realtime Global Securities Quote");
                break;
            } catch (Exception e) {
                System.out.println("Error occurred... Retry #" + i);
            }
        }
        System.out.println(json);
        return json;
    }

    protected List<String> getCachedTweets(Context context, String fileName) {
        List<String> list = new ArrayList<String>();
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            InputStream inputStream = context.openFileInput(fileName);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {
                    list.add(receiveString);
                    list.add("\n");
                }

                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    protected List<String> getLiveTweets(Context context, String stockName) {
        List<String> subList = new ArrayList<String>();
        try {
            ThreadInitializer();

            Properties properties = new Properties();
            InputStream input = context.getAssets().open("config.properties");
            properties.load(input);

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(properties.getProperty("MY_PRIVATE_TWITTER_KEY"))
                    .setOAuthConsumerSecret(properties.getProperty("MY_PRIVATE_TWITTER_SECRET"))
                    .setOAuthAccessToken(properties.getProperty("MY_PRIVATE_TWITTER_TOKEN"))
                    .setOAuthAccessTokenSecret(properties.getProperty("MY_PRIVATE_TWITTER_TOKEN_SECRET"));

            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();
            int remaining = twitter.getRateLimitStatus().get("/search/tweets").getRemaining();
            //int remaining = 0;
            System.out.println("Twitter API remaining calls = " + remaining);

            //Get new live tweets
            if (remaining > 0) {
                Query query = new Query("$" + stockName);
                query.setCount(30);
                QueryResult result = twitter.search(query);
                for (Status status : result.getTweets()) {
                    if (!status.isRetweet()) {
                        subList.add("@" + status.getUser().getScreenName() + ":" + status.getText() + "\n\n\n\n");
                    }
                }
                updateCache(context, subList, stockName);
            }

            //Read old tweets
            else {
                return getCachedTweets(context, stockName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return subList;
    }

    protected void updateCache(Context context, List<String> list, String fileName) {
        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            for (String text : list) {
                outputStream.write(text.getBytes());
                outputStream.write("\n\n\n\n".getBytes());
            }
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ThreadInitializer() {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    protected boolean isValidStock(Context context, String input) {
        boolean valid = true;
        try {
            ThreadInitializer();
            if (!StringUtils.hasText(input) || YahooFinance.get(input).getName() == null) {
                Toast.makeText(context, "Invalid stock name", Toast.LENGTH_SHORT).show();
                valid = false;
            }

            if (getWatchList(context, stockFile).contains(input)) {
                Toast.makeText(context, "You're already following this stock", Toast.LENGTH_SHORT).show();
                valid = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            valid = false;
        }

        return valid;
    }

    protected String getNotificationMessage(Context context) throws Exception {
        String str = "<b><u>Today's Stock Recommendations</u></b><br/><br/>" + getCachedTweets(context, "stocksToBuy");
        return str.replaceAll(",", " ").replaceAll("\\[", "").replaceAll("\\]", "");
    }

    public String getCommunityRating(String stockName) {
        try {
            URL url = new URL("https://community.nasdaq.com/community-ratings.aspx?stockticker=" + stockName);
            BufferedReader reader = null;
            StringBuilder builder = new StringBuilder();
            try {
                reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                for (String line; (line = reader.readLine()) != null; ) {
                    builder.append(line.trim());
                }
            } finally {
                if (reader != null)
                    try {
                        reader.close();
                    } catch (IOException logOrIgnore) {
                    }
            }

            String start = "<span id=\"ctl00_ContentPlaceHolder_commRating\" class=\"bullRating\">";
            String end = "</span>";
            String part = builder.substring(builder.indexOf(start) + start.length());
            String question = part.substring(0, part.indexOf(end));
            if (question.length() < 10) {
                return question;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
