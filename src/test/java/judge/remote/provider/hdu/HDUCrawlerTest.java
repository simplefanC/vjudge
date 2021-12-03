package judge.remote.provider.hdu;

import judge.remote.crawler.SimpleCrawlerTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对杭电题库爬取器进行测试
 *
 * @author zzzz76
 */
public class HDUCrawlerTest extends SimpleCrawlerTest {

    @Autowired
    private HDUCrawler crawler;

    @Override
    public void testCrawl() throws Exception {
        crawler.crawl("1001", getHandler());
        terminal();
    }

}
