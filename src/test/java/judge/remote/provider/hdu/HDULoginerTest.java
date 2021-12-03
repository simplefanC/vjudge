package judge.remote.provider.hdu;

import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.LoginerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对杭电题库登入器进行测试
 *
 * @author zzzz76
 */
public class HDULoginerTest extends LoginerTest {

    @Autowired
    private HDULoginer loginer;

    @Override
    public void testLogin() throws Exception {
        RemoteAccount account = new RemoteAccount(
                RemoteOj.HDU,
                "gongtengxinyi",
                "123456ecnu",
                "",
                getNewContext()
        );
        loginer.login(account);
        terminal();
    }
}
