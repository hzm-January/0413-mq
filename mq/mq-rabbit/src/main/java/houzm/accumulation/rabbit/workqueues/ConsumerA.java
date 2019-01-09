package houzm.accumulation.rabbit.workqueues;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

/**
 * author: hzm_dream@163.com
 * date: 2019/1/9 13:39
 * description:
 * 消息消费者
 */
public class ConsumerA {
    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        //连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("47.101.152.55");
        connectionFactory.setUsername("houzm");
        connectionFactory.setPassword("houzm");
        //创建连接
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        DeliverCallback deliverCallback = new DeliverCallback() {
            @Override
            public void handle(String consumerTag, Delivery delivery) throws IOException {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                try {
                    doWork(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            }
        };
//        boolean autoAck = true; //自动确认
        boolean autoAck = false; //手动确认
        channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> {
        });


        //[x] Received 'the 2 message ..'
        //[x] Done
        //[x] Received 'the 4 message ....'
        //[x] Done
        //[x] Received 'the 6 message ......'
        //[x] Done
        //[x] Received 'the 8 message ........'
        //[x] Done
        //[x] Received 'the 10 message ..........'
        //[x] Done
    }

    /**
     * 模拟生产环境中每个消息的执行
     *
     * @param message
     */
    private static void doWork(String message) throws InterruptedException {
        if (message != null && message.trim().length() > 0) {
            for (char aChar : message.toCharArray()) {
                if (aChar == '.') {
                    TimeUnit.SECONDS.sleep(1);
                }
            }
        }
    }


}
