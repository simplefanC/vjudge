package judge.remote.provider.tkoj;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.tool.Tools;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-12-11
 */
public class TKOJVerifyUtil {

    public static String getCsrf(DedicatedHttpClient client) {
        String result = client.get("/csrf.php", HttpStatusValidator.SC_OK).getBody();
        return Tools.regFind(result, "value=\"([\\s\\S]*?)\"");
    }

    public static String getCaptcha(DedicatedHttpClient client) throws ClientProtocolException, IOException {
        HttpGet get = new HttpGet("/vcode.php");
        BufferedImage image = client.execute(get, new ResponseHandler<BufferedImage>() {
            @Override
            public BufferedImage handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                FileOutputStream fos = null;
                try {
                    File captchaImg = File.createTempFile("TKOJ", ".png");
                    fos = new FileOutputStream(captchaImg);
                    response.getEntity().writeTo(fos);
                    return ImageIO.read(captchaImg);
                } finally {
                    fos.close();
                }
            }}
        );
        return TKOJCaptchaRecognizer.recognize(image);
    }
}
