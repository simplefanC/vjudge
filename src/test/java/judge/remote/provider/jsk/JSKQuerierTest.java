package judge.remote.provider.jsk;

import judge.remote.RemoteOj;
import judge.remote.querier.AuthenticatedQuerierTest;
import judge.remote.submitter.SubmissionInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对计蒜客轮询器进行测试
 *
 * @author zzzz76
 */
public class JSKQuerierTest extends AuthenticatedQuerierTest {

    @Autowired
    private JSKQuerier querier;

    @Override
    public void testQuery() throws Exception {
        querier.query(fakeSubmissionInfo(), getHandler());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remoteOj = RemoteOj.JSK;
        info.remoteProblemId = "34486";
        info.remoteRunId= "GOIXDRewUmK3Ludq3UEopOq";
        info.remoteAccountId = "+8615079084006";
        return info;
    }
}
