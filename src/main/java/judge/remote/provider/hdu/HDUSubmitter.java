package judge.remote.provider.hdu;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.ComplexSubmitter;
import judge.remote.submitter.SubmissionInfo;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class HDUSubmitter extends ComplexSubmitter {

    @Override
    public RemoteOjInfo getOjInfo() {
        return HDUInfo.INFO;
    }

    @Override
    protected boolean needLogin() {
        return true;
    }

    @Override
    protected long getSubmitReceiptDelay() {
        return 10000;
    }

    @Override
    protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) {
        String html = client.get("/status.php?user=" + info.remoteAccountId + "&pid=" + info.remoteProblemId).getBody();
        Matcher matcher = Pattern.compile("<td height=22px>(\\d+)").matcher(html);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        HttpEntity entity = SimpleNameValueEntityFactory.create( //
            "_usercode", encode(info.sourceCode),
            "check", "0", //
            "language", info.remotelanguage, //
            "problemid", info.remoteProblemId, //
//            "usercode", info.sourceCode, //
            getCharset() //
        );
        client.post("/submit.php?action=submit", entity, HttpStatusValidator.SC_MOVED_TEMPORARILY);
        return null;
    }

    private String encode(String usercode) {
        String res = "";
        try {
            String urlStr = URLEncoder.encode(usercode, "utf-8");
            res = Base64.encodeBase64String(urlStr.getBytes("utf-8"));
        } catch (Exception e) {
        }
        return res;
    }

}
