package houzm.accumulation.rabbit.pulishsubscribe.fanout;

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
 * 消息消费者A
 * exchanges fanout
 *
 *
 */
public class ConsumerA {
    public static final String EXCHANGES_NAME = "logs";
    public static final int PREFETCH_COUNT = 1;

    public static void main(String[] args) throws IOException, TimeoutException {
        //连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(ServerInfo.SERVER_HOST);
        connectionFactory.setUsername(ServerInfo.SERVER_USERNAME);
        connectionFactory.setPassword(ServerInfo.SERVER_PASSWORD);
        //创建连接
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        //定义exchanges
        channel.exchangeDeclare(EXCHANGES_NAME, "fanout");
        channel.basicQos(PREFETCH_COUNT);
        //获取独占自动删除的临时队列
        String queueName = channel.queueDeclare().getQueue();
        //绑定exchanges和queue
        channel.queueBind(queueName, EXCHANGES_NAME, "");
        DeliverCallback deliverCallback = new DeliverCallback() {
            @Override
            public void handle(String consumerTag, Delivery delivery) throws IOException {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                boolean multiple = true; //批量消息确认
                //通知RabbitMQ，任务执行成功
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), multiple);
                //消息消极确认，任务执行未成功，通知RabbitMQ消息可以被删除，
//                    channel.basicReject(delivery.getEnvelope().getDeliveryTag(), multiple);
                //通知RabbitMQ，任务执行失败
//                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, multiple);
            }
        };
//        boolean autoAck = true; //自动确认
        boolean autoAck = false; //手动确认
        channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {});
    }


}
