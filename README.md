# Virtual-Judge

基础代码来自 https://github.com/zhblue/vjudge

本人初步工作：

- 整理了所有的jar依赖，转变为Maven项目
- 接入新的OJ
- 给出Docker部署方案，实现一键部署
- 给出核心模块源码的简要解析

## 部署

### docker-compose部署

1. 安装docker

```
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
```

2. 安装docker-compose

```
curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```

```
chmod +x /usr/local/bin/docker-compose
```

```
ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
```

```
docker-compose --version
```

3. 编写docker-compose.yml（见deploy目录）

```yaml
version: "3"
services:
  voj-mysql:
    image: mysql:5.7
    container_name: voj-mysql
    command: mysqld --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    restart: always
    environment:
      - MYSQL_ROOT_PASSWORD=root #设置root帐号密码
      - TZ=Asia/Shanghai
    ports:
      - 3306:3306
    volumes:
      - /mydata/vjudge/mysql/data/db:/var/lib/mysql #数据文件挂载
      - /mydata/vjudge/mysql/data/conf:/etc/mysql/conf.d #配置文件挂载
      - /mydata/vjudge/mysql/log:/var/log/mysql #日志文件挂载

  voj-tomcat:
    image: tomcat:9.0
    container_name: voj-tomcat
    restart: always
    depends_on:
      - voj-mysql
    volumes:
      - /mydata/vjudge/tomcat/logs:/usr/local/tomcat/logs
      - /mydata/vjudge/tomcat/webapps:/usr/local/tomcat/webapps
      - /mydata/vjudge/data:/data
    environment:
      - TZ=Asia/Shanghai
    ports:
      - 80:8080
```

4. 启动

`docker-compose up -d`

5. 将vjudge.war放至/mydata/vjudge/tomcat/webapps目录
6. 修改log4j.properties

```sh
sed -i 's#../logs/log4j.log#/usr/local/tomcat/logs/log4j.log#g' /mydata/vjudge/tomcat/webapps/vjudge/WEB-INF/classes/log4j.properties
```

7. 将数据库SQL脚本导入MySQL（见deploy目录）

8. 远程账户可在`/mydata/vjudge/tomcat/webapps/vjudge/WEB-INF/classes/remote_accounts.json`配置

9. 重启voj-tomcat容器

   `docker restart voj-tomcat`

   访问`ip/vjudge`测试

10. 配置vjudge项目可通过根路径访问

- `docker cp voj-tomcat:/usr/local/tomcat/conf/server.xml ./`

- `vi server.xml`作出如下修改

```xml
<Host name="localhost"  appBase=""
    unpackWARs="true" autoDeploy="true">
	<Context docBase="webapps/vjudge" path="" /> 
</Host>
```

- `docker cp ./server.xml voj-tomcat:/usr/local/tomcat/conf/`

- 重启voj-tomcat容器
- 访问`ip`测试

https://stackoverflow.com/questions/4044129/tomcat-making-a-project-folder-the-web-root

https://segmentfault.com/a/1190000002985203

### docker部署

```bash
docker run -p 3306:3306 --name mysql \
-v /mydata/mysql/log:/var/log/mysql \
-v /mydata/mysql/data:/var/lib/mysql \
-v /mydata/mysql/conf:/etc/mysql \
-e MYSQL_ROOT_PASSWORD=root  \
-d mysql:5.7
```

```bash
docker run -p 8080:8080 --name tomcat \
--link mysql:db \
-v /mydata/tomcat/webapps:/usr/local/tomcat/webapps \
-v /mydata/tomcat/logs:/usr/local/tomcat/logs \
-e TZ="Asia/Shanghai" \
-d tomcat:8.0.20
```

导入数据

修改config.properties的

- jdbc.url=jdbc:mysql://db/vhoj

访问ip:8080/vjudge

## 源码解析

judge.executor.TaskExecutor定义了线程池

两个方法：submitNoDelay和submitDelay（使用ScheduledExecutorService实现）

↓

judge.executor.Task定义任务

重要方法submit

被以下几个地方调用

- judge.remote.**ProblemInfoUpdateManager**#updateProblem 爬取
- judge.remote.**SubmitCodeManager**#submitCode 提交
- judge.remote.**QueryStatusManager**#createQuery 轮询
- judge.remote.account.RemoteAccountRepository#execute



### loginer

![image-20211129143353652](https://gitee.com/cf_9909/image_bed/raw/master/images/image-20211129143353652.png)

judge.remote.loginer.RetentiveLoginer#login

### crawler

#### 业务

添加比赛功能：

contest_edit.js#updateTitle

addContest.jsp

judge.service.JudgeService#findProblemSimple

judge.remote.ProblemInfoUpdateManager#updateProblem

![image-20211129175125411](https://gitee.com/cf_9909/image_bed/raw/master/images/image-20211129175125411.png)

judge.remote.ProblemInfoUpdateTask具体实现Callable接口

**核心代码 judge.remote.ProblemInfoUpdateTask#call**

call方法中调用crawler.crawl(problemId, handler)执行爬题

judge.remote.crawler.SyncCrawler#crawl(problemId, handler)

info = judge.remote.crawler.SimpleCrawler#crawl(problemId)

judge.remote.provider.hdu.HDUCrawler#populateProblemInfo(info, problemId, html)

**执行handler**

#### crawler

![image-20211129145031101](https://gitee.com/cf_9909/image_bed/raw/master/images/image-20211129145031101.png)

CrawlersHolder维护一个HashMap

```java
private static HashMap<RemoteOj, Crawler> crawlers = new HashMap<>();
```



```markdown
SyncCrawler#crawl(problemId, handler)
	调用子类的crawl(problemId)
		调用子类的populateProblemInfo
	执行handler
```

### submitter

```
RunningSubmissions
// submission.id -> submission
private Map<Integer, Submission> records = new ConcurrentHashMap<>();
```



#### 业务

judge.action.ProblemAction#submit

judge.remote.SubmitCodeManager#submitCode进行判题

​	submission缓存到judge.remote.RunningSubmissions

​	new SubmitCodeTask(submission).submit();提交线程池执行

​	**核心代码 judge.remote.SubmitCodeManager.SubmitCodeTask#call**

​		judge.remote.submitter.SubmittersHolder#getSubmitter根据OJ获取submitter实例

​		调用judge.remote.submitter.SimpleSubmitter#submitCode(SubmissionInfo, Handler)

​			new SubmitTask(info, handler).submit();调用RemoteAccountTask的submit交由RemoteAccountTaskExecutor执行

​				RemoteAccountTaskExecutor的守护线程调用RemoteAccountRepository#handle(task)

​					RemoteAccountRepository#tryExecute(task)

​						findAccount

​						execute(task, account) Task实现Callable接口 并调用submit方法 交由TaskExecutor线程池去执行

​							执行task.call 实际 **judge.remote.submitter.SimpleSubmitter.SubmitTask#call**

​								需要登录则登录 获取HttpClient

​								SimpleSubmitter#submitCode(info, remoteAccount, client)完成远程提交 实际由子类(如MXTSubmitter)实现

​								SimpleSubmitter#getMaxRunId完成最新runID的获取

​							结果放入RemoteAccountTask#resultQueue 任务执行完毕 再次入队

​		**调用Handler**【将结果(SubmissionReceipt)作为参数】

​			submission.setRealRunId(receipt.remoteRunId);

​			queryStatusManager.createQuery(submission);开始查询

![image-20211130170004518](https://gitee.com/cf_9909/image_bed/raw/master/images/image-20211130170004518.png)





![image-20211129185130011](https://gitee.com/cf_9909/image_bed/raw/master/images/image-20211129185130011.png)





![image-20211129145104444](https://gitee.com/cf_9909/image_bed/raw/master/images/image-20211129145104444.png)



### querier

每次提交时 将submissionId->Submission缓存于judge.remote.RunningSubmissions#records

```java
private Map<Integer, Submission> records = new ConcurrentHashMap<>();
```



业务

提交后进行轮询judge.service.JudgeService#getResult

查询judge.remote.RunningSubmissions

![image-20211129145158307](https://gitee.com/cf_9909/image_bed/raw/master/images/image-20211129145158307.png)

judge.remote.QueryStatusManager#createQuery

​	runningSubmissions.add(submission);
​	new QueryStatusTask(submission, 0).submit();提交线程池执行

​		**核心代码judge.remote.QueryStatusManager.QueryStatusTask#call**

​			QueriersHolder.getQuerier(info.remoteOj);获取Querier

​			judge.remote.querier.SyncQuerier#query(info, handler)

​				具体query由子类具体实现 返回SubmissionRemoteStatus

​				执行handler回调

​				int nextQueryDelaySeconds = submission.getQueryCount() + 2;

​				new QueryStatusTask(submission, nextQueryDelaySeconds).submit();

### account

**RemoteAccountTaskExecutorFactory**创建RemoteAccountTaskExecutor

**RemoteAccountTaskExecutor**的构造函数中，根据远程账户配置构造RemoteAccountRepository

```java
private Map<RemoteOj, RemoteAccountRepository> repos = new HashMap<>();

public RemoteAccountTaskExecutor(Map<RemoteOj, RemoteAccountOJConfig> config) {
    for (RemoteOj remoteOj : config.keySet()) {
        RemoteAccountOJConfig ojConfig = config.get(remoteOj);
        repos.put(remoteOj, new RemoteAccountRepository(remoteOj, ojConfig, this));
    }
}
```

**RemoteAccountRepository**

```java
private final Map<String, RemoteAccountStatus> publicRepo = new HashMap<>();
private final Map<String, RemoteAccountStatus> privateRepo = new HashMap<>();

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
```

#### RemoteAccountRepository解析

**execute**

实现Callable接口 并调用submit方法 交由TaskExecutor去执行



tryBacklog



**handle**

被RemoteAccountTaskExecutor#init所调用

```java
public void handle(RemoteAccountTask<?> task) {
    if (task.isDone()) {
        releaseAccount(task.getAccount());
    } else {
        tryExecute(task);
    }
}
```

**tryExecute**

​	findAccount获取未锁定的账号

​	execute(task, account);

releaseAccount



#### RemoteAccountTask

![](https://gitee.com/cf_9909/image_bed/raw/master/images/image-20211130170004518.png)		

RemoteAccountTask#**call方法**交由子类去实现

被RemoteAccountRepository#execute调用



**submit方法**

```java
public void submit() {
    if (!_submitted) {
        _submitted = true;
        SpringBean.getBean(RemoteAccountTaskExecutor.class).submit(this);
    }
}
```

调用RemoteAccountTaskExecutor#submit

#### 核心RemoteAccountTaskExecutor

```java
private LinkedBlockingQueue<RemoteAccountTask<?>> running = new LinkedBlockingQueue<>();
```

一个RemoteAccountTask两次进入该队列

- Apply for an account and execute 一次执行
- Return the account, whatever that task finishes normally 一次释放

**submit**

```java
public void submit(RemoteAccountTask<?> task) {
    running.offer(task);//将任务放入队列
}
```

**核心 RemoteAccountTaskExecutor#init**

通过在配置文件中通过init-method指定该方法来初始化remoteAccountTaskExecutor



### language

获取语言列表

judge.remote.language.LanguageManager#getLanguages(remoteOj, remoteProblemId)



### 自动登录

使用redis实现了自动登录功能



