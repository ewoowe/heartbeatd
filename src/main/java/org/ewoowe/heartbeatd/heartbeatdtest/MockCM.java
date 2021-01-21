package org.ewoowe.heartbeatd.heartbeatdtest;

import org.ewoowe.heartbeatd.HeartbeatTask;
import org.ewoowe.heartbeatd.HeartbeatdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MockCM
{
    HeartbeatHandler heartbeatHandler = new HeartbeatHandler();

    HeartbeatTask task;

    @Autowired
    private HeartbeatdService heartbeatd;

    @PostConstruct
    public void init() throws InterruptedException
    {
        task = new HeartbeatTask();
        task.setHeartbeatLostHandler(heartbeatHandler);
        task.setHeartbeatRecoverHandler(heartbeatHandler);
        task.setTimes(3);
        task.setPeriod(5);
        heartbeatd.registerHeartbeatTask(task);
    }

    public HeartbeatHandler getHeartbeatHandler()
    {
        return heartbeatHandler;
    }

    public HeartbeatTask getTask()
    {
        return task;
    }
}
