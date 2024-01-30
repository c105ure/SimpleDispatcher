package com.klaus.simpledispatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "DispatcherTest";

    public static final int WHAT_THREAD = 0x01;

    private TextView tvLog;

    private Handler mHandler;

    public static final int CORE_POOL_SIZE = 16;
    public static final int MAX_POOL_SIZE = 32;

    private ExecutorService mExecutorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }


    protected void initViews() {
        tvLog = findViewById(R.id.tv_log_dsp);

        mExecutorService = new ThreadPoolExecutor(CORE_POOL_SIZE,MAX_POOL_SIZE,2000, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(CORE_POOL_SIZE));

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if(msg.what == WHAT_THREAD){
                    appendLog("(Async)" +msg.obj.toString() ,false);
                }
                return false;
            }
        });
    }

    public void dspMainToSub(View view) {
        appendLog("MainToSub(0):"+Thread.currentThread().getName()+";\t",false);

        Mind.run(SimpleDispatcher.Mode.Sub, new Runnable() {
            @Override
            public void run() {
                appendLog("MainToSub(1):"+Thread.currentThread().getName()+"\n",false);
            }
        });
    }

    public void dspMainToIO(View view) {
        appendLog("MainToIO(0):"+Thread.currentThread().getName()+";\t",false);

        Mind.run(SimpleDispatcher.Mode.IO, new Runnable() {
            @Override
            public void run() {
                appendLog("MainToIO(1):"+Thread.currentThread().getName()+"\n",true);
            }
        });
    }

    public void dspSubToMain(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                appendLog("SubToMain(0):"+Thread.currentThread().getName()+";\t",true);

                Mind.run(SimpleDispatcher.Mode.Main,new Runnable() {
                    @Override
                    public void run() {
                        appendLog("SubToMain(1):"+Thread.currentThread().getName()+"\n",false);
                    }
                });
            }
        }).start();
    }

    public void dspSubToIO(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                appendLog("SubToIO(0):"+Thread.currentThread().getName()+";\t",true);
                SimpleDispatcher.getInstance().launch(SimpleDispatcher.Mode.IO, new Runnable() {
                    @Override
                    public void run() {
                        appendLog("SubToIO(1):"+Thread.currentThread().getName()+"\n",true);
                    }
                });
            }
        }).start();
    }

    public void dspSubToSub(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                appendLog("SubToSub(0):"+Thread.currentThread().getName()+";\t",true);
                SimpleDispatcher.getInstance().launch(SimpleDispatcher.Mode.Sub, new Runnable() {
                    @Override
                    public void run() {
                        appendLog("SubToSub(1):"+Thread.currentThread().getName()+"\n",true);
                    }
                });
            }
        }).start();
    }

    public void dspIoToMain(View view) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                appendLog("IoToMain(0):"+Thread.currentThread().getName()+";\t",true);
                SimpleDispatcher.getInstance().launch(SimpleDispatcher.Mode.Main, new Runnable() {
                    @Override
                    public void run() {
                        appendLog("IoToMain(1):"+Thread.currentThread().getName()+"\n",false);
                    }
                });
            }
        });
    }

    /**
     * 子线程到IO线程到主线程
     * @param view view
     */
    public void dspSubToIoToMain(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkThreadLog("SubToIo(0):",false,true);
                Mind.run(SimpleDispatcher.Mode.IO, new Runnable() {
                    @Override
                    public void run() {
                        checkThreadLog("IoToMain(1)",false,true);
                        Mind.run(SimpleDispatcher.Mode.Main, new Runnable() {
                            @Override
                            public void run() {
                                checkThreadLog("SubToIoToMain(2)",true,false);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void checkThreadLog(String tag,boolean end,boolean async){
        appendLog(tag+Thread.currentThread().getName()+(end?"\n":";\t"),async);
    }

    private void appendLog(String log,boolean async){
        if(async){
            mHandler.sendMessage(mHandler.obtainMessage(WHAT_THREAD,log));
        }else{
            tvLog.append(log);
        }
    }

    public void btnClear(View view) {
        tvLog.setText(null);
    }
}