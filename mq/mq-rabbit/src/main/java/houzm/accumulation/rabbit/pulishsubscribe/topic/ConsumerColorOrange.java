package houzm.accumulation.rabbit.pulishsubscribe.topic;

import houzm.accumulation.rabbit.common.ServerInfo;
import java.io.IOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

/**
 * Author: hzm_dream@163.com
 * Date:  2019/1/12 11:41
 * Modified By:
 * Description：ConsumerColorOrange
 */
public class ConsumerColorOrange {

    public static final String EXCHANGES_TOPIC_LOGS = "exchanges_topic_logs";

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(ServerInfo.SERVER_HOST);
        connectionFactory.setUsername(ServerInfo.SERVER_USERNAME);
        connectionFactory.setPassword(ServerInfo.SERVER_PASSWORD);
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGES_TOPIC_LOGS, "topic");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGES_TOPIC_LOGS, "*.orange.*");
        DeliverCallback deliverCallback = new DeliverCallback() {
            @Override
            public void handle(String consumerTag, Delivery delivery) throws IOException {
                System.out.println(new String(delivery.getBody(), "UTF-8"));
                //手动确认消息
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
            }
        };
        channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {});
    }

}
