package houzm.accumulation.rabbit.queue;

import houzm.accumulation.rabbit.common.ServerInfo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Author: hzm_dream@163.com
 * Date:  2019/1/13 22:28
 * Modified By:
 * Description：
 */
public class QueueDeclareConsumer {
    public static void main(String[] args) throws IOException {
        Connection connection = ServerInfo.connection();
        Channel channel = connection.createChannel();
        System.out.println(channel);
        channel.queueDeclare("test_queue_declare", false, false, false, null);
//        TimeUnit.MILLISECONDS.sleep(500);
        channel.basicConsume("test_queue_declare", true, (consumerTag, delivery) -> {
            String msg = new String(delivery.getBody(), "UTF-8");
            System.out.println(" res : msg - " + msg);
        }, consumerTag -> {
        });

    }
}
