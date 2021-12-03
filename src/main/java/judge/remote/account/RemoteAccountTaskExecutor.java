package judge.remote.account;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import judge.remote.RemoteOj;
import judge.remote.account.config.RemoteAccountOJConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 远程账户 任务执行器
 * Short for remote account task executor
 * @author Isun
 *
 */
public class RemoteAccountTaskExecutor {
    private final static Logger log = LoggerFactory.getLogger(RemoteAccountTaskExecutor.class);
    
    /**
     * remoteOj -> repo dedicating to one OJ
     */
    private Map<RemoteOj, RemoteAccountRepository> repos = 
            new HashMap<RemoteOj, RemoteAccountRepository>();
    
    /**
     * <pre>A RemoteAccountTask must enter this queue twice for: 
     * 1st. Apply for an account and execute;
     * 2nd. Return the account, whatever that task finishes normally. 返回帐户，无论该任务正常完成。
     * </pre>
     */
    private LinkedBlockingQueue<RemoteAccountTask<?>> running = 
            new LinkedBlockingQueue<RemoteAccountTask<?>>();
    
    ////////////////////////////////////////////////////////////////

    public RemoteAccountTaskExecutor(Map<RemoteOj, RemoteAccountOJConfig> config) {
        for (RemoteOj remoteOj : config.keySet()) {
            RemoteAccountOJConfig ojConfig = config.get(remoteOj);
            repos.put(remoteOj, new RemoteAccountRepository(remoteOj, ojConfig, this));
        }
    }
    
    ////////////////////////////////////////////////////////////////

    /**
     * 守护线程 负责处理阻塞队列中的消息
     */
    public void init() {
        Thread executingThead = new Thread() {
            public void run() {
                while (true) {
                    RemoteAccountTask<?> task = null;
                    try {
                        // 守护线程空闲 则取出队首任务
                        task = running.take();
                        // 获取任务的种类
                        RemoteOj remoteOj = task.getRemoteOj();

                        // 假设在处理一个提交任务时，存在多个账户可以选择
                        // 获取账号仓库
                        RemoteAccountRepository repo = repos.get(remoteOj);
                        if (repo != null) {
                            // 仓库 调用 任务实例
                            repo.handle(task);
                        } else {
                            throw new RuntimeException("There are no remote accounts configured for remote OJ: " + remoteOj);
                        }
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        if (task != null) {
                            try {
                                // 通过函数回调，进行事务回滚
                                task.getHandler().onError(t);
                            } catch (Throwable t1) {
                                log.error(t.getMessage(), t1);
                            }
                        }
                    }
                }
            }
        };
        // This method must be invoked before the thread is started.
        executingThead.setDaemon(true);
        executingThead.start();
    }
    
    public void submit(RemoteAccountTask<?> task) {
        running.offer(task);
    }
    
}
