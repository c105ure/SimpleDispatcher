package com.klaus.simpledispatcher;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 最简单的调度器
 * fixme 防止内存泄露
 */
public class SimpleDispatcher {

    //调度器静态单例
    static SimpleDispatcher instance = new SimpleDispatcher();

    public static final int CORE_POOL_SIZE = 32;

    //获取单例
    public static SimpleDispatcher getInstance(){
        if(instance == null){
            synchronized (SimpleDispatcher.class){
                instance = new SimpleDispatcher();
            }
        }
        return instance;
    }

    //静态初始化
    public static SimpleDispatcher newInstance(SimpleDispatcher simpleDispatcher){
        if(instance!=null){
            instance.mHandler.removeCallbacksAndMessages(null);
            instance.mExecutorService.shutdownNow();
        }
        instance = simpleDispatcher;
        return instance;
    }

    //Runnable的启动模式
    public enum Mode {
        IO,         //executed in an IO thread
        Sub,        //executed in a new thread
        Main        //executed in main thread
    }

    //消息处理器
    private final Handler mHandler;

    //线程池
    private final ExecutorService mExecutorService;

    //默认构造器
    public SimpleDispatcher(){
        this(Looper.getMainLooper());
    }

    public SimpleDispatcher(Looper looper){
        this(looper,CORE_POOL_SIZE);
    }

    public SimpleDispatcher(Looper looper,int corePoolSize){
        this(looper, Executors.newScheduledThreadPool(corePoolSize));
    }

    public SimpleDispatcher(Looper looper,ExecutorService executorService){
        this.mHandler = new Handler(looper);
        this.mExecutorService = executorService;
    }

    //启动方法
    public void launch(Mode mode,Runnable runnable){
        switch (mode){
            case IO:
                mExecutorService.execute(runnable);
                break;
            case Sub:
                runnable.run();
                break;
            case Main:
                mHandler.post(runnable);
                break;
        }
    }

    /**
     * Mini Dispatcher
     */
    public static class MiniDispatcher extends SimpleDispatcher{

        static void init(SimpleDispatcher simpleDispatcher){
            newInstance(simpleDispatcher);
        }

        static void run(Mode mode,Runnable runnable){
            getInstance().launch(mode,runnable);
        }
    }
}
