package top.chengpei.rabbitmq.delayed.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.chengpei.rabbitmq.delayed.config.RabbitConfig;

@Slf4j
@RestController
public class RabbitController {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping(value = "/rabbit/sendMessage")
    public String sendMessage(@RequestParam(value = "message", defaultValue = "这是一条测试消息") String message, @RequestParam(value = "delay", defaultValue = "10000") Long delay) {
        log.info("发送消息：{}", message);
        rabbitTemplate.convertAndSend(RabbitConfig.DELAY_EXCHANGE, RabbitConfig.DELAY_ROUTING_KEY, message, msg -> {
            msg.getMessageProperties().setDelayLong(delay);
            return msg;
        });
        return "success";
    }
}
