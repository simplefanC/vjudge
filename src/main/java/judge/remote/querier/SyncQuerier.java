package judge.remote.querier;

import judge.httpclient.DedicatedHttpClientFactory;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.submitter.SubmissionInfo;
import judge.tool.Handler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * No dependence on any other resource, just do it in invoking thread.
 * 例如HDU
 * @author Isun
 */
public abstract class SyncQuerier implements Querier {

    @Autowired
    protected DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public void query(SubmissionInfo info, Handler<SubmissionRemoteStatus> handler) {
        SubmissionRemoteStatus status = null;
        try {
            status = query(info);
        } catch (Throwable t) {
            handler.onError(t);
            return;
        }
        handler.handle(status);
    }

    abstract protected SubmissionRemoteStatus query(SubmissionInfo info) throws Exception;

}
