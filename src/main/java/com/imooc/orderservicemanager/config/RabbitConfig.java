package com.imooc.orderservicemanager.config;

import com.imooc.orderservicemanager.service.OrderMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Description：
 * @Author： Rhine
 * @Date： 2020/11/22 1:08
 **/
@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    OrderMessageService orderMessageService;

    @Autowired
    public void startListenMessage() throws InterruptedException, TimeoutException, IOException {
//        orderMessageService.handleMessage();
    }

    /***************** restaurant ******************/
    @Bean
    public Exchange exchange1(){
        return new DirectExchange("exchange.order.restaurant");
    }

    @Bean
    public Queue queue1() {
        return new Queue("queue.order");
    }

    @Bean
    public Binding binding1() {
        return new Binding("queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.order.restaurant",
                "key.order",
                null);
    }

    /******************** deliveryman ***************/
    @Bean
    public Exchange exchange2(){
        return new DirectExchange("exchange.order.deliveryman");
    }
    @Bean
    public Binding binding2() {
        return new Binding( "queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.order.deliveryman",
                "key.order",
                null);
    }

    /************** settlement *****************/
    @Bean
    public Exchange exchange3(){
        return new FanoutExchange("exchange.order.settlement");
    }
    @Bean
    public Exchange exchange4(){
        return new FanoutExchange("exchange.settlement.order");
    }
    @Bean
    public Binding binding3() {
        return new Binding( "queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.settlement.order",
                "key.order",
                null);
    }

    /******************* reward ********************/
    @Bean
    public Exchange exchange5(){
        return new DirectExchange("exchange.order.reward");
    }
    @Bean
    public Binding binding4() {
        return new Binding(  "queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.order.reward",
                "key.order",
                null);
    }


    @Bean
    public ConnectionFactory connectionFactory() {
        //老的ConnectionFactory的包装增强类
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("101.132.104.74");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //使用一下connectionFactory，否则SpringBoot会懒加载connectionFactory，导致使用Rabbit之前，控制台找不到配置信息
        connectionFactory.createConnection();
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(@Autowired ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }


    // @Configuration+@Autowired 启动时执行一次
//    @Autowired
    public void initRabbit() {


    }
}
