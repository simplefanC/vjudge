package judge.remote.provider.mxt;

import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.LoginerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对码学堂登入器进行测试
 *
 * @author zzzz76
 */
public class MXTLoginerTest extends LoginerTest {

    @Autowired
    private MXTLoginer loginer;

    @Override
    public void testLogin() throws Exception {
        RemoteAccount account = new RemoteAccount(
                RemoteOj.MXT,
                "ecnuv2@163.com",
                "123456ecnu",
                "",
                getNewContext()
        );
        loginer.login(account);
        terminal();
    }

}
