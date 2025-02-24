package judge.remote;

import judge.bean.Submission;
import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.remote.status.RemoteStatusType;
import judge.remote.submitter.SubmissionInfo;
import judge.remote.submitter.SubmissionReceipt;
import judge.remote.submitter.Submitter;
import judge.remote.submitter.SubmittersHolder;
import judge.service.IBaseService;
import judge.tool.Handler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubmitCodeManager {
    private final static Logger log = LoggerFactory.getLogger(SubmitCodeManager.class);

    @Autowired
    private IBaseService baseService;

    @Autowired
    private QueryStatusManager queryStatusManager;

    @Autowired
    private RunningSubmissions runningSubmissions;


    public void submitCode(Submission submission) {
        if (submission.getId() == 0) {
            log.error(String.format("Please persist first: %s", runningSubmissions.getLogKey(submission)));
            return;
        }
        if (!runningSubmissions.contains(submission.getId())) {
            log.info("Create submit: " + runningSubmissions.getLogKey(submission) + " [" + submission.getUsername() + "]");// 打印submission的信息
            runningSubmissions.add(submission);// 缓存submission和最近时间
            new SubmitCodeTask(submission).submit();// 异步线程
        }
    }

    class SubmitCodeTask extends Task<Void> {
        private Submission submission;

        public SubmitCodeTask(Submission submission) {
            super(ExecutorTaskType.SUBMIT_CODE);
            this.submission = submission;
        }

        @Override
        public Void call() throws Exception {
            try {
                submitCode();
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
                onErrorNotSubmitted();
            }
            return null;
        }

        private void submitCode() throws Exception {
            final SubmissionInfo info = SubmissionConverter.toInfo(submission);// 格式转换
            Submitter submitter = SubmittersHolder.getSubmitter(info.remoteOj);// 获取三方实例
            submitter.submitCode(info, new Handler<SubmissionReceipt>() {// 异步线程
                @Override
                public void handle(SubmissionReceipt receipt) {
                    try {
                        _handle(receipt);
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        runningSubmissions.remove(submission.getId());
                    }
                }

                @Override
                public void onError(Throwable t) {
                    log.error(t.getMessage(), t);
                    onErrorNotSubmitted();
                }

                private void _handle(SubmissionReceipt receipt) {
                    //从提交结果中获取到remoteRunId
                    submission.setRealRunId(receipt.remoteRunId);
                    submission.setRemoteAccountId(receipt.remoteAccountId);
                    submission.setRemoteSubmitTime(receipt.submitTime);
                    if (receipt.remoteRunId == null) {
                        submission.setStatusCanonical(RemoteStatusType.SUBMIT_FAILED_PERM.name());
                        if (StringUtils.isBlank(receipt.errorStatus)) {
                            submission.setStatus("Submit Failed");
                        } else {
                            submission.setStatus(receipt.errorStatus);
                        }
                        log.error("Submit Failed: " + runningSubmissions.getLogKey(submission));
                    } else {
                        submission.setStatus("Submitted");
                        submission.setStatusCanonical(RemoteStatusType.SUBMITTED.name());
                        log.info("Submit Finished: " + runningSubmissions.getLogKey(submission));
                    }
                    baseService.addOrModify(submission);
                    System.out.println(submission.getStatus());
                    // 为啥这里就要remove 因为在createQuery中又add了
                    runningSubmissions.remove(submission.getId());

                    if (receipt.remoteRunId != null) {
                        queryStatusManager.createQuery(submission);
                    }
                }
            });
        }

        private void onErrorNotSubmitted() {
            runningSubmissions.remove(submission.getId());
            submission.setStatus("Submit Failed");
            submission.setStatusCanonical(RemoteStatusType.SUBMIT_FAILED_TEMP.name());
            baseService.addOrModify(submission);
        }
    }

}
