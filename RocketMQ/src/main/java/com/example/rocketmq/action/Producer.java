package com.example.rocketmq.action;


import com.alibaba.fastjson.JSON;
import com.example.rocketmq.bean.UserContent;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.stereotype.Component;

@Component
public class Producer {


    public static void main(String[] args) throws MQClientException {
        //生产者
        DefaultMQProducer producer = new DefaultMQProducer("test-group");
        producer.setNamesrvAddr("192.168.43.68:9876");//添加nameServer地址
        //默认情况下不设置instanceName，会使用ip@pid(pid代表jvm名字)作为唯一标识
        //如果同一个jvm中，不同的producer往不同的RocketMQ集群发送消息，需要不同的instanceName
        producer.setInstanceName("rem-instance");
        //启动mq-producer，做一些初始化、连接的事
        producer.start();
        try {
            for(int i=0; i<5; i++){
                UserContent user =  new UserContent("abc"+i, String.valueOf(i));
                //创建消息，指定Topic和tag，第三个参数是消息内容
                Message message = new Message("log-topic", "user-tag", JSON.toJSONString(user).getBytes());
                System.out.println("生产者发送消息：" + JSON.toJSONString(user));
                //同步发送消息
                producer.send(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //关闭
        producer.shutdown();
    }

}

