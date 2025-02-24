package judge.remote.provider.poj;

import judge.remote.RemoteOjInfo;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SimpleCrawler;
import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class POJCrawler extends SimpleCrawler {

    @Override
    public RemoteOjInfo getOjInfo() {
        return POJInfo.INFO;
    }

    @Override
    protected String getProblemUrl(String problemId) {
        return getHost().toURI() + "/problem?id=" + problemId;
    }
    
    @Override
    protected void preValidate(String problemId) {
        Validate.isTrue(problemId.matches("[1-9]\\d*"));
    }

    @Override
    protected boolean autoTransformAbsoluteHref() {
        return false;
    }

    @Override
    protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
        html = HtmlHandleUtil._transformUrlToAbs(html, info.url);
        info.title = Tools.regFind(html, "<title>\\d{3,} -- ([\\s\\S]*?)</title>").trim();
        info.timeLimit = Integer.parseInt(Tools.regFind(html, "<b>Time Limit:</b> (\\d{3,})MS</td>"));
        info.memoryLimit = Integer.parseInt(Tools.regFind(html, "<b>Memory Limit:</b> (\\d{2,})K</td>"));
        info.description = Tools.regFind(html, "<p class=\"pst\">Description</p>([\\s\\S]*?)<p class=\"pst\">");
        info.input = Tools.regFind(html, "<p class=\"pst\">Input</p>([\\s\\S]*?)<p class=\"pst\">");
        info.output = Tools.regFind(html, "<p class=\"pst\">Output</p>([\\s\\S]*?)<p class=\"pst\">");
        info.sampleInput = Tools.regFind(html, "<p class=\"pst\">Sample Input</p>([\\s\\S]*?)<p class=\"pst\">");
        info.sampleOutput = Tools.regFind(html, "<p class=\"pst\">Sample Output</p>([\\s\\S]*?)<p class=\"pst\">");
        info.hint = Tools.regFind(html, "<p class=\"pst\">Hint</p>([\\s\\S]*?)<p class=\"pst\">");
        info.source = Tools.regFind(html, "<p class=\"pst\">Source</p>([\\s\\S]*?)</td></tr></tbody></table>").replaceAll("<[\\s\\S]*?>", "");
    }

}
