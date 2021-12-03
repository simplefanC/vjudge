package judge.remote.provider.hdu;

import judge.remote.RemoteOjInfo;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SimpleCrawler;
import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class HDUCrawler extends SimpleCrawler {

    @Override
    public RemoteOjInfo getOjInfo() {
        return HDUInfo.INFO;
    }

    @Override
    protected String getProblemUrl(String problemId) {
        return getHost().toURI() + "/showproblem.php?pid=" + problemId;
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
        String abs = HtmlHandleUtil.transformUrlToAbs(html, info.url);
        info.title = Tools.regFind(abs, "color:#1A5CC8\">([\\s\\S]*?)</h1>").trim();
        info.timeLimit = (Integer.parseInt(Tools.regFind(abs, "(\\d*) MS")));
        info.memoryLimit = (Integer.parseInt(Tools.regFind(abs, "/(\\d*) K")));
        info.description = (Tools.regFind(abs, "> Problem Description </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
        info.input = (Tools.regFind(abs, "> Input </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
        info.output = (Tools.regFind(abs, "> Output </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
        info.sampleInput = (Tools.regFind(html, ">Sample Input</div>([\\s\\S]*?)<br><[^<>]*?panel_title[^<>]*?>"));
        info.sampleOutput = (Tools.regFind(html, ">Sample Output</div>([\\s\\S]*?)(<br><[^<>]*?panel_title[^<>]*?>|<[^<>]*?><[^<>]*?><i>Hint)") + "</div></div>");
        info.hint = (Tools.regFind(abs, "<i>Hint</i></div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
        if (!StringUtils.isEmpty(info.hint)){
            info.hint = "<pre>" + info.hint + "</pre>";
        }
        info.source = (Tools.regFind(abs, "Source </div><div class=\"panel_content\">([\\s\\S]*?)<[^<>]*?panel_[^<>]*?>").replaceAll("<[\\s\\S]*?>", ""));
    }

}
