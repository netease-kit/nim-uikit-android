package com.netease.nim.uikit.common.ui.barrage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.netease.nim.uikit.common.util.sys.ScreenUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * 弹幕控件
 * <p>
 * Created by huangjun on 2016/5/8.
 */
public class BarrageSurfaceView extends SurfaceViewTemplate {

    private static final String TAG = "BarrageSurfaceView";

    private static final boolean OUTPUT_LOG = true;

    private static final int DEFAULT_RANDOM_COLOR_NUM = 30;

    private static final int TIME_INTERVAL = 30;

    private static final int MESSAGE_FREE_LINE = 0x01;

    private static final int MESSAGE_END = 0x02;

    private static final String MESSAGE_DATA_LINE = "LINE";

    private Random random;

    // 配置管理
    private BarrageConfig config;

    // 轨道管理
    private Set<Integer> linesUnavailable = new HashSet<>();
    private int lineCount;
    private int lineHeight;

    // 字幕管理
    private Queue<String> textCache = new LinkedList<>();

    // 执行者管理（多线程访问）
    private final List<BarrageTextTask> tasks = new LinkedList<>();

    public BarrageSurfaceView(Context context) {
        super(context);
    }

    public BarrageSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(final BarrageConfig config) {
        this.random = new Random();
        int totalLineHeight = getBottom() - getTop() - getPaddingTop() - getPaddingBottom();
        this.lineHeight = ScreenUtil.sp2px(config.getMaxTextSizeSp());
        this.lineCount = totalLineHeight / lineHeight;

        // random colors
        if (config.getColors() == null || config.getColors().isEmpty()) {
            List<Integer> colors = new ArrayList<>(DEFAULT_RANDOM_COLOR_NUM);
            for (int i = 0; i < DEFAULT_RANDOM_COLOR_NUM; i++) {
                colors.add(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            }
            config.setColors(colors);
        }
        this.config = config;

        log("barrage init, lineHeight=" + lineHeight + ", lineCount=" + lineCount);
    }

    public void addTextBarrage(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        textCache.add(text);

        checkAndRunTextBarrage();
    }

    private void checkAndRunTextBarrage() {
        if (textCache.isEmpty()) {
            return;
        }

        int availableLine = getAvailableLine();
        if (availableLine < 0) {
            return; // pend
        }

        BarrageTextTask task = buildBarrageTextTask(textCache.poll(), availableLine);
        synchronized (tasks) {
            tasks.add(task);
        }

        notifyHasTask(); // 通知有绘制任务
    }

    private int getAvailableLine() {
        int line = -1;
        for (int i = 0; i < lineCount; i++) {
            if (!linesUnavailable.contains(i)) {
                line = i;
                break;
            }
        }

        if (line >= 0 && line < lineCount) {
            linesUnavailable.add(line); // 占用
        }

        return line;
    }

    private BarrageTextTask buildBarrageTextTask(String text, int line) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        // text size, length
        int size = config.getMinTextSizeSp() + random.nextInt(config.getMaxTextSizeSp() - config.getMinTextSizeSp() + 1);
        size = ScreenUtil.sp2px(size);

        // text color
        int color;
        if (config.getColors() != null && !config.getColors().isEmpty()) {
            color = config.getColors().get(random.nextInt(config.getColors().size()));
        } else {
            color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }

        // duration
        int duration = config.getDuration() + random.nextInt() % 500;

        // start position
        float x = getWidth();
        float y = size + line * lineHeight;

        // speed
        float deltaX = (1.0f * getWidth() / duration) * TIME_INTERVAL;

        // log
        StringBuilder log = new StringBuilder();
        log.append("build text barrage task")
                .append(", line=").append(line)
                .append(", text=").append(text)
                .append(", speed=").append(deltaX);
        log(log.toString());

        return new BarrageTextTask(text, line, color, size, duration, x, y, deltaX);
    }

    private Handler mHandler = new Handler(getContext().getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_FREE_LINE:
                    onLineAvailable(msg.getData().getInt(MESSAGE_DATA_LINE));
                    break;
                case MESSAGE_END:
                    onTextBarrageDone(msg.getData().getInt(MESSAGE_DATA_LINE));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private void onLineAvailable(final int line) {
        log("free line, line=" + line);

        linesUnavailable.remove(line);
        checkAndRunTextBarrage();
    }

    private void onTextBarrageDone(final int line) {
        log("text barrage completed, line=" + line);

        checkAndRunTextBarrage();
    }


    /**
     * ******************************** 在线程中执行绘制 ************************************
     */
    @Override
    public void onDrawView(Canvas canvas) {
        synchronized (tasks) {
            if (tasks.size() <= 0) {
                return;
            }

            Iterator<BarrageTextTask> iterator = tasks.iterator();
            BarrageTextTask task;
            while (iterator.hasNext()) {
                task = iterator.next();
                // draw
                task.updatePosition();
                canvas.drawText(task.getText(), task.getX(), task.getY(), task.getPaint());

                // check end
                if (task.isEnd()) {
                    iterator.remove(); // reach the end，remove
                    Message message = new Message();
                    message.what = MESSAGE_END;
                    Bundle data = new Bundle();
                    data.putInt(MESSAGE_DATA_LINE, task.getLine());
                    message.setData(data);
                    mHandler.sendMessage(message);
                    continue;
                }

                // check free line
                if (task.canFreeLine()) {
                    Message message = new Message();
                    message.what = MESSAGE_FREE_LINE;
                    Bundle data = new Bundle();
                    data.putInt(MESSAGE_DATA_LINE, task.getLine());
                    message.setData(data);
                    mHandler.sendMessage(message);
                }
            }
        }
    }

    @Override
    public int getRunTimeInterval() {
        return TIME_INTERVAL;
    }

    @Override
    protected boolean hasTask() {
        boolean hasTask;
        synchronized (tasks) {
            hasTask = tasks.size() > 0;
        }

        return hasTask;
    }

    private void log(String message) {
        if (OUTPUT_LOG) {
            Log.i(TAG, message);
        }
    }
}
