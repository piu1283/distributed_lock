package com.priv.lock.distributed_lock.redis.worker;

import com.priv.lock.distributed_lock.redis.service.AddingService;

public class AddingWorker implements Runnable {

    AddingService addingService;

    // simulate the GC or long time execution
    // in second
    private int executingDuration;

    private String key;

    private int executingTimes;

    public AddingWorker(int executingTime, int duration, String key, AddingService service) {
        this.executingTimes = executingTime;
        this.executingDuration = duration;
        this.key = key;
        this.addingService = service;
    }

    @Override
    public void run() {
        for (int i = 0; i < executingTimes; i++) {
            addingService.addOne(key, executingDuration);
        }
    }
}
