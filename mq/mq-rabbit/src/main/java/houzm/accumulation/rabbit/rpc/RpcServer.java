package houzm.accumulation.rabbit.rpc;

import houzm.accumulation.rabbit.common.ServerInfo;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Author: hzm_dream@163.com
 * Date:  2019/1/13 22:10
 * Modified By:
 * Description：
 */
public class RpcServer {
    private static final String REQUEST_QUEUE_NAME = "req_queue_name";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(ServerInfo.SERVER_HOST);
        connectionFactory.setUsername(ServerInfo.SERVER_USERNAME);
        connectionFactory.setPassword(ServerInfo.SERVER_PASSWORD);
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(REQUEST_QUEUE_NAME, true, false, false, null);
//        channel.queuePurge(REQUEST_QUEUE_NAME);
        channel.basicQos(1);
        channel.basicConsume(REQUEST_QUEUE_NAME, false, (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            int n = Integer.parseInt(message);
            System.out.println(" [.] fib(" + message + ")");
            String respMsg = "" + fib(n);

            // reply
            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();
            channel.basicPublish("", delivery.getProperties().getReplyTo(), props, respMsg.getBytes("UTF-8"));
            // consumer ack
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            }, consumerTag -> {
        });
    }

    private static int fib(int n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        return fib(n - 1) + fib(n - 2);
    }
}
