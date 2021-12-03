package judge.remote.provider.jsk;

import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.LoginerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对计蒜客登入器进行测试
 *
 * @author zzzz76
 */
public class JSKLoginerTest extends LoginerTest {

    @Autowired
    private JSKLoginer loginer;

    @Override
    public void testLogin() throws Exception {
        RemoteAccount account = new RemoteAccount(
                RemoteOj.JSK,
                "+8615079084006",
                "123456ecnu",
                "",
                getNewContext()
        );
        loginer.login(account);
        terminal();
    }

}
