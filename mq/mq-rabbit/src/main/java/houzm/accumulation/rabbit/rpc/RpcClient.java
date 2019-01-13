package houzm.accumulation.rabbit.rpc;

import houzm.accumulation.rabbit.common.ServerInfo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import static jdk.nashorn.internal.objects.NativeFunction.call;

/**
 * Author: hzm_dream@163.com
 * Date:  2019/1/12 11:22
 * Modified By:
 * Description： producer
 */
public class RpcClient implements AutoCloseable {
    private static final String REQUEST_QUEUE_NAME = "req_queue_name";
    private static String replyQueueName;
    private static ConnectionFactory connectionFactory;
    private static Connection connection;
    private static Channel channel;

    static {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(ServerInfo.SERVER_HOST);
        connectionFactory.setUsername(ServerInfo.SERVER_USERNAME);
        connectionFactory.setPassword(ServerInfo.SERVER_PASSWORD);
        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            replyQueueName = channel.queueDeclare().getQueue();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        try (RpcClient rpcClient = new RpcClient()) {
            IntStream.rangeClosed(0, 31).forEach(key->{
                String reqMsg = Integer.valueOf(key).toString();
                System.out.println(" [x] Requesting fib(" + reqMsg + ")");
                String respMsg = null;
                try {
                    respMsg = rpcClient.call(reqMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(" [.] Got '" + respMsg + "'");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String call(String reqMsg) throws IOException, InterruptedException {
        //1. correlationId
        String corrId = UUID.randomUUID().toString();
        //2. callback info
        AMQP.BasicProperties props = new AMQP.BasicProperties()
                .builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();
        //3. publish request to server
        channel.basicPublish("", REQUEST_QUEUE_NAME, props, reqMsg.getBytes("UTF-8"));
        //4. blocking queue
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);
        String ctag = channel.basicConsume(replyQueueName, false, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equalsIgnoreCase(corrId)) {
                blockingQueue.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });
        String respMsg = blockingQueue.take();
        channel.basicCancel(ctag);
        return respMsg;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
