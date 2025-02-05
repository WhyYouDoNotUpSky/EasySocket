package com.easysocket.interfaces.conn;

import com.easysocket.entity.sender.ISender;

/**
 * Author：Alex
 * Date：2019/6/5
 * Note：发送接口
 */
public interface ISend {
    /**
     * 发送字符串
     * @param sender
     * @return
     */
    IConnectionManager upString(String sender);

    /**
     * 发送对象
     * @param sender
     * @return
     */
    IConnectionManager upObject(ISender sender);

    /**
     * 发送bytes
     * @param bytes
     * @return
     */
    IConnectionManager upBytes(byte[] bytes);
}
