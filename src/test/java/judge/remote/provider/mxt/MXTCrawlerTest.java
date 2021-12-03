package judge.remote.provider.mxt;

import judge.remote.crawler.AuthenticatedCrawlerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对码学堂爬取器进行测试
 *
 * @author zzzz76
 */
public class MXTCrawlerTest extends AuthenticatedCrawlerTest{
    @Autowired
    private MXTCrawler crawler;

    @Override
    public void testCrawl() throws Exception {
        crawler.crawl("1008", getHandler());
        terminal();
    }
}
