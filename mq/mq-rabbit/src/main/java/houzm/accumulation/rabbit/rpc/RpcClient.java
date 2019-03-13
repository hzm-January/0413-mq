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
    private static final String REPLY_QUEUE_NAME = "reply_queue_name";
    private static final String REQUEST_EXCHANGES_NAME = "req_exchanges_name";
    private static String replyQueueName;
    private Connection connection;
    private Channel channel;

    public RpcClient() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(ServerInfo.SERVER_HOST);
        connectionFactory.setUsername(ServerInfo.SERVER_USERNAME);
        connectionFactory.setPassword(ServerInfo.SERVER_PASSWORD);
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
//        channel.queueDeclare(REQUEST_QUEUE_NAME, true, false, false, null);
//        replyQueueName = channel.queueDeclare().getQueue();
        channel.queueDeclare(REPLY_QUEUE_NAME, true, false, false, null);
    }

    public static void main(String[] args) {
        try (RpcClient rpcClient = new RpcClient()) {
            for (int i = 0; i < 32; i++) {
                String reqMsg = Integer.valueOf(i).toString();
                System.out.println(" [x] Requesting fib(" + reqMsg + ")");
                String respMsg = rpcClient.call(reqMsg);
                System.out.println(" [.] Got '" + respMsg + "'");
            }
            System.out.println("------------------------ end =-------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String call(String reqMsg) throws IOException, InterruptedException {
        //1. correlationId
        String corrId = UUID.randomUUID().toString();
        //2. callback info
        AMQP.BasicProperties props = new AMQP.BasicProperties()
                .builder()
                .correlationId(corrId)
                .replyTo(REPLY_QUEUE_NAME)
                .build();
        //3. publish request to server
        channel.basicPublish("", REQUEST_QUEUE_NAME, props, reqMsg.getBytes("UTF-8"));
        //4. blocking queue
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);
        String ctag = channel.basicConsume(REPLY_QUEUE_NAME, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equalsIgnoreCase(corrId)) {
                blockingQueue.offer(new String(delivery.getBody(), "UTF-8"));
            }
//            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
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
