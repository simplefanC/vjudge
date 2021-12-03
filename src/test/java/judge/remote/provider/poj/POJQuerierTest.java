package judge.remote.provider.poj;

import judge.remote.RemoteOj;
import judge.remote.querier.AuthenticatedQuerierTest;
import judge.remote.submitter.SubmissionInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对北京大学题库轮询器进行测试
 *
 * @author zzzz76
 */
public class POJQuerierTest extends AuthenticatedQuerierTest {

    @Autowired
    private POJQuerier querier;

    @Override
    public void testQuery() throws Exception {
        querier.query(fakeSubmissionInfo(), getHandler());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remoteOj = RemoteOj.POJ;
        info.remoteProblemId = "1000";
        info.remoteRunId= "22433364";
        info.remoteAccountId = "zzzz76";
        return info;
    }
}
