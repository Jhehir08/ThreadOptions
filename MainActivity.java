package com.example.android.threads1;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.displayThreadType)
    TextView textView;
    @BindView(R.id.button)
    Button goThread;
    @BindView(R.id.button2)
    Button goAsync;
    @BindView(R.id.button3)
    Button goEventbus;
    @BindView(R.id.button4)
    Button goRx;
    @BindView(R.id.button5)
    Button goBroadcast;
    Subscription mySubscription;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    //////////////////////Thread///////////////////////////////

    @OnClick(R.id.button3)
    public void onEventBus(View view) {

        new Thread() { // Create new thread
            public void run() { // start thread
                try {
                    Thread.sleep(3000); // after 3 seconds
                    EventBus.getDefault().post(new MyEvent("Done with Event Bus", 747)); // send this message
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @OnClick(R.id.button)
    public void onThread(View view) {

        new Thread() { // <<< anonymous class
            public void run() {
                try {
                    Thread.sleep(2000);
                    //Runnable runs in a seperate thread
                    textView.post(new Runnable() { // simplest way to create a thread
                        @Override
                        public void run() {
                            textView.setText("Done with thread");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //////////////////////Thread///////////////////////////////

    /////////////////////////asyncTask/////////////////////////

    @OnClick(R.id.button2)
    public void onAsyncTask(View view) { //uses generics - 3 kinds: onPreExecute do in background, onPostExecute, onProgress

        new AsyncTask<Integer, Void, String>() { //autoboxing
            @Override
            protected String doInBackground(Integer[] params) {

                int timeToSleep = params[0] * 1000;
                try {
                    Thread.sleep(timeToSleep);
                    return "Done with AsyncTask " + timeToSleep;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return "AsyncTask Error";
                }
            }
            @Override
            protected void onPostExecute(String s) { // runs in UI
                super.onPostExecute(s);
                textView.setText(s);
            }
        }.execute(2); //sends to doInBackground

    }

    /////////////////////////asyncTask///////////////////////////////

    /////////////////////////Event Bus///////////////////////////////

    // can start event and anywhere else in code you can run that event
    // Event bus uses observable design pattern
    private class MyEvent {

        private String message;
        private int messageCode;

        public MyEvent(String message, int messageCode) {
            this.message = message;
            this.messageCode = messageCode;
        }

        // this method makes our message receivable
        public String getMessage() {
            return message;
        }

        public int getMessageCode() {
            return messageCode;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void myEventHandler(MyEvent e) {
        // use message code to know which message because you could call messages from different place
        if (e.getMessageCode() == 747)
            textView.setText(e.getMessage());
    }

    // have to register and unregister
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        // unregiter broadcase receiver as well
        unregisterReceiver(broadcastReceiver);
    }

    /////////////////////////Event Bus//////////////////////////////

    //////////////////////////RX Java///////////////////////////////

    @OnClick (R.id.button4)
    public void onRxJava(View view) {  // observer subscribes with observable

        Observable<Integer> myObservable = Observable.fromCallable(
                new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        Thread.sleep(4000);

                        return 4;
                    }
                }
        );

        mySubscription = myObservable.subscribeOn(Schedulers.newThread()) // builder pattern
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        textView.setText("Done with RxJava: " + integer);

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mySubscription.isUnsubscribed()) {
            mySubscription.unsubscribe();
        }
    }

    //////////////////////////RX Java///////////////////////////////

    //////////////////////////Broadcast Receiver////////////////////

    @OnClick(R.id.button5)
    public void onBroadcastReceiver(View view) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.android.threads1.MyAction");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                textView.setText("Done with Broadcast Receiver");
            }
        };

        registerReceiver(broadcastReceiver, intentFilter);

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(3000);
                    Intent i = new Intent();
                    i.setAction("com.example.android.threads1.MyAction");
                    sendBroadcast(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    //////////////////////////Broadcast Receiver///////////////////////////////
}

