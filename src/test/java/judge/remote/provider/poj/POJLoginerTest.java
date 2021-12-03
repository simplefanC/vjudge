package judge.remote.provider.poj;

import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.LoginerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对北京大学题库登入器进行测试
 *
 * @author zzzz76
 */
public class POJLoginerTest extends LoginerTest {

    @Autowired
    private POJLoginer loginer;

    @Override
    public void testLogin() throws Exception {
        RemoteAccount account = new RemoteAccount(
                RemoteOj.POJ,
                "zzzz76",
                "123456ecnu",
                "",
                getNewContext()
        );
        loginer.login(account);
        terminal();
    }
}
