package judge.remote.provider.mxt;

import judge.BaseJunitTest;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.DedicatedHttpClientFactory;
import judge.remote.RemoteOjInfo;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 对TK题库的验证工具进行测试
 *
 * @author zzzz76
 */
public class MXTVerifyUtilTest extends BaseJunitTest {

    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    private RemoteOjInfo getOjInfo() {
        return MXTInfo.INFO;
    }

    private HttpContext getNewContext() {
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        return context;
    }

    @Test
    public void testGetCaptcha() throws Exception{
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, getNewContext(), getOjInfo().defaultChaset);
        String res = getCaptcha(client);
        System.err.println(res);
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
