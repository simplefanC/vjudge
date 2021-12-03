package judge.remote.provider.mxt;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.RetentiveLoginer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-11-22
 */
@Component
public class MXTLoginer extends RetentiveLoginer {

    @Override
    public RemoteOjInfo getOjInfo() {
        return MXTInfo.INFO;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) throws Exception {
        client.get("/login");
        if (client.execute(new HttpPost("/islogin"), HttpStatusValidator.SC_OK).getBody().contains("1")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "username", account.getAccountId(),
                "password", account.getPassword(),
                "valiCode", getCaptcha(client));

        HttpPost post = new HttpPost("/login");
        post.setEntity(entity);
        client.execute(post, HttpStatusValidator.SC_MOVED_TEMPORARILY);
    }

    private String getCaptcha(DedicatedHttpClient client) throws ClientProtocolException, IOException {
        HttpGet get = new HttpGet("/code");
        File gif = client.execute(get, new ResponseHandler<File>() {
            @Override
            public File handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                FileOutputStream fos = null;
                try {
                    File captchaGif = File.createTempFile("mxt", ".gif");
                    fos = new FileOutputStream(captchaGif);
                    response.getEntity().writeTo(fos);
                    return captchaGif;
                } finally {
                    fos.close();
                }
            }}
        );
        return MXTCaptchaRecognizer.recognize(gif);
    }

}