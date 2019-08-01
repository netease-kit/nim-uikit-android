package com.netease.nim.uikit.common.framework.infra;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 控制任务执行频率，允许调用根据任务类型，设定最小的执行间隔。
 * 任务类型默认就是任务的类名，如果同一种任务需要再细分，由用户提供自定义的tag
 */
public class TaskFrequencyController {

    private static Map<String, Long> taskTime = new HashMap<String, Long>();

    public static <T> T runTask(ControllableTask<T> task, int frequencyInS, T def) {
        String key = makeKey(task);
        Long lastTime = taskTime.get(key);
        long current = new Date().getTime() / 1000;
        if (lastTime != null && current - lastTime < frequencyInS) {
            return def;
        }

        taskTime.put(key, current);
        return task.run();
    }

    public static boolean runTask(VoidControllableTask task, int frequencyInS) {
        String key = makeKey(task);
        Long lastTime = taskTime.get(key);
        long current = new Date().getTime() / 1000;
        if (lastTime != null && current - lastTime < frequencyInS) {
            return false;
        }

        taskTime.put(key, current);
        task.run();
        return true;
    }

    public static void reset() {
        taskTime.clear();
    }

    private static interface IControllableTask {
        public String tag();
    }

    public static abstract class ControllableTask<T> implements IControllableTask {
        public String tag() {
            return getClass().getSimpleName();
        }

        public abstract T run();
    }

    public static abstract class VoidControllableTask implements IControllableTask {
        public String tag() {
            return getClass().getSimpleName();
        }

        public abstract void run();
    }

    private static String makeKey(IControllableTask task) {
        return task.getClass().getName() + "#" + task.tag();
    }
}
