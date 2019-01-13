package houzm.accumulation.rabbit.workqueues.batchAck;

import houzm.accumulation.rabbit.common.ServerInfo;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * author: hzm_dream@163.com
 * date: 2019/1/9 13:39
 * description:
 * 消息生产者
 */
public class Producer {
    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) {
        //连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(ServerInfo.SERVER_HOST);
        connectionFactory.setUsername(ServerInfo.SERVER_USERNAME);
        connectionFactory.setPassword(ServerInfo.SERVER_PASSWORD);

        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel();) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            for (int i = 1; i <= 10; i++) {
                StringBuilder message = new StringBuilder("the " + i + " message ");
                for (int j = 0; j < i; j++) {
                    message.append(".");
                }
                channel.basicPublish("", QUEUE_NAME, null, message.toString().getBytes());
                System.out.println("[x] Sent '" + message.toString() + "'");
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//            [x] Sent 'the 1 message .'
//            [x] Sent 'the 2 message ..'
//            [x] Sent 'the 3 message ...'
//            [x] Sent 'the 4 message ....'
//            [x] Sent 'the 5 message .....'
//            [x] Sent 'the 6 message ......'
//            [x] Sent 'the 7 message .......'
//            [x] Sent 'the 8 message ........'
//            [x] Sent 'the 9 message .........'
//            [x] Sent 'the 10 message ..........'
}
