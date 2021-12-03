package judge.remote.provider.tkoj;

import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.LoginerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对TK题库登入器进行测试
 *
 * @author zzzz76
 */
public class TKOJLoginerTest extends LoginerTest {

    @Autowired
    private TKOJLoginer loginer;

    @Override
    public void testLogin() throws Exception {
        RemoteAccount account = new RemoteAccount(
                RemoteOj.TKOJ,
                "onz11223",
                "123456ecnu",
                "",
                getNewContext()
        );
        loginer.login(account);
        terminal();
    }

}
