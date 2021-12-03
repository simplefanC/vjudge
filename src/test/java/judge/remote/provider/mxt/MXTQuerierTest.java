package judge.remote.provider.mxt;

import judge.remote.RemoteOj;
import judge.remote.querier.AuthenticatedQuerierTest;
import judge.remote.submitter.SubmissionInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对码学堂轮询器进行测试
 *
 * @author zzzz76
 */
public class MXTQuerierTest extends AuthenticatedQuerierTest {

    @Autowired
    private MXTQuerier querier;

    @Override
    public void testQuery() throws Exception {
        querier.query(fakeSubmissionInfo(), getHandler());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remoteOj = RemoteOj.MXT;
        info.remoteProblemId = "1691";
        info.remoteRunId= "683841";
        info.remoteAccountId = "ecnuv4@163.com";
        return info;
    }
}
