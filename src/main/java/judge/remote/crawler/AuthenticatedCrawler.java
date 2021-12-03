package judge.remote.crawler;

import judge.executor.ExecutorTaskType;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.DedicatedHttpClientFactory;
import judge.remote.account.RemoteAccount;
import judge.remote.account.RemoteAccountTask;
import judge.remote.loginer.LoginersHolder;
import judge.tool.Handler;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Which need to login to crawl (and of course, not SyncCrawler)
 * 例如MXT
 *
 * User: zzzz76
 * Date: 2020-11-28
 */
public abstract class AuthenticatedCrawler implements Crawler{

    @Autowired
    protected DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public void crawl(String problemId, Handler<RawProblemInfo> handler) {
        // 在此处验证爬取真实性以避免无意义的性能损失
        try {
            preValidate(problemId);
        } catch (Throwable e) {
            handler.onError(e);
            return;
        }
        new CrawlTask(problemId, handler).submit();
    }

    /**
     * Can be overridden
     * @param problemId
     */
    protected void preValidate(String problemId) {}

    class CrawlTask extends RemoteAccountTask<RawProblemInfo> {
        private String problemId;

        public CrawlTask(String problemId, Handler<RawProblemInfo> handler) {
            super(
                    ExecutorTaskType.UPDATE_PROBLEM_INFO,
                    getOjInfo().remoteOj,
                    null,
                    null,
                    handler);
            this.problemId = problemId;
        }

        @Override
        protected RawProblemInfo call(RemoteAccount remoteAccount) throws Exception {
            LoginersHolder.getLoginer(getOjInfo().remoteOj).login(remoteAccount);
            DedicatedHttpClient client = dedicatedHttpClientFactory.build(getHost(), remoteAccount.getContext(), getCharset());
            return crawl(problemId, remoteAccount, client);
        }
    }

    protected abstract RawProblemInfo crawl(String problemId, RemoteAccount remoteAccount, DedicatedHttpClient client) throws Exception;

    /**
     * Can be overridden
     * @return
     */
    protected HttpHost getHost() {
        return getOjInfo().mainHost;
    }

    /**
     * Can be overridden
     * @return
     */
    protected String getCharset() {
        return getOjInfo().defaultChaset;
    }
}
