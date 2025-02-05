package com.easysocket.connection.reconnect;

import com.easysocket.entity.SocketAddress;
import com.easysocket.entity.IsReconnect;
import com.easysocket.utils.LogUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author：Alex
 * Date：2019/5/28
 * Note：默认的重连管理器
 */
public class DefaultReConnection extends AbsReconnection {
    /**
     * 最大连接失败次数，超过可以切换到备用的服务器地址
     */
    private static final int MAX_CONNECTION_FAILED_TIMES = 10;
    /**
     * 连接失败的次数
     */
    private int connectionFailedTimes = 0;
    /**
     * 重连的时间不能小于10秒
     */
    private long reconnectTimeDelay = 10 * 1000;
    /**
     * 重连线程管理器
     */
    private ScheduledExecutorService reConnExecutor;

    public DefaultReConnection() {
    }

    /**
     * 重连任务
     */
    private final Runnable RcConnTask = new Runnable() {
        @Override
        public void run() {
            LogUtil.d("执行重连任务");
            if (isDetach) {
                shutDown();
                return;
            }
            //是否处于可连接状态
            if (!connectionManager.isConnectViable()) {
                shutDown();
                return;
            }
            if (reconnectTimeDelay < connectionManager.getOptions().getConnectTimeout())
                reconnectTimeDelay = connectionManager.getOptions().getConnectTimeout();
            //连接
            connectionManager.connect();
        }
    };

    /**
     * 开始进行重连
     */
    private void reconnect() {
        if (reConnExecutor == null || reConnExecutor.isShutdown()) {
            reConnExecutor = Executors.newSingleThreadScheduledExecutor();
            reConnExecutor.scheduleWithFixedDelay(RcConnTask, 0, reconnectTimeDelay, TimeUnit.MILLISECONDS);
        }
    }

    //关闭重连线程
    private void shutDown() {
        if (reConnExecutor != null && !reConnExecutor.isShutdown()) {
            reConnExecutor.shutdownNow();
            reConnExecutor = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        //getClass返回Class类型的对象，比较它们的类型对象是否==，其实比较它们是否是同一个Class创建的对象
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public void onSocketConnSuccess(SocketAddress socketAddress) {
        shutDown();
    }

    @Override
    public void onSocketConnFail(SocketAddress socketAddress, IsReconnect isReconnect) {
        if (!isReconnect.booleanValue()) {
            shutDown();
            return;
        }
        connectionFailedTimes++;
        //如果大于最大连接次数，则可使用备用host,然后轮流切换两个host
        if (connectionFailedTimes > MAX_CONNECTION_FAILED_TIMES && socketAddress.getBackupAddress() != null) {
            connectionFailedTimes = 0; //归零
            SocketAddress backupAddress = socketAddress.getBackupAddress();
            SocketAddress bbAddress = new SocketAddress(socketAddress.getIp(), socketAddress.getPort());
            backupAddress.setBackupAddress(bbAddress);
            if (connectionManager.isConnectViable()) {
                connectionManager.switchHost(backupAddress);
                reconnect();
            }
        } else {
            reconnect();
        }

    }

    @Override
    public void onSocketDisconnect(SocketAddress socketAddress, IsReconnect isReconnect) {
        if (!isReconnect.booleanValue()) {
            shutDown();
            return;
        }
        reconnect();
    }
}
