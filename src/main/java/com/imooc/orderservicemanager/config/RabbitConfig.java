package com.imooc.orderservicemanager.config;

import com.imooc.orderservicemanager.service.OrderMessageService;
import com.rabbitmq.client.BuiltinExchangeType;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Description：
 * @Author： Rhine
 * @Date： 2020/11/22 1:08
 **/
@Configuration
public class RabbitConfig {

    @Autowired
    OrderMessageService orderMessageService;

    @Autowired
    public void startListenMessage() throws InterruptedException, TimeoutException, IOException {
        orderMessageService.handleMessage();
    }


    // @Configuration+@Autowired 启动时执行一次
    @Autowired
    public void initRabbit() {
        //老的ConnectionFactory的包装增强类
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("101.132.104.74");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");

        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);


        /***************** restaurant ******************/
        Exchange exchange = new DirectExchange("exchange.order.restaurant");
        rabbitAdmin.declareExchange(exchange);

        Queue queue = new Queue("queue.order");
        rabbitAdmin.declareQueue(queue);

        Binding binding = new Binding(
                "queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.order.restaurant",
                "key.order",
                null);
        rabbitAdmin.declareBinding(binding);


        /******************** deliveryman ***************/
        exchange = new DirectExchange("exchange.order.deliveryman");
        rabbitAdmin.declareExchange(exchange);

        queue = new Queue("queue.deliveryman");
        rabbitAdmin.declareQueue(queue);

        binding = new Binding(
                "queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.order.deliveryman",
                "key.order",
                null);
        rabbitAdmin.declareBinding(binding);


        /************** settlement *****************/
        exchange = new FanoutExchange("exchange.order.settlement");
        rabbitAdmin.declareExchange(exchange);

        exchange = new FanoutExchange("exchange.settlement.order");
        rabbitAdmin.declareExchange(exchange);

        binding = new Binding(
                "queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.settlement.order",
                "key.order",
                null);
        rabbitAdmin.declareBinding(binding);


        /******************* reward ********************/
        exchange = new TopicExchange("exchange.order.reward");
        rabbitAdmin.declareExchange(exchange);

        queue = new Queue("queue.reward");
        rabbitAdmin.declareQueue(queue);

        binding = new Binding(
                "queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.order.reward",
                "key.order",
                null);
        rabbitAdmin.declareBinding(binding);


    }
}
