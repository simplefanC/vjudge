package judge.remote.provider.mxt;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;
import org.apache.http.HttpHost;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-11-22
 */
public class MXTInfo {
    public static final RemoteOjInfo INFO = new RemoteOjInfo(
            RemoteOj.MXT,
            "MXT",
            new HttpHost("www.maxuetang.cn", 443, "https")
    );

    static {
        INFO.faviconUrl = "images/remote_oj/MXT_favicon.ico";
    }
}
