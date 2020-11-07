package com.example.rocketmq.action;

import com.alibaba.fastjson.JSON;
import com.example.rocketmq.bean.UserContent;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;

@Component
public class UserProducer {

    //生产者的组名
    @Value("${suning.rocketmq.producerGroup}")
    private String producerGroup;
    //nameServer地址
    @Value("${suning.rocketmq.namesrvaddr}")
    private String namesrvAddr;

    @PostConstruct
    public void producer(){
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(namesrvAddr);
        try {
            producer.start();
            for (int i = 0; i < 100; i++) {
                UserContent userContent = new UserContent(String.valueOf(i), "abc" + i);
                String jsonstr = JSON.toJSONString(userContent);
                System.out.println("发送消息：" + jsonstr);
                Message message = new Message("user-topic", "user-tag", jsonstr.getBytes(RemotingHelper.DEFAULT_CHARSET));
                SendResult result = producer.send(message);
                System.out.println("发送相应：MsgId：" + result.getMsgId() + "，发送状态：" + result.getSendStatus());
            }
        } catch (MQClientException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } finally{
            producer.shutdown();
        }
    }


}
