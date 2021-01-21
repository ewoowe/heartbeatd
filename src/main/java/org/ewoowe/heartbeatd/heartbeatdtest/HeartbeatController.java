package org.ewoowe.heartbeatd.heartbeatdtest;

import org.ewoowe.heartbeatd.HeartbeatdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test")
public class HeartbeatController
{
    @Autowired
    private HeartbeatdService heartbeatd;

    @Autowired
    private MockCM cm;

    @RequestMapping("/notificationSink")
    @ResponseBody
    public void receiveHeartbeat()
    {
        heartbeatd.haveHeartbeatOnce(cm.getTask());
    }
}
