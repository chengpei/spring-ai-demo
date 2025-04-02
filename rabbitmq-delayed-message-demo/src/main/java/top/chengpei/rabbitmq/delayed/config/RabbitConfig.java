package top.chengpei.rabbitmq.delayed.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // 延迟交换机名称
    public static final String DELAY_EXCHANGE = "delay.exchange";
    // 延迟队列名称
    public static final String DELAY_QUEUE = "delay.queue";
    // 路由键
    public static final String DELAY_ROUTING_KEY = "delay.routing.key";

    /**
     * 创建延迟交换机（自定义交换机类型为 x-delayed-message）
     */
//    @Bean
//    public CustomExchange delayExchange() {
//        // 参数说明：
//        // 1. 交换机名称
//        // 2. 交换机类型
//        // 3. 是否持久化
//        // 4. 是否自动删除
//        // 5. 其他参数
//        return new CustomExchange(
//                DELAY_EXCHANGE,
//                "x-delayed-message",
//                true,
//                false,
//                Map.of("x-delayed-type", "direct")
//        );
//    }

    @Bean
    public Exchange delayExchange() {
        return ExchangeBuilder.directExchange(DELAY_EXCHANGE)
                .delayed() // 实测设置delayed也可以创建延迟交换机替代以上写法
                .durable(true)
                .build();
    }

    /**
     * 创建延迟队列
     */
    @Bean
    public Queue delayQueue() {
        return new Queue(DELAY_QUEUE, true);
    }

    /**
     * 绑定延迟队列到延迟交换机
     */
    @Bean
    public Binding delayBinding(Queue delayQueue, Exchange delayExchange) {
        return BindingBuilder.bind(delayQueue)
                .to(delayExchange)
                .with(DELAY_ROUTING_KEY)
                .noargs();
    }
}
