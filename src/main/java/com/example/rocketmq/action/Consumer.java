package com.example.rocketmq.action;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Consumer {

    public static void main(String[] args) throws MQClientException {
        //创建consumer默认有两种方式：
        /*  DefaultMQPullConsumer：Consumer连接服务器端broker后，轮询请求获取消息
            DefaultMQPushConsumer(常用)：服务器端broker接收到消息后，主动将消息发给Consumer。
                    实际上push也是由Consumer主动拉取请求实现的；Consumer将 长轮询 过程封装了，并注册一个MessageListener监听,
                    获取消息后，唤醒messageListener里面的consumeMessage（）消费。
                    所以使用的时候，就像是服务器端主动推送一样
        */
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("push-group");

        //设置nameServer地址，如果有多个，可以用分号隔开
        consumer.setNamesrvAddr("localhost:9876");
        //设置instanceName
        consumer.setInstanceName("rem-instance");
        //订阅指定topic和tag，订阅并不会创建topic，是由Producer创建topic的
        consumer.subscribe("log-topic", "user-tag");
        //监听：有消息，唤醒监听调动ConsumerMessage
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgList, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for(MessageExt msg : msgList){
                    System.out.println("消费者数据：" + new String(msg.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }

}
