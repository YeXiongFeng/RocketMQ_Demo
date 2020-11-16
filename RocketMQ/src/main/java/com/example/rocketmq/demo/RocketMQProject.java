package com.example.rocketmq.demo;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class RocketMQProject {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping(value = "/")
    public String defaultPage(){
        /*ValueOperations<String, String> op = redisTemplate.opsForValue();
        op.set("surplusNumber", "100");*/
        System.out.println("秒杀商品的数量已写入Redis库存");
        return "index";
    }


    /**
     * 收到秒杀请求时，检查库存数量
     *      1.库存不足，直接返回失败
     *      2.库存充足，提交下单请求到rocketMQ，并返回正在抢购页面
     *      3.异步成功后再返回订单，生成订单，减少库存
     * @return
     */
    @RequestMapping("/seckill")
    @ResponseBody
    public String seckill(Integer seckillNumber) throws MQClientException, RemotingException, InterruptedException {
        //预扣减Redis库存（就是检查库存是否充足）
        ValueOperations<String, String> op = redisTemplate.opsForValue();
        //获取剩余库存
        Integer surplusNumber = Integer.parseInt(op.get("surplusNumber"));
        //抢购数量判断
        if(surplusNumber < seckillNumber){
            return "库存数量不足，抢购失败";
        }

        //发送异步消息
        DefaultMQProducer producer = new DefaultMQProducer("group1");
        producer.setNamesrvAddr("192.168.43.68:9876");
        producer.start();
        String messageStr = UUID.randomUUID().toString();
        Message message = new Message("seckill_topic", "seckill_tag", messageStr.getBytes());
        System.out.println("正在抢购商品，预订单号：" + messageStr);
        //异步回调
        producer.send(message, new SendCallback() {
            /**
             * 发送成功回调
             * @param sendResult
             */
            @Override
            public void onSuccess(SendResult sendResult) {
                //获取剩余库存
//                ValueOperations<String, String> op = redisTemplate.opsForValue();
//                Integer surplusNumber = Integer.parseInt(op.get("surplusNumber"));
                op.set("surplusNumber", String.valueOf(surplusNumber-seckillNumber));
                System.out.println("秒杀成功，扣除秒杀商品数量后，剩余：" + (surplusNumber-seckillNumber) );
            }

            /**
             * 发送异常回调
             * @param throwable
             */
            @Override
            public void onException(Throwable throwable) {
                System.out.println("发送失败："+throwable.toString());
            }
        });
        //等待两秒关闭，让它发送完成
        Thread.sleep(2000);
        producer.shutdown();

        return "抢购成功，订单号："+messageStr;
    }

}
