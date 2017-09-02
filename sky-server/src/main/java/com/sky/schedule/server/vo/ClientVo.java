package com.sky.schedule.server.vo;

/**
 * Created by gantianxing on 2017/9/2.
 */
public class ClientVo {

    /**
     * 客户端id
     */
    private String id;

    /**
     * 客户端分配的任务号
     */
    private Integer nodeNum;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(Integer nodeNum) {
        this.nodeNum = nodeNum;
    }
}
