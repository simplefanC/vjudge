package judge.remote.submitter;

import judge.executor.ExecutorTaskType;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.DedicatedHttpClientFactory;
import judge.remote.account.RemoteAccount;
import judge.remote.account.RemoteAccountTask;
import judge.remote.language.LanguageFinder;
import judge.remote.language.LanguageFindersHolder;
import judge.remote.loginer.LoginersHolder;
import judge.tool.Handler;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

/**
 * On many OJs, after submitting, we can get the remote run ID from the direct
 * response of submitting request.
 * 例如JSK MXT
 *
 * 原作者 名为 SyncSubmitter
 * @author Isun
 */
public abstract class SimpleSubmitter implements Submitter {
    private final static Logger log = LoggerFactory.getLogger(SimpleSubmitter.class);

    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public void submitCode(SubmissionInfo info, Handler<SubmissionReceipt> handler) throws Exception {
        // 在回调指定类中新建子线程，实现回调异步
        // 三方提交器的回调异步，需要先在队列中排队等待
        new SubmitTask(info, handler).submit();
    }

    private boolean isRunIdValid(String remoteRunId) {
        return remoteRunId != null && !remoteRunId.isEmpty();
    }

    /**
     * Can be overridden
     *
     * @return
     */
    protected HttpHost getHost() {
        return getOjInfo().mainHost;
    }

    /**
     * Can be overridden
     *
     * @return
     */
    protected String getCharset() {
        return getOjInfo().defaultChaset;
    }

    protected abstract boolean needLogin();

    protected abstract String getMaxRunId(SubmissionInfo info, DedicatedHttpClient client)
            throws Exception;

    /**
     * @param info
     * @param remoteAccount
     * @param client
     * @return error status string, if submitting succeeds, return null
     * @throws Exception
     */
    protected abstract String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client)
            throws Exception;

    class SubmitTask extends RemoteAccountTask<SubmissionReceipt> {
        private SubmissionInfo info;

        public SubmitTask(SubmissionInfo info, Handler<SubmissionReceipt> handler) {
            super( //
                    ExecutorTaskType.SUBMIT_CODE, //
                    getOjInfo().remoteOj, //
                    info.remoteAccountId, //
                    "SUBMIT_CODE_" + info.remoteProblemId, //
                    handler //
            );
            this.info = info;
        }

        @Override
        protected SubmissionReceipt call(RemoteAccount remoteAccount) throws Exception {
            Validate.isTrue(info.remoteAccountId == null || info.remoteAccountId.equals(remoteAccount.getAccountId()));
            info.remoteAccountId = remoteAccount.getAccountId();

            LanguageFinder languageFinder = LanguageFindersHolder.getLanguageFinder(getOjInfo().remoteOj);
            HashMap<String, String> languageAdapter = languageFinder.getLanguagesAdapter();
            if (languageAdapter != null && languageAdapter.containsKey(info.remotelanguage)) {
                info.remotelanguage = languageAdapter.get(info.remotelanguage);
            }

            if (needLogin()) {
                LoginersHolder.getLoginer(getOjInfo().remoteOj).login(remoteAccount);// 完成远程登入，若之前登入过则不会重复登入
            }
            DedicatedHttpClient client = dedicatedHttpClientFactory.build(getHost(), remoteAccount.getContext(),
                    getCharset()); // 从连接池里 getHttpClient(hostname)

            String errorStatus = submitCode(info, remoteAccount, client);// 完成远程提交
            if (errorStatus != null) {
                return new SubmissionReceipt(null, null, errorStatus);
            }

            // 完成最新runID的获取
            String remoteRunId = getMaxRunId(info, client);

            if (!isRunIdValid(remoteRunId)) {
                log.error(String.format( //
                        "Submit failed: errorStatus = %s, remoteRunId = %s", //
                        errorStatus, //
                        remoteRunId));
                return new SubmissionReceipt(null, null, errorStatus);
            }

            log.info(String.format( //
                    "Submit %s | %s | %s | %s", //
                    getOjInfo().literal, //
                    info.remoteProblemId, //
                    info.remoteAccountId, //
                    remoteRunId));
            return new SubmissionReceipt(remoteRunId, remoteAccount.getAccountId(), null);
        }

    }

}
