package judge.remote.provider.nbut;

import judge.remote.RemoteOj;
import judge.remote.submitter.SubmissionInfo;
import judge.remote.submitter.SubmitterTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对宁波工程学院提交器进行测试
 *
 * @author zzzz76
 */
public class NBUTSubmitterTest extends SubmitterTest {

    @Autowired
    private NBUTSubmitter submitter;

    @Override
    public void testSubmitCode() throws Exception {
        submitter.submitCode(fakeSubmissionInfo(), getHandle());
        terminal();
    }

    private SubmissionInfo fakeSubmissionInfo() {
        SubmissionInfo info = new SubmissionInfo();
        info.remotelanguage = "2";
        info.remoteOj = RemoteOj.NBUT;
        info.remoteProblemId = "1000";
        info.sourceCode =
                "#include <cstdio>\n" +
                "#include <cstdlib>\n" +
                "int main() {\n" +
                "    long long a, b;\n" +
                "    scanf(\"%lld%lld\", &a, &b);\n" +
                "    while (a != 0 && b != 0) {\n" +
                "        printf(\"%lld\\n\", a + b);\n" +
                "        scanf(\"%lld%lld\", &a, &b);\n" +
                "    }\n" +
                "    return EXIT_SUCCESS;\n" +
                "}";
        return info;
    }
}
