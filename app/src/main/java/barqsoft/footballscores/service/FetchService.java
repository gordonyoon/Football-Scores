package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class FetchService extends IntentService {
    public static final String LOG_TAG = "FetchService";

    public FetchService() {
        super("FetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        saveNewData("n2");
        saveNewData("p2");
    }

    private void saveNewData(String timeFrame) {
        ArrayList values = null;
        try {
            String JSON_data = downloadData(timeFrame);
            if (JSON_data != null) {
                // use dummy data if there are no matches (i.e., off season)
                JSONArray matches = new JSONObject(JSON_data).getJSONArray("fixtures");
                if (matches.length() == 0) {
                    values = processJSONdata(getString(R.string.dummy_data), false);
                } else {
                    values = processJSONdata(JSON_data, true);
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        if (values != null) {
            ContentValues[] valuesArr = new ContentValues[values.size()];
            values.toArray(valuesArr);
            int numInserted = getApplicationContext().getContentResolver()
                    .bulkInsert(DatabaseContract.BASE_CONTENT_URI, valuesArr);
        }
    }

    private String downloadData(String timeFrame) throws IOException {
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures";
        final String QUERY_TIME_FRAME = "timeFrame";

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();

        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection)fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token", "fd8e01efff1f4684bbb7d758fbbc6045");
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() > 0) {
                    JSON_data = buffer.toString();
                }
            }
        } catch (MalformedURLException | ProtocolException e) {
            e.printStackTrace();
        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return JSON_data;
    }

    private ArrayList processJSONdata(String JSONdata, boolean isReal) {
        //JSON data
        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String league = null;
        String date = null;
        String time = null;
        String home = null;
        String away = null;
        String homeGoals = null;
        String awayGoals = null;
        String matchId = null;
        String matchDay = null;

        ArrayList<ContentValues> values = null;
        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);

            values = new ArrayList<>(matches.length());
            for (int i = 0; i < matches.length(); i++) {
                JSONObject match_data = matches.getJSONObject(i);
                league = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                league = league.replace(SEASON_LINK, "");
                if (Utilities.isSupportedLeague(Integer.parseInt(league))) {
                    matchId = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    matchId = matchId.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        matchId = matchId + Integer.toString(i);
                    }

                    date = match_data.getString(MATCH_DATE);
                    time = date.substring(date.indexOf("T") + 1, date.indexOf("Z"));
                    date = date.substring(0, date.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parseddate = match_date.parse(date + time);
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        date = new_date.format(parseddate);
                        time = date.substring(date.indexOf(":") + 1);
                        date = date.substring(0, date.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                            date = mformat.format(fragmentdate);
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    home = match_data.getString(HOME_TEAM);
                    away = match_data.getString(AWAY_TEAM);
                    homeGoals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    awayGoals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    matchDay = match_data.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID, matchId);
                    match_values.put(DatabaseContract.scores_table.DATE_COL, date);
                    match_values.put(DatabaseContract.scores_table.TIME_COL, time);
                    match_values.put(DatabaseContract.scores_table.HOME_COL, home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL, away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, homeGoals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, awayGoals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL, league);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY, matchDay);

                    values.add(match_values);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return values;
    }
}

