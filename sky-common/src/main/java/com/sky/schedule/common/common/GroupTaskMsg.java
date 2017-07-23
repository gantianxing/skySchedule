package com.sky.schedule.common.common;


import com.sky.schedule.common.base.BaseMsg;
import com.sky.schedule.common.base.MsgType;

/**
 * 处理集群任务的类
 * Created by zengqijuan on 2017/3/31.
 */
public class GroupTaskMsg extends BaseMsg {


    public GroupTaskMsg(){
        setType(MsgType.GROUP_TASK);
    }

    /**
     * engine总节点数
     */
    private Integer totalNode;

    /**
     * 当前节点编号
     */
    private Integer nodeNum;

    public Integer getTotalNode() {
        return totalNode;
    }

    public void setTotalNode(Integer totalNode) {
        this.totalNode = totalNode;
    }

    public Integer getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(Integer nodeNum) {
        this.nodeNum = nodeNum;
    }
}
