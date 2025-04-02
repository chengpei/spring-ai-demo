package top.chengpei.rabbitmq.delayed.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.chengpei.rabbitmq.delayed.config.RabbitConfig;

@Slf4j
@Component
public class MessageListener {

    @RabbitListener(queues = RabbitConfig.DELAY_QUEUE)
    public void receiveMessage(String message) {
        log.info("接收到消息：{}", message);
    }
}
