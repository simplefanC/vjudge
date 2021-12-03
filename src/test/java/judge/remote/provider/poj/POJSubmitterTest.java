package judge.remote.provider.poj;

import judge.remote.RemoteOj;
import judge.remote.submitter.SubmissionInfo;
import judge.remote.submitter.SubmitterTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对北京大学题库提交器进行测试
 *
 * @author zzzz76
 */
public class POJSubmitterTest extends SubmitterTest {

    @Autowired
    private POJSubmitter submitter;

    @Override
    public void testSubmitCode() throws Exception {
        submitter.submitCode(fakeSubmissionInfo(), getHandle());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remotelanguage = "4";
        info.remoteOj = RemoteOj.POJ;
        info.remoteProblemId = "1000";
        info.sourceCode =
                "#include <iostream>\n" +
                "using namespace std;\n" +
                "int main()\n" +
                "{\n" +
                "    int a,b;\n" +
                "    cin >> a >> b;\n" +
                "    cout << a+b << endl;\n" +
                "    return 0;\n" +
                "}";
        return info;
    }
}
