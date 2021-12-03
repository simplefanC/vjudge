package judge.remote.provider.hdu;

import judge.remote.RemoteOj;
import judge.remote.submitter.SubmissionInfo;
import judge.remote.submitter.SubmitterTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对杭电题库提交器进行测试
 *
 * @author zzzz76
 */
public class HDUSubmitterTest extends SubmitterTest {

    @Autowired
    private HDUSubmitter submitter;

    @Override
    public void testSubmitCode() throws Exception {
        submitter.submitCode(fakeSubmissionInfo(), getHandle());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remotelanguage = "2";
        info.remoteOj = RemoteOj.HDU;
        info.remoteProblemId = "1003";
        info.sourceCode =
                "#include <cstdio>\n" +
                "#include <cstdlib>\n" +
                "int main() {\n" +
                "    int a, b;\n" +
                "    while (scanf(\"%d%d\", &a, &b) != EOF) {\n" +
                "        printf(\"%d\\n\", a + b);\n" +
                "    }\n" +
                "    return EXIT_SUCCESS;\n" +
                "}";
        return info;
    }
}
