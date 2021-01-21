package org.ewoowe.heartbeatd;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 心跳监测任务管理器
 *
 * @author wangcheng2017@ict.ac.cn
 * @date 2021.01.20
 */
@Service
public class Heartbeatd implements HeartbeatdService
{
	/**
	 * 心跳监测任务存储
	 */
	private Map<HeartbeatTask, HeartbeatTaskMonitor> heartbeatHolder = new ConcurrentHashMap<>();

	/**
	 * 接受外部提交的所有请求任务，等待处理
	 */
	private LinkedBlockingQueue<InternalHeartbeatTask> taskQueue = new LinkedBlockingQueue<>();

	/**
	 * 心跳监测任务执行器的调度器
	 */
	private Scheduler scheduler;

	/**
	 * 接收用户的任务类型
	 */
	enum TaskType
	{
		REGISTER,
		UNREGISTER,
		NULL
	}

	class InternalHeartbeatTask
	{
		private TaskType taskType;
		private HeartbeatTask task;
		public InternalHeartbeatTask(TaskType taskType, HeartbeatTask task)
		{
			this.taskType = taskType;
			this.task = task;
		}

		public TaskType getTaskType()
		{
			return taskType;
		}

		public HeartbeatTask getTask()
		{
			return task;
		}
	}

	/**
	 * Heartbeatd实例化后启动，将自己的run方法包装成Quartz Job交给Quartz框架执行
	 * @throws SchedulerException
	 */
	@PostConstruct
	public void start() throws SchedulerException
	{
		JobDetail jobDetail = JobBuilder.newJob(HeartbeatdRunner.class).withIdentity("heartbeat main job").build();
		jobDetail.getJobDataMap().put("heartbeatd instance", this);
		SimpleTrigger trigger = TriggerBuilder.newTrigger().withIdentity("heartbeat main trigger").startNow()
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(1).withRepeatCount(0)).build();
		SchedulerFactory sf = new StdSchedulerFactory();
		scheduler = sf.getScheduler();
		scheduler.start();
		scheduler.scheduleJob(jobDetail, trigger);
	}

	public void run()
	{
		while (true)
		{
			InternalHeartbeatTask task;
			try
			{
				task = taskQueue.take();
			}
			catch (InterruptedException e)
			{
//				task = new InternalHeartbeatTask(TaskType.NULL, null);
				e.printStackTrace();
				continue;
			}

			switch (task.taskType)
			{
				case REGISTER:
				{
					HeartbeatTaskMonitor taskMonitor = createMonitor(task.getTask());
					heartbeatHolder.put(task.task, taskMonitor);

					JobDetail jobDetail = JobBuilder.newJob(HeartbeatTaskMonitorRunner.class)
							.withIdentity("heartbeat task monitor " + task.getTask().getId()).build();
					jobDetail.getJobDataMap().put("heartbeat task monitor instance", taskMonitor);
					jobDetail.getJobDataMap().put("heartbeatd service", this);
					SimpleTrigger trigger = TriggerBuilder.newTrigger().withIdentity("heartbeat task monitor trigger " + task.getTask().getId())
							.startAt(new Date(new Date().getTime() + (task.task.getPeriod() + 1) * 1000))
							.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(task.task.getPeriod() + 1)
									.repeatForever()).build();
					try
					{
						scheduler.scheduleJob(jobDetail, trigger);
					}
					catch (SchedulerException e)
					{
						e.printStackTrace();
					}
					break;
				}
				case UNREGISTER:
				{
					HeartbeatTaskMonitor taskMonitor = heartbeatHolder.get(task.getTask());
					taskMonitor.getCancel().compareAndSet(false, true);
					TriggerKey triggerKey = TriggerKey.triggerKey("heartbeat task monitor trigger " + task.getTask().getId());
					try
					{
						scheduler.pauseTrigger(triggerKey);
						scheduler.unscheduleJob(triggerKey);
						scheduler.deleteJob(JobKey.jobKey("heartbeat task monitor " + task.getTask().getId()));
					}
					catch (SchedulerException exception)
					{
						exception.printStackTrace();
					}
					heartbeatHolder.remove(task.task, taskMonitor);
					break;
				}
				default:
					break;
			}
		}
	}

	/**
	 * 注册心跳监测任务
	 *
	 * @param task 心跳监测任务
	 */
	@Override
	public void registerHeartbeatTask(HeartbeatTask task) throws InterruptedException
	{
		if (task != null)
		{
			task.setId(UUID.randomUUID().toString());
			taskQueue.put(new InternalHeartbeatTask(TaskType.REGISTER, task));
		}
	}

	/**
	 * 注销心跳监测任务
	 *
	 * @param task 心跳监测任务
	 */
	@Override
	public void unregisterHeartbeatTask(HeartbeatTask task) throws InterruptedException
	{
		if (heartbeatHolder.containsKey(task))
			taskQueue.put(new InternalHeartbeatTask(TaskType.UNREGISTER, task));
	}

	/**
	 * 心跳正常监测一次，当收到一次心跳时，调用此方法
	 *
	 * @param task 心跳监测任务
	 */
	@Override
	public void haveHeartbeatOnce(HeartbeatTask task)
	{
		resetHeartbeatTask(task);
		HeartbeatTaskMonitor monitor = heartbeatHolder.get(task);
		if (monitor.getLosted().get())
		{
			if (monitor.incrementRemainsAndGet() == monitor.getThreshold().get())
			{
				monitor.getLosted().compareAndSet(true, false);
				task.getHeartbeatRecoverHandler().doWhenRecover(task);
			}
		}
		else
			monitor.incrementRemainsAndGet();
	}

	/**
	 * 每收到一次心跳正常的监测，需要将已有的心跳丢失监测任务重置
	 * @param task 心跳监测任务
	 */
	private void resetHeartbeatTask(HeartbeatTask task)
	{
		HeartbeatTaskMonitor taskMonitor = heartbeatHolder.get(task);

		taskMonitor.getCancel().compareAndSet(false, true);
		TriggerKey triggerKey = TriggerKey.triggerKey("heartbeat task monitor trigger " + task.getId());
		try
		{
			scheduler.pauseTrigger(triggerKey);
			scheduler.unscheduleJob(triggerKey);
			scheduler.deleteJob(JobKey.jobKey("heartbeat task monitor " + task.getId()));
		}
		catch (SchedulerException exception)
		{
			exception.printStackTrace();
		}

		taskMonitor.getCancel().compareAndSet(true, false);
		JobDetail jobDetail = JobBuilder.newJob(HeartbeatTaskMonitorRunner.class)
				.withIdentity("heartbeat task monitor " + task.getId()).build();
		jobDetail.getJobDataMap().put("heartbeat task monitor instance", taskMonitor);
		jobDetail.getJobDataMap().put("heartbeatd service", this);
		SimpleTrigger trigger = TriggerBuilder.newTrigger().withIdentity("heartbeat task monitor trigger " + task.getId())
				.startAt(new Date(new Date().getTime() + (task.getPeriod() + 1) * 1000))
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(task.getPeriod() + 1)
						.repeatForever()).build();
		try
		{
			scheduler.scheduleJob(jobDetail, trigger);
		}
		catch (SchedulerException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 心跳丢失监测一次，当超过间隔时间仍未收到心跳消息，调用此方法
	 *
	 * @param task 心跳监测任务
	 */
	@Override
	public void lostHeartbeatOnce(HeartbeatTask task)
	{
		HeartbeatTaskMonitor monitor = heartbeatHolder.get(task);
		if (!monitor.getLosted().get())
		{
			if (monitor.decrementRemainsAndGet() == 0)
			{
				monitor.getLosted().compareAndSet(false, true);
				task.getHeartbeatLostHandler().doWhenLost(task);
			}
		}
		else
			monitor.decrementRemainsAndGet();

	}

	/**
	 * 创建一个心跳监测任务监视器
	 * @param task 心跳监测任务
	 * @return 心跳监测任务监视器
	 */
	private HeartbeatTaskMonitor createMonitor(HeartbeatTask task)
	{
		HeartbeatTaskMonitor monitor = new HeartbeatTaskMonitor();
		monitor.setTask(task);
		monitor.setThreshold(new AtomicInteger(task.getTimes()));
		monitor.setRemains(new AtomicInteger(task.getTimes()));
		return monitor;
	}
}
