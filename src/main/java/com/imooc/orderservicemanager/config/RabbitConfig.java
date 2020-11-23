package com.imooc.orderservicemanager.config;

import com.imooc.orderservicemanager.service.OrderMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Bean
    public Queue queue2() {
        return new Queue("queue.restaurant");
    }
    @Bean
    public Binding binding6() {
        return new Binding("queue.restaurant",
                Binding.DestinationType.QUEUE,
                "exchange.order.restaurant",
                "key.restaurant",
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
        //发送端确认机制和消息返回机制
        //SIMPLE是异步确认 CORRELATED是同步确认，并且消息需要设置CORRELATED属性
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connectionFactory.setPublisherReturns(true);

        //使用一下connectionFactory，否则SpringBoot会懒加载connectionFactory，导致使用Rabbit之前，控制台找不到配置信息
        connectionFactory.createConnection();
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(@Autowired ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        //admin需要自动启动
        admin.setAutoStartup(true);
        return admin;
    }


    // @Configuration+@Autowired 启动时执行一次
//    @Autowired
    public void initRabbit() {


    }


    @Bean
    public RabbitTemplate rabbitTemplate(@Autowired ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //设置托管，开启发送端消息确认和消息返回
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                log.info("消息被打回+ message:{}", returned.getMessage());
            }
        });
        //发送端确认
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                //默认是异步确认
                log.info("消息发送已确认 correlationData:{}", correlationData);
            }
        });
        return rabbitTemplate;
    }
}
