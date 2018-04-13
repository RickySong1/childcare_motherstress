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
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Locale;


import stresstest.ntt.kaist.childcare.MainActivity;
import stresstest.ntt.mymanager.MySocketManager;

import static stresstest.ntt.kaist.childcare.MainActivity.USER_ID;

public class MyOAuthConnect {

    private int timeOffset = 32400;
    private String PROTECTED_RESOURCE_URL = "https://healthapi.garmin.com/wellness-api/rest/stressDetails?uploadStartTimeInSeconds=1521363600&uploadEndTimeInSeconds=1521435360";
    private String DAILIES_URL =              "https://healthapi.garmin.com/wellness-api/rest/dailies?uploadStartTimeInSeconds=1523718000&uploadEndTimeInSeconds=1523761200";

    //Momicontest
    //consumer
    //587ae996-5042-4936-acc3-9c8600054f21
    //consumer secret
    //5wk39bzuWxUPwtrSTWntQWRFBYZo7doKp3p
    // uat
    //22ea058f-5252-428b-8d6c-f1de830f9ff0
    // user secret
    //1h1ac9mzeO8KmlmBwhr3qnioL3ICN4dO5go

    final String Consumer_Key ="fef35759-89eb-4915-acf4-4a991c0414d6";
    final String Consumer_Secret = "BzhUGDBRCmo8IpDMxzPB80AgSDteJFOnSkw";

    String User_Token = "862f45ab-c3d6-4be1-83bc-9064a14eeb76"; // Mine
    String User_Secret = "dbZOJJC4PVRsDU2ALS8rFuc5TBOretBdYZi"; // Mine

    String this_date;
    public MyOAuthConnect(String _this ) throws Exception {
        if( MainActivity.USER_ID.split("_")[0].equals("USER01")==true){
            User_Token = "17d307ca-c3c3-406c-b29f-a03030ee9c01";
            User_Secret = "Rgpwi2WcEHgelQTSFEb7OPorkUBS0KpVSLx";
        }else if( MainActivity.USER_ID.split("_")[0].equals("USER02")==true){
            User_Token = "e514b6ab-5c98-4393-af6e-071636491540";
            User_Secret = "seGObe0dLD86D3JaPaoa1HQpbCfclMXrgyU";
        }else if( MainActivity.USER_ID.split("_")[0].equals("USER03")==true){
            User_Token = "262e4483-ee4f-4428-901b-a387f95d19cd";
            User_Secret = "BABlHnqKiW9Y7mQou7gH8oIg5Fo3KmtBi5V";
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
            User_Token = "862f45ab-c3d6-4be1-83bc-9064a14eeb76";
            User_Secret = "dbZOJJC4PVRsDU2ALS8rFuc5TBOretBdYZi";
        }else if( MainActivity.USER_ID.split("_")[0].equals("TEST02")==true){
            User_Token = "9415eb28-e42c-4361-a91a-261bc9100dc2";
            User_Secret = "CCJHrWbbLPA3eZRoUTTLBNHkSY9qrs5yGpn";
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

    public int[] stressGet(String this_time){

        MySocketManager socketM = new MySocketManager(USER_ID);

        SimpleDateFormat save_date = new SimpleDateFormat("yyyyMMdda", Locale.US);
        String stress_data = socketM.getDataFromServer(MySocketManager.SOCKET_MSG.GET_STRESS_DATA, this_time);

        if(stress_data.contains("NO")){
            int [] stress_value2 = new int[240];
            return stress_value2;
        }

        Log.e("stress_data",this_time+" : " +stress_data);
        stress_data = stress_data.substring(1,stress_data.length()-1);

        int [] stress_value = new int[240];
        for(int i=0 ; i <stress_data.split(",").length ; i++) {
            String time = stress_data.split(",")[i].split("\"")[1];
            String value = stress_data.split(",")[i].split(":")[1];

            /*
            if(save_date.format(NOW_TIME).contains("AM") && Integer.parseInt(time) >=0  && Integer.parseInt(time) <43200 ){
                stress_value[ Integer.parseInt(time) / 180  ] = Integer.parseInt(value);
            }else if(save_date.format(NOW_TIME).contains("PM") && Integer.parseInt(time) >=43200)  {
                stress_value[ (Integer.parseInt(time) / 180)  - 240  ] = Integer.parseInt(value);
            }
            */
            if(this_time.contains("AM") && Integer.parseInt(time) >=0  && Integer.parseInt(time) <43200 ){
                stress_value[ Integer.parseInt(time) / 180  ] = Integer.parseInt(value);
            }else if(this_time.contains("PM") && Integer.parseInt(time) >=43200)  {
                stress_value[ (Integer.parseInt(time) / 180)  - 240  ] = Integer.parseInt(value);
            }
        }

        return stress_value;
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
