package houzm.accumulation.rabbit.workqueues.prefetch;

import houzm.accumulation.rabbit.common.ServerInfo;
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
 *
 * prefetch 预取设置方式：
 * 在消费端设置 channel.basicQos()
 *
 */
public class Consumer {
    private static final String QUEUE_NAME = "task_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        //连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(ServerInfo.SERVER_HOST);
        connectionFactory.setUsername(ServerInfo.SERVER_USERNAME);
        connectionFactory.setPassword(ServerInfo.SERVER_PASSWORD);
        //创建连接
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        boolean durable = true; //持久化
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
        channel.basicQos(1); // prefetch count
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
                    boolean multiple = true; //批量消息确认
                    //通知RabbitMQ，任务执行成功
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), multiple);
                    //消息消极确认，任务执行未成功，通知RabbitMQ消息可以被删除，
//                    channel.basicReject(delivery.getEnvelope().getDeliveryTag(), multiple);
                    //通知RabbitMQ，任务执行失败
//                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, multiple);
                }
            }
        };
//        boolean autoAck = true; //自动确认
        boolean autoAck = false; //手动确认
        channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> {});
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

    //[x] Received 'the 1 message .'
    //[x] Done
    //[x] Received 'the 3 message ...'
    //[x] Done
    //[x] Received 'the 5 message .....'
    //[x] Done
    //[x] Received 'the 7 message .......'
    //[x] Done
    //[x] Received 'the 9 message .........'
    //[x] Done

}
