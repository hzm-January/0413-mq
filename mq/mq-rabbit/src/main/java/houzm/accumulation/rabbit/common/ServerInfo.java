package houzm.accumulation.rabbit.common;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Author: hzm_dream@163.com
 * Date:  2019/1/12 12:05
 * Modified By:
 * Description：
 */
public class ServerInfo implements AutoCloseable {

    public static final String SERVER_HOST = "47.101.152.55";
    public static final String SERVER_USERNAME = "houzm";
    public static final String SERVER_PASSWORD = "houzm";
    public static volatile Connection connection = null;

    public static Connection connectionSingle() {
        Connection result = connection;
        if (result == null) {
            synchronized (ServerInfo.class) {
                result = connection;
                if (result == null) {
                    try {
                        result = connection = connectionFactory().newConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    public static Connection connection() {
        Connection result = null;
        try {
            result = connectionFactory().newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ConnectionFactory connectionFactory() {
        ConnectionFactory connectionFactory = SingleConnectionFactory.CONNECTION_FACTORY;
        connectionFactory.setHost(SERVER_HOST);
        connectionFactory.setUsername(SERVER_USERNAME);
        connectionFactory.setPassword(SERVER_PASSWORD);
        return connectionFactory;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    public static class SingleConnectionFactory {
        private static final ConnectionFactory CONNECTION_FACTORY = new ConnectionFactory();
    }
}
