package judge.remote.account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import judge.executor.Task;
import judge.remote.RemoteOj;
import judge.remote.account.config.RemoteAccountConfig;
import judge.remote.account.config.RemoteAccountOJConfig;
import judge.tool.Handler;

import org.apache.commons.lang3.Validate;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Containing remote accounts dedicating to one remote OJ
 * 专用于一个远程OJ 的远程账户
 *
 * @author Isun
 */
public class RemoteAccountRepository {
    private final static Logger log = LoggerFactory.getLogger(RemoteAccountRepository.class);

    private final RemoteOj remoteOj;

    /**
     * accountId -> acountStatus
     */
    private final Map<String, RemoteAccountStatus> publicRepo = new HashMap<>();

    /**
     * accountId -> acountStatus
     */
    private final Map<String, RemoteAccountStatus> privateRepo = new HashMap<>();

    /**
     * accountId == null, meaning any account is OK.
     */
    private final LinkedList<RemoteAccountTask<?>> normalBacklog = new LinkedList<>();

    /**
     * accountId != null, meaning specifying an account.
     * accountId -> tasks
     */
    private final Map<String, LinkedList<RemoteAccountTask<?>>> pickyBacklog = new HashMap<>();

    private final RemoteAccountTaskExecutor remoteAuthTaskExecutor;

    /////////////////////////////////////////////////////////////

    /**
     * 构造函数
     *
     * @param remoteOj
     * @param ojConfig
     * @param remoteAuthTaskExecutor
     */
    public RemoteAccountRepository(RemoteOj remoteOj, RemoteAccountOJConfig ojConfig, RemoteAccountTaskExecutor remoteAuthTaskExecutor) {
        this.remoteOj = remoteOj;
        this.remoteAuthTaskExecutor = remoteAuthTaskExecutor;
        // 初始化publicRepo和privateRepo
        for (RemoteAccountConfig accountConfig : ojConfig.accounts) {
            RemoteAccountStatus status = new RemoteAccountStatus(
                    remoteOj,
                    accountConfig.id,
                    accountConfig.password,
                    accountConfig.isPublic,
                    ojConfig.contextNumber);
            (accountConfig.isPublic ? publicRepo : privateRepo).put(accountConfig.id, status);
        }
    }

    /////////////////////////////////////////////////////////////

    /**
     * 在仓库容器中 执行任务
     * @param task
     */
    public void handle(RemoteAccountTask<?> task) {
        if (task.isDone()) {
            // 若待执行的任务 为done
            releaseAccount(task.getAccount());
        } else {
            // 若待执行的任务 为start
            tryExecute(task);
        }
    }

    /**
     * 释放任务锁定的 账户
     * @param account
     */
    private void releaseAccount(RemoteAccount account) {
        Validate.isTrue(account.getRemoteOj().equals(remoteOj));
        String accountId = account.getAccountId();

        RemoteAccountStatus accountStatus;
        if (publicRepo.containsKey(accountId)) {
            accountStatus = publicRepo.get(accountId);
        } else if (privateRepo.containsKey(accountId)) {
            accountStatus = privateRepo.get(accountId);
        } else {
            return;
        }
        // 移除exclusiveCode
        accountStatus.removeExclusiveCode(account.getExclusiveCode());
        // 归还
        accountStatus.returnContext(account.getContext());

        // 私有账号
        LinkedList<RemoteAccountTask<?>> backlogs = pickyBacklog.get(accountId);
        if (backlogs != null) {
            if (tryBacklog(backlogs)) {
                if (backlogs.isEmpty()) {
                    pickyBacklog.remove(accountId);
                }
                return;
            }
        }
        // 公共账号（默认）
        // 账号释放之后 执行之前未获得账号的Task
        if (accountStatus.isPublic()) {
            tryBacklog(normalBacklog);
        }
    }

    /**
     * 执行之前未获得账号而阻塞的Task
     * @param backlog
     * @return
     */
    private boolean tryBacklog(LinkedList<RemoteAccountTask<?>> backlog) {
        for (Iterator<RemoteAccountTask<?>> iterator = backlog.iterator(); iterator.hasNext(); ) {
            RemoteAccountTask<?> task = iterator.next();
            RemoteAccount account = findAccount(task.getAccountId(), task.getExclusiveCode());
            if (account != null) {
                iterator.remove();
                execute(task, account);
                return true;
            }
        }
        return false;
    }

    /**
     * 在仓库容器中 执行任务调用(正向)
     * @param task
     */
    private void tryExecute(RemoteAccountTask<?> task) {
        // 获取task中账号id信息
        String accountId = task.getAccountId();
        if (accountId != null && !publicRepo.containsKey(accountId) && !privateRepo.containsKey(accountId)) {
            task.offerResult(new RuntimeException("Specified accountId(" + accountId + ") not found."));
            return;
        }

        // accountId为空(RemoteAccountTask中并未指定) 或 账号存在于publicRepo 或 账号存在于privateRepo
        RemoteAccount account = findAccount(accountId, task.getExclusiveCode());
        if (account != null) {
            // 使用账号 执行任务
            execute(task, account);
            return;
        }
        // 无可用账号 未指定accountId 放入normalBacklog
        if (accountId == null) {
            normalBacklog.add(task);
            return;
        }
        // 无可用账号 且指定accountId 任务放入该accountId的RemoteAccountTask列表
        LinkedList<RemoteAccountTask<?>> picky = pickyBacklog.get(accountId);
        if (picky == null) {
            picky = new LinkedList<>();
            pickyBacklog.put(accountId, picky);
        }
//        LinkedList<RemoteAccountTask<?>> picky = pickyBacklog.computeIfAbsent(accountId, k -> new LinkedList<>());
        picky.add(task);
    }

    /**
     * 获取尚未锁定的 账号 并返回
     * @param accountId
     * @param exclusiveCode
     * @return
     */
    private RemoteAccount findAccount(String accountId, String exclusiveCode) {
        List<RemoteAccountStatus> candidates = new ArrayList<>();
        //未指定accountId publicRepo皆可
        if (accountId == null) {
            candidates.addAll(publicRepo.values());
        } else if (publicRepo.containsKey(accountId)) {
            candidates.add(publicRepo.get(accountId));
        } else if (privateRepo.containsKey(accountId)) {
            candidates.add(privateRepo.get(accountId));
        }
        Collections.shuffle(candidates);
        for (RemoteAccountStatus remoteAccountStatus : candidates) {
            //  exclusiveCode: 如"SUBMIT_CODE_1413"
            if (remoteAccountStatus.eligible(accountId, exclusiveCode)) {
                // 借出
                HttpContext context = remoteAccountStatus.borrowContext();
                // 添加exclusiveCode
                remoteAccountStatus.addExclusiveCode(exclusiveCode);
                return new RemoteAccount(
                        remoteOj,
                        remoteAccountStatus.getAccountId(),
                        remoteAccountStatus.getPassword(),
                        exclusiveCode,
                        context);
            }
        }
        return null;
    }

    private <V> void execute(final RemoteAccountTask<V> task, final RemoteAccount account) {
        // 实现Callable接口 并调用submit方法 交由TaskExecutor去执行
        new Task<V>(task.getExecutorTaskType()) {
            @Override
            public V call() throws Exception {
                V result = null;
                Throwable throwable = null;
                try {
                    task.setAccount(account);
                    try {
                        result = task.call(account);//调用 实现RemoteAccountTask的具体XxxTask#call (如SubmitTask)
                    } catch (Throwable t) {
                        throwable = t;
                        task.offerResult(throwable);//插入resultQueue
                    }
                    if (result != null) {
                        task.offerResult(result);
                    }
                } catch (Throwable t) {
                    log.error(t.getMessage(), t);
                } finally {
                    task.done();
                    //任务执行完毕 再次入队
                    remoteAuthTaskExecutor.submit(task);
                }

                Handler<V> handler = task.getHandler();
                if (handler != null) {
                    if (result != null) {
                        // 回调 处理结果
                        handler.handle(result);
                    } else if (throwable != null) {
                        // 回调 业务回滚
                        handler.onError(throwable);
                    } else {
                        // 回调 业务回滚
                        handler.onError(new RuntimeException("What the fuck ??"));
                    }
                }
                return null;
            }
        }.submit();
    }

}
