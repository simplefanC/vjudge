package judge.remote.provider.tkoj;

import judge.remote.RemoteOj;
import judge.remote.querier.AuthenticatedQuerierTest;
import judge.remote.submitter.SubmissionInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对TK题库轮询器进行测试
 *
 * @author zzzz76
 */
public class TKOJQuerierTest extends AuthenticatedQuerierTest {

    @Autowired
    private TKOJQuerier querier;

    @Override
    public void testQuery() throws Exception {
        querier.query(fakeSubmissionInfo(), getHandler());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remoteOj = RemoteOj.TKOJ;
        info.remoteProblemId = "1000";
        info.remoteRunId= "1321785";
        info.remoteAccountId = "fruitshampoo";
        return info;
    }
}
