package judge.remote.provider.nbut;

import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.LoginerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对宁波工程学院登入器进行测试
 *
 * @author zzzz76
 */
public class NBUTLoginerTest extends LoginerTest {

    @Autowired
    private NBUTLoginer loginer;

    @Override
    public void testLogin() throws Exception {
        RemoteAccount account = new RemoteAccount(
                RemoteOj.NBUT,
                "jianghuchuan",
                "123456ecnu",
                "",
                getNewContext()
        );
        loginer.login(account);
        terminal();
    }

}
