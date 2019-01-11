package houzm.accumulation.rabbit.common;

import com.rabbitmq.client.ConnectionFactory;

/**
 * author: hzm_dream@163.com
 * date: 2019/1/11 17:12
 * description: 工厂的单例模式
 */
public class ConnectionFactorySingle {

    private static volatile ConnectionFactory connectionFactory = null;

    private ConnectionFactorySingle() {
        if (connectionFactory != null) {
            throw new IllegalStateException(" the instance was initialed ...");
        }
    }

    public static ConnectionFactory connectionFactory() {
        ConnectionFactory result = connectionFactory;
        if (result == null) {
            synchronized (ConnectionFactorySingle.class) {
                if (result == null) {
                    connectionFactory = new ConnectionFactory();
                    connectionFactory.setHost("47.101.152.55");
                    connectionFactory.setUsername("houzm");
                    connectionFactory.setPassword("houzm");
                    result = connectionFactory;
                }
            }
        }
        return result;
    }

}
