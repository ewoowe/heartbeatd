package org.ewoowe.heartbeatd;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 心跳监测服务执行器
 * @author wangcheng2017@ict.ac.cn
 * @date 2021.01.21
 */
public class HeartbeatdRunner implements Job
{
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Heartbeatd heartbeatd = (Heartbeatd) jobDataMap.get("heartbeatd instance");
        System.out.println("heartbeatd instance run");
        heartbeatd.run();
    }
}
