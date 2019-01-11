package houzm.accumulation.rabbit.common;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.Connection;

/**
 * author: hzm_dream@163.com
 * date: 2019/1/11 17:12
 * description: 创建连接
 */
public class ConnectionUtil {

    public static Connection connection() {
        Connection result = null;
        try {
            result = ConnectionFactorySingle.connectionFactory().newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return result;
    }

}
