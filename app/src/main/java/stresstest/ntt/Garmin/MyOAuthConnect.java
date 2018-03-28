package stresstest.ntt.Garmin;

/**
 * Created by Ricky Song on 2018-03-05.
 */

import android.util.Log;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


import stresstest.ntt.kaist.childcare.MainActivity;
import stresstest.ntt.mymanager.MyFileManager;
import stresstest.ntt.mymanager.MySocketManager;

import static stresstest.ntt.kaist.childcare.MainActivity.USER_ID;

public class MyOAuthConnect {

    private int timeOffset = 32400;
    private String PROTECTED_RESOURCE_URL = "https://healthapi.garmin.com/wellness-api/rest/stressDetails?uploadStartTimeInSeconds=1521363600&uploadEndTimeInSeconds=1521435360";
    private String DAILIES_URL =              "https://healthapi.garmin.com/wellness-api/rest/dailies?uploadStartTimeInSeconds=1523718000&uploadEndTimeInSeconds=1523761200";

    final String Consumer_Key ="fef35759-89eb-4915-acf4-4a991c0414d6";
    final String Consumer_Secret = "BzhUGDBRCmo8IpDMxzPB80AgSDteJFOnSkw";

    String User_Token = "500c5606-46cc-4ffb-a436-1ad508d42cf0"; // Mine
    String User_Secret = "Vi9YHR99yUVvxdqe03CDIayVo3S4axhDIJV"; // Mine

    String this_date;
    public MyOAuthConnect(String _this ) throws Exception {
        if( MainActivity.USER_ID.split("_")[0].equals("USER01")==true){
            User_Token = "17d307ca-c3c3-406c-b29f-a03030ee9c01";
            User_Secret = "Rgpwi2WcEHgelQTSFEb7OPorkUBS0KpVSLx";
        }else if( MainActivity.USER_ID.split("_")[0].equals("USER02")==true){
            User_Token = "f6c74606-9a4c-461e-804f-f2cc324ff4bc";
            User_Secret = "FFMFW5C21xFqqpczeWfQfmrmfiKkHzImFh3";
        }else if( MainActivity.USER_ID.split("_")[0].equals("USER03")==true){
            User_Token = "f6bdd9f2-faa1-421b-93f2-07d7a74707a3";
            User_Secret = "df8Jc9m1GCPqYQSee4uq2pkbYueICRZoXlR";
        }else if( MainActivity.USER_ID.split("_")[0].equals("USER04")==true){
            User_Token = "090f1a3b-9637-473d-a590-79a26b51043d";
            User_Secret = "Kgt3RZfC4i4QXUIW1LWML2LkjqPROxPzyA1";
        }else if( MainActivity.USER_ID.split("_")[0].equals("USER05")==true){
            User_Token = "f6c39caf-b739-48b7-9426-a5b284f63625";
            User_Secret = "OdkSF0kJ8UhLTiYBBy5YlFOCdRkxOg80XVB";
        }else if( MainActivity.USER_ID.split("_")[0].equals("USER06")==true){
            User_Token = "b0000746-1d8f-4579-8f00-80357aeb25ed";
            User_Secret = "2njWNyvQ9aTc2aomSjupV7G5Q3PTcTQgT2m";
        }else if( MainActivity.USER_ID.split("_")[0].equals("USER07")==true){
            User_Token = "ac524007-1cf2-41aa-97c7-fd817c0bee8e";
            User_Secret = "48S7cvY2RO3ikIIQ7C88E7XWUdPIBT1RpGl";
        }else if( MainActivity.USER_ID.split("_")[0].equals("USER08")==true){
            User_Token = "cb6a85f0-22d5-4485-8ba2-ab60b1734304";
            User_Secret = "NcyzjaqcfGsyHky1D18RKzbH5ZWlZSnMhjP";
        }else if( MainActivity.USER_ID.split("_")[0].equals("USER09")==true){
            User_Token = "f7abb4f7-9c40-4e86-b90f-9b3963e20020";
            User_Secret = "nIn7Fd4svydQwH77j5gq3H18G8OKItC7pau";
        }else if( MainActivity.USER_ID.split("_")[0].equals("TEST01")==true){
            User_Token = "500c5606-46cc-4ffb-a436-1ad508d42cf0";
            User_Secret = "Vi9YHR99yUVvxdqe03CDIayVo3S4axhDIJV";
        }else if( MainActivity.USER_ID.split("_")[0].equals("TEST02")==true){
            User_Token = "0d5c10c6-334b-417a-bc91-98a623e357e2";
            User_Secret = "7hT7F0jNGmpdw2iKDY6nizqi0tyuNvRUPa5";
        }

        this_date = _this;
        /*
        new Thread(new Runnable() {
            @Override public void run()
            {
                try {
                    sendGet();
                } catch (Exception e) {
                    Log.e("MyOAuthConnect",e.toString());
                }
            }
        }).start();
        */
    }

    public int[] sendGet(String start , String end) throws Exception {
        final OAuth10aService service = new ServiceBuilder(Consumer_Key).apiSecret(Consumer_Secret).debug().build(MyGarminApi.instance());

        SimpleDateFormat real_time = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        SimpleDateFormat target_day = new SimpleDateFormat("yyyy-MM-dd");

        //final OAuth10aService service = new ServiceBuilder(Consumer_Key).apiKey(Consumer_Key).apiSecret(Consumer_Secret).debug().build(TwitterApi.instance());
        //final OAuth1RequestToken requestToken = service.getRequestToken();
        //Log.e("step 1",service.getAuthorizationUrl(requestToken));
        //final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, "oauthVerifier");
        OAuth1AccessToken accessToken = new OAuth1AccessToken(User_Token , User_Secret);

        int [] values;
        values = new int[240];

        Log.e("THIS_TIME",this_date);

        // I'll search 2 DAY's upload record
        for(int day=0 ; day < 2 ; day++){
            String start1 = Integer.toString(Integer.parseInt(start) - timeOffset + (day*86400)  );
            String start2 = Integer.toString(Integer.parseInt(end) - timeOffset + (day*86400) );

            String url = "https://healthapi.garmin.com/wellness-api/rest/stressDetails?uploadStartTimeInSeconds="+start1+"&uploadEndTimeInSeconds="+start2;
            final OAuthRequest request = new OAuthRequest(Verb.GET, url );

            Log.e("StressQueryTime["+Integer.toString(day)+"]", start1+" : " + start2);
            //final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL );
            //final OAuthRequest request = new OAuthRequest(Verb.GET, DAILIES_URL);
            service.signRequest(accessToken, request);
            final Response response = service.execute(request);
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getStream()));
            String inputLine;
            StringBuffer responseJson = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                responseJson.append(inputLine);
            }
            Log.e("Json",responseJson.toString());
            if(responseJson != null){
                JsonArray array = Json.parse(responseJson.toString()).asArray();
                for(int i=0 ; i < array.size() ; i++){
                    String garmin_date = array.get(i).asObject().getString("calendarDate","empty");
                    if( garmin_date.equals("empty") == false){
                        String target_date = garmin_date.split("-")[0] + garmin_date.split("-")[1] + garmin_date.split("-")[2];
                        if( target_date.equals(this_date.substring(0,8)) == true ) {
                            JsonObject obj = array.get(i).asObject();
                            JsonObject a = obj.get("timeOffsetStressLevelValues").asObject();
                            MySocketManager socketM = new MySocketManager(USER_ID);
                            socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_STRESS_DATA , this_date , 0 , a.toString());
                            Log.e("CalendarDate" , garmin_date);
                            Log.e("StressValue" , obj.toString());
                            int start_value;
                            if(this_date.contains("AM")){
                                start_value = 0;
                            }else{
                                start_value = 240;
                            }
                            // Get 12 hours data
                            for(int j=start_value ; j< start_value+240 ; j++){
                                JsonValue value = a.get(Integer.toString(j * 180));
                                if(value == null){
                                    values[j-start_value] = -1;
                                }
                                else{
                                    values[j-start_value] = value.asInt();
                                }
                            }
                        }
                    }
                }
            }
        }
        return values;
    }
}
