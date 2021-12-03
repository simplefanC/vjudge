package judge.remote.provider.nbut;

import judge.remote.RemoteOj;
import judge.remote.querier.AuthenticatedQuerierTest;
import judge.remote.submitter.SubmissionInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对宁波工程学院轮询器进行测试
 *
 * @author zzzz76
 */
public class NBUTQuerierTest extends AuthenticatedQuerierTest {

    @Autowired
    private NBUTQuerier querier;

    @Override
    public void testQuery() throws Exception {
        querier.query(fakeSubmissionInfo(), getHandler());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remoteOj = RemoteOj.TKOJ;
        info.remoteProblemId = "1000";
        info.remoteRunId= "137021";
        info.remoteAccountId = "jianghuchuan";
        return info;
    }
}
