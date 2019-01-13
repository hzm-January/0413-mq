package houzm.accumulation.rabbit.pulishsubscribe.fanout;

import houzm.accumulation.rabbit.common.ServerInfo;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * author: hzm_dream@163.com
 * date: 2019/1/11 17:11
 * description: 消息生产者
 */
public class Producer {

    public static final String EXCHANGES_NAME = "logs";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(ServerInfo.SERVER_HOST);
        connectionFactory.setUsername(ServerInfo.SERVER_USERNAME);
        connectionFactory.setPassword(ServerInfo.SERVER_PASSWORD);
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel();) {
            //声明exchanges
            channel.exchangeDeclare(EXCHANGES_NAME, "fanout");
            for (int i = 1; i <= 10; i++) {
                StringBuilder message = new StringBuilder("the " + i + " message ");
                for (int j = 0; j < i; j++) {
                    message.append(".");
                }
                channel.basicPublish(EXCHANGES_NAME, "", null, message.toString().getBytes());
                System.out.println("[x] Sent '" + message.toString() + "'");
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
