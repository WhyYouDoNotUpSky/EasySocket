package com.easysocket.callback;


import com.easysocket.entity.sender.SuperCallbackSender;
import com.google.gson.Gson;

/**
 * Created by LXR ON 2018/8/29.
 */
public abstract class SimpleCallBack<T> extends SuperCallBack<T> {


    public SimpleCallBack(SuperCallbackSender sender) {
        super(sender);
        onStart();
    }

    @Override
    public void onStart() {
        openTimeoutTask(); //开始超时任务
    }

    @Override
    public void onCompleted() {
        closeTimeoutTask(); //停止超时任务
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onSuccess(String s) {
        onCompleted();
        Class<?> clazz = getClazz();
        if (clazz.equals(String.class)) { //泛型是字符串类型
            onResponse((T) s);
        } else { //非string
            Gson gson = new Gson();
            T result = (T) gson.fromJson(s, clazz);
            onResponse(result);
        }
    }

    public abstract void onResponse(T t);

}
