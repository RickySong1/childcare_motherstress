package stresstest.ntt.Garmin;

/**
 * Created by Ricky Song on 2018-03-05.
 */

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.github.scribejava.*;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

public class MyOAuthConnect {

    final String Consumer_Key ="fef35759-89eb-4915-acf4-4a991c0414d6";
    final String Consumer_Secret = "BzhUGDBRCmo8IpDMxzPB80AgSDteJFOnSkw";


    public MyOAuthConnect() throws InterruptedException, ExecutionException, IOException {

        final OAuth10aService service = new ServiceBuilder(Consumer_Key).apiSecret(Consumer_Secret).build(MyGarminApi.instance());
        final OAuth1RequestToken requestToken = service.getRequestToken();

        Log.e("step 1",service.getAuthorizationUrl(requestToken));

        final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, "oauthVerifier");

        Log.e("step 2", accessToken.getRawResponse());

        final OAuthRequest request = new OAuthRequest(Verb.GET, "PROTECTED_RESOURCE_URL");

        service.signRequest(accessToken, request);
        final Response response = service.execute(request);
        Log.e("step 3", response.getBody());
    }

}
