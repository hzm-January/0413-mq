package houzm.accumulation.rabbit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

/**
 * author: hzm_dream@163.com
 * date: 2019/1/8 17:01
 * description: rabbit
 * <p>
 * 官方Api地址：https://www.rabbitmq.com/tutorials/tutorial-one-java.html
 * <p>
 * 异常：
 *      SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
 *      SLF4J: Defaulting to no-operation (NOP) logger implementation
 *      SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
 * 处理：
 *      如果在类路径上找不到SLF4J提供程序，则会报告此警告，即非错误消息。
 *      在类路径上放置一个（并且只有一个）slf4j-nop.jar slf4j-simple.jar，slf4j-log4j12.jar， slf4j-jdk14.jar或logback-classic.jar应该可以解决问题。
 *      请注意，这些提供程序必须以slf4j-api 1.8或更高版本为目标。
 */
public class ApiDemo {
    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception {
        // producer
//        producer();

        //创建链接工厂
//        consumer();
    }

    private static void consumer() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("47.101.152.55");
        connectionFactory.setUsername("houzm");
        connectionFactory.setPassword("houzm");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        DeliverCallback deliverCallback = new DeliverCallback() {
            @Override
            public void handle(String consumerTag, Delivery message) throws IOException {
                String messageRev = new String(message.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + messageRev + "'");
            }
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });
    }

    private static void producer() {
        //创建链接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("47.101.152.55");
        connectionFactory.setUsername("houzm");
        connectionFactory.setPassword("houzm");
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println("[x] Sent '" + message + "'");
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
