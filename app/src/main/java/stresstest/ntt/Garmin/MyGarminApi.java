package stresstest.ntt.Garmin;


import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;

/**
 * Created by Ricky Song on 2018-03-05.
 */

public class MyGarminApi extends DefaultApi10a {
    private static final String AUTHORIZE_URL = "http://jimbo.com/oauth/authorize?token=%s";

    protected MyGarminApi() {
    }

    private static class InstanceHolder {
        private static final MyGarminApi INSTANCE = new MyGarminApi();
    }

    public static MyGarminApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint(){
        return "https://connectapi.garmin.com/oauth-service/oauth/access_token";
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken oAuth1RequestToken) {
        return null;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "https://connectapi.garmin.com/oauth-service/oauth/request_token";
    }

}
