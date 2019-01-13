package houzm.accumulation.rabbit.pulishsubscribe.direct;

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
    public static final String EXCHANGES_DIRECT_LOG = "direct_logs";
    private static String[] routingkeys = new String[]{"info", "error", "debug", "warning"};

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(ServerInfo.SERVER_HOST);
        connectionFactory.setUsername(ServerInfo.SERVER_USERNAME);
        connectionFactory.setPassword(ServerInfo.SERVER_PASSWORD);
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGES_DIRECT_LOG, "direct");
            for (int i = 1; i <= 10; i++) {
                int index = new Random().nextInt(4);
                String routingkey = routingkeys[index];
                String message = routingkey.concat(" : the message  index*i*16+33 : " + (index * i << 4 + 33));
                channel.basicPublish(EXCHANGES_DIRECT_LOG, routingkey, null, message.getBytes("UTF-8"));
                System.out.println("[x] Sent '" + message + "'");
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
