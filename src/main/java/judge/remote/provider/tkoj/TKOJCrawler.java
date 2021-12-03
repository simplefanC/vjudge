package judge.remote.provider.tkoj;

import judge.remote.RemoteOjInfo;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SimpleCrawler;
import judge.tool.Tools;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Created with IDEA
 * User: zzzz76
 * Date: 2020-12-10
 */
@Component
public class TKOJCrawler extends SimpleCrawler {

    @Override
    public RemoteOjInfo getOjInfo() {
        return TKOJInfo.INFO;
    }

    @Override
    protected void preValidate(String problemId) {
        Validate.isTrue(problemId.matches("[1-9]\\d*"));
    }

    @Override
    protected String getProblemUrl(String problemId) {
        return getOjInfo().mainHost.toURI() + "/problem.php?id=" + problemId;
    }

    @Override
    protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) throws Exception {
        // 在此处进行爬取
        info.title = Tools.regFind(html, "<h2>" + problemId + ": ([\\s\\S]*?)</h2>").trim();
        info.timeLimit = Integer.valueOf(Tools.regFind(html, "Time Limit: </span>([1-9]\\d*) Sec")) * 1000;
        info.memoryLimit = Integer.valueOf(Tools.regFind(html, "Memory Limit: </span>([1-9]\\d*) MB")) * 1024;
        info.description = Tools.regFind(html, "<h2>Description</h2><div class=\"content\">([\\s\\S]*?)</div><h2>Input</h2");
        info.input = Tools.regFind(html, "<h2>Input</h2><div class=\"content\">([\\s\\S]*?)</div>");
        info.output = Tools.regFind(html, "<h2>Output</h2><div class=\"content\">([\\s\\S]*?)</div>");
        info.sampleInput = Tools.regFind(html, "<h2>Sample Input</h2>(<pre[\\s\\S]*?</pre>)");
        info.sampleOutput = Tools.regFind(html, "<h2>Sample Output</h2>(<pre[\\s\\S]*?</pre>)");
        info.hint = Tools.regFind(html, "<h2>HINT</h2><div class=\"content\">([\\s\\S]*?)</div>");
        info.source = Tools.regFind(html, "<h2>Source</h2><div class=\"content\">([\\s\\S]*?)</div>").replaceAll("<[\\s\\S]*?>", "");
    }

}
