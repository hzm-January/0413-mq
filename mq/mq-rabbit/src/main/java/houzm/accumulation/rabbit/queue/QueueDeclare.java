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
public class QueueDeclare {
    public static void main(String[] args) throws IOException {
        try (Connection connection = ServerInfo.connection();
             Channel channel = connection.createChannel()) {
            IntStream.rangeClosed(0, 100).forEach(key -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                    System.out.println(" send : msg - " + key);
                    channel.basicPublish("", "test_queue_declare", null, "".concat(key+"").getBytes("UTF-8"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }
}
