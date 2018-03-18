package stresstest.ntt.Garmin;

/**
 * Created by Ricky Song on 2018-03-05.
 */

import android.util.Log;

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
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class MyOAuthConnect {

    private static final String PROTECTED_RESOURCE_URL = "https://healthapi.garmin.com/wellness-api/rest/stressDetails?uploadStartTimeInSeconds=1520348400&uploadEndTimeInSeconds=1520402702";

    final String Consumer_Key ="fef35759-89eb-4915-acf4-4a991c0414d6";
    final String Consumer_Secret = "BzhUGDBRCmo8IpDMxzPB80AgSDteJFOnSkw";

    final String User_Token = "500c5606-46cc-4ffb-a436-1ad508d42cf0";
    final String User_Secret = "Vi9YHR99yUVvxdqe03CDIayVo3S4axhDIJV";

    public MyOAuthConnect( ) throws Exception {
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
    }

    public int[] sendGet() throws Exception {

        final OAuth10aService service = new ServiceBuilder(Consumer_Key).apiSecret(Consumer_Secret).debug().build(MyGarminApi.instance());
        //final OAuth10aService service = new ServiceBuilder(Consumer_Key).apiKey(Consumer_Key).apiSecret(Consumer_Secret).debug().build(TwitterApi.instance());
        //final OAuth1RequestToken requestToken = service.getRequestToken();
        //Log.e("step 1",service.getAuthorizationUrl(requestToken));
        //final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, "oauthVerifier");

        OAuth1AccessToken accessToken = new OAuth1AccessToken(User_Token , User_Secret);
        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL );

        service.signRequest(accessToken, request);
        final Response response = service.execute(request);

        BufferedReader in = new BufferedReader(new InputStreamReader(response.getStream()));
        String inputLine;
        StringBuffer responseJson = new StringBuffer();
        StringBuffer sbRet = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            responseJson.append(inputLine);
        }

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(responseJson.toString());

        int [] values;
        values = new int[240];

        for(int i=0; i < 1;i++){
            JSONObject jsonObj = (JSONObject)jsonArray.get(i);
            JSONObject temp = (JSONObject) jsonObj.get("timeOffsetStressLevelValues");

            Log.e("aa",responseJson.toString());
            Log.e("readline",temp.toJSONString());

            // GET 12 hours data for every 3 minutes.  240 * 180 (3m) = 43200s (12hours)
            for(int j=0 ; j< 240 ; j++){

                String value = temp.get(Integer.toString(j * 180)).toString();
                Log.e("dd",value);

                if(value == null){
                    values[j] = -1;
                }
                else{
                    values[j] = Integer.parseInt(value);
                }
            }
        }

        return values;
    }
}
