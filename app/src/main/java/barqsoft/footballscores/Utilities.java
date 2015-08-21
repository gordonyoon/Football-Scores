package barqsoft.footballscores;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilities {
    public static final int SERIE_A = 401;
    public static final int PREMIER_LEAGUE = 398;
    public static final int CHAMPIONS_LEAGUE = -1;
    public static final int PRIMERA_DIVISION = 399;
    public static final int BUNDESLIGA1 = 394;
    public static final int BUNDESLIGA2 = 395;
    public static final int BUNDESLIGA3 = 403;

    public static boolean isSupportedLeague(int league) {
        return (league == SERIE_A ||
                league == PREMIER_LEAGUE ||
                league == CHAMPIONS_LEAGUE ||
                league == PRIMERA_DIVISION ||
                league == BUNDESLIGA1 ||
                league == BUNDESLIGA2 ||
                league == BUNDESLIGA3);
    }

    public static String getLeague(Context context, int league_num) {
        switch (league_num) {
            case SERIE_A:
                return context.getString(R.string.league_serie_a);
            case PREMIER_LEAGUE:
                return context.getString(R.string.league_premier);
            case CHAMPIONS_LEAGUE:
                return context.getString(R.string.league_uefa_champions);
            case PRIMERA_DIVISION:
                return context.getString(R.string.league_primera_division);
            case BUNDESLIGA1:
                return context.getString(R.string.league_bundesliga);
            case BUNDESLIGA2:
                return context.getString(R.string.league_bundesliga);
            case BUNDESLIGA3:
                return context.getString(R.string.league_bundesliga);
        }
        return null;
    }

    public static String getMatchDay(Context context, int match_day, int league_num) {
        if (league_num == CHAMPIONS_LEAGUE) {
            if (match_day <= 6) {
                if (isRtl(context)) {
                    return "6 : " + context.getString(R.string.matchday) + ", " + context.getString(R.string.group_stages);

                } else {
                    return context.getString(R.string.group_stages) + ", " + context.getString(R.string.matchday) + " : 6";
                }
            } else if (match_day == 7 || match_day == 8) {
                return context.getString(R.string.first_knockout_round);
            } else if (match_day == 9 || match_day == 10) {
                return context.getString(R.string.quarter_final);
            } else if (match_day == 11 || match_day == 12) {
                return context.getString(R.string.semi_final);
            } else {
                return context.getString(R.string.finals);
            }
        } else {
            if (isRtl(context)) {
                return String.valueOf(match_day) + " : " + context.getString(R.string.matchday);
            } else {
                return context.getString(R.string.matchday) + " : " + String.valueOf(match_day);
            }
        }
    }

    private static boolean isRtl(Context context) {
        Configuration config = context.getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            return true;
        }
        return false;
    }

    public static String getScores(int home_goals, int awaygoals) {
        if (home_goals < 0 || awaygoals < 0) {
            return " - ";
        } else {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName(Context context, String teamname) {
        if (teamname == null) {
            return R.drawable.no_icon;
        }

        if (teamname.equals(context.getString(R.string.team_arsenal_london_fc))) {
            return R.drawable.arsenal;
        } else if (teamname.equals(context.getString(R.string.team_manchester_united_fc))) {
            return R.drawable.manchester_united;
        } else if (teamname.equals(context.getString(R.string.team_swansea_city))) {
            return R.drawable.swansea_city_afc;
        } else if (teamname.equals(context.getString(R.string.team_leicester_city))) {
            return R.drawable.leicester_city_fc_hd_logo;
        } else if (teamname.equals(context.getString(R.string.team_everton_fc))) {
            return R.drawable.everton_fc_logo1;
        } else if (teamname.equals(context.getString(R.string.team_west_ham_united_fc))) {
            return R.drawable.west_ham;
        } else if (teamname.equals(context.getString(R.string.team_tottenham_hotspur_fc))) {
            return R.drawable.tottenham_hotspur;
        } else if (teamname.equals(context.getString(R.string.team_west_bromwich_albion))) {
            return R.drawable.west_bromwich_albion_hd_logo;
        } else if (teamname.equals(context.getString(R.string.team_sunderland_afc))) {
            return R.drawable.sunderland;
        } else if (teamname.equals(context.getString(R.string.team_stoke_city_fc))) {
            return R.drawable.stoke_city;
        } else {
            return R.drawable.no_icon;
        }
    }

    public static String getDate(int dayOffset) {
        Date fragmentdate = new Date(System.currentTimeMillis() + (dayOffset * 86400000));
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        return mformat.format(fragmentdate);
    }
}
