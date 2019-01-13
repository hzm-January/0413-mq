package houzm.accumulation.rabbit.pulishsubscribe.topic;

import houzm.accumulation.rabbit.common.ServerInfo;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Author: hzm_dream@163.com
 * Date:  2019/1/12 11:22
 * Modified By:
 * Description： producer
 */
public class Producer {
    public static final String EXCHANGES_DIRECT_LOG = "exchanges_topic_logs";
    public static final String [] messages = new String[]{"quick.orange.rabbit", "lazy.orange.elephant", "quick.orange.fox",
            "lazy.pink.rabbit", "quick.brown.fox", "quick.orange.male.rabbit", "lazy.orange.male.rabbit"};

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(ServerInfo.SERVER_HOST);
        connectionFactory.setUsername(ServerInfo.SERVER_USERNAME);
        connectionFactory.setPassword(ServerInfo.SERVER_PASSWORD);
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGES_DIRECT_LOG, "topic");
            for (int i = 0; i < messages.length; i++) {
                String message = messages[i];
                channel.basicPublish(EXCHANGES_DIRECT_LOG, message, null, message.getBytes("UTF-8"));
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
