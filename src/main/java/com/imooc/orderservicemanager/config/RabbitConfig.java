package com.imooc.orderservicemanager.config;

import com.imooc.orderservicemanager.dto.OrderMessageDTO;
import com.imooc.orderservicemanager.service.OrderMessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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


    /***************** restaurant ******************/
//    @Bean
//    public Exchange exchange1(){
//        return new DirectExchange("exchange.order.restaurant");
//    }
//
//    @Bean
//    public Queue queue1() {
//        return new Queue("queue.order");
//    }
//
//    @Bean
//    public Binding binding1() {
//        return new Binding("queue.order",
//                Binding.DestinationType.QUEUE,
//                "exchange.order.restaurant",
//                "key.order",
//                null);
//    }
//
//    @Bean
//    public Queue queue2() {
//        return new Queue("queue.restaurant");
//    }
//    @Bean
//    public Binding binding6() {
//        return new Binding("queue.restaurant",
//                Binding.DestinationType.QUEUE,
//                "exchange.order.restaurant",
//                "key.restaurant",
//                null);
//    }
//
//    /******************** deliveryman ***************/
//    @Bean
//    public Exchange exchange2(){
//        return new DirectExchange("exchange.order.deliveryman");
//    }
//    @Bean
//    public Binding binding2() {
//        return new Binding( "queue.order",
//                Binding.DestinationType.QUEUE,
//                "exchange.order.deliveryman",
//                "key.order",
//                null);
//    }
//
//    /************** settlement *****************/
//    @Bean
//    public Exchange exchange3(){
//        return new FanoutExchange("exchange.order.settlement");
//    }
//    @Bean
//    public Exchange exchange4(){
//        return new FanoutExchange("exchange.settlement.order");
//    }
//    @Bean
//    public Binding binding3() {
//        return new Binding( "queue.order",
//                Binding.DestinationType.QUEUE,
//                "exchange.settlement.order",
//                "key.order",
//                null);
//    }
//
//    /******************* reward ********************/
//    @Bean
//    public Exchange exchange5(){
//        return new DirectExchange("exchange.order.reward");
//    }
//    @Bean
//    public Binding binding4() {
//        return new Binding(  "queue.order",
//                Binding.DestinationType.QUEUE,
//                "exchange.order.reward",
//                "key.order",
//                null);
//    }













/************************** 使用SpringBoot的配置代替下面的Bean ******************************/

//    @Bean
//    public ConnectionFactory connectionFactory() {
//        //老的ConnectionFactory的包装增强类
//        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
//        connectionFactory.setHost("101.132.104.74");
//        connectionFactory.setPort(5672);
//        connectionFactory.setUsername("guest");
//        connectionFactory.setPassword("guest");
//        //发送端确认机制和消息返回机制
//        //SIMPLE是异步确认 CORRELATED是同步确认，并且消息需要设置CORRELATED属性
//        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
//        connectionFactory.setPublisherReturns(true);
//
//        //使用一下connectionFactory，否则SpringBoot会懒加载connectionFactory，导致使用Rabbit之前，控制台找不到配置信息
//        connectionFactory.createConnection();
//        return connectionFactory;
//    }

//    @Bean
//    public RabbitAdmin rabbitAdmin(@Autowired ConnectionFactory connectionFactory) {
//        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
//        //admin需要自动启动
//        admin.setAutoStartup(true);
//        return admin;
//    }


    // @Configuration+@Autowired 启动时执行一次
////    @Autowired
//    public void initRabbit() {
//
//
//    }
//

//    @Bean
//    public RabbitTemplate rabbitTemplate(@Autowired ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        //设置托管，开启发送端消息确认和消息返回
//        rabbitTemplate.setMandatory(true);
//        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
//            @Override
//            public void returnedMessage(ReturnedMessage returned) {
//                log.info("消息被打回+ message:{}", returned.getMessage());
//            }
//        });
//        //发送端确认
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            @Override
//            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                //默认是异步确认
//                log.info("消息发送已确认 correlationData:{}", correlationData);
//            }
//        });
//        return rabbitTemplate;
//    }

//    //消费端高级特性
//    @Bean
//    public SimpleMessageListenerContainer messageListenerContainer(@Autowired ConnectionFactory connectionFactory) {
//        SimpleMessageListenerContainer messageListenerContainer =
//                new SimpleMessageListenerContainer(connectionFactory);
//        //监听"queue.order"队列
//        messageListenerContainer.setQueueNames("queue.order");
//        messageListenerContainer.setConcurrentConsumers(3);
//        messageListenerContainer.setMaxConcurrentConsumers(5);
//        //确认方式
////        messageListenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
////        messageListenerContainer.setMessageListener(new MessageListener() {
////            @Override
////            public void onMessage(Message message) {
////                log.info("message:{}", message);
////            }
////        });
//
//        //手动确认
//        messageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
////        messageListenerContainer.setMessageListener(new ChannelAwareMessageListener() {
////            @Override
////            public void onMessage(Message message, Channel channel) throws Exception {
////                log.info("处理message:{}", message);
////                orderMessageService.handleMessage(message.getBody());
////                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
////            }
////        });
//
//        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(orderMessageService);
//
//        //消息转化器
//        Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter();
//        //1
//        messageConverter.setClassMapper(new ClassMapper() {
//            @Override
//            public void fromClass(Class<?> clazz, MessageProperties properties) {
//
//            }
//
//            @Override
//            public Class<?> toClass(MessageProperties properties) {
//                return OrderMessageDTO.class;
//            }
//        });
//        messageListenerAdapter.setMessageConverter(messageConverter);
//
//
//        Map<String, String> methodMap = new HashMap<>(8);
//        //messageListenerAdapter高级特性，指定队列和方法直接绑定
//        methodMap.put("queue.order", "handleMessage");
//        messageListenerAdapter.setQueueOrTagToMethodName(methodMap);
//
//        messageListenerContainer.setMessageListener(messageListenerAdapter);
//
//
////        //消费端限流
//        messageListenerContainer.setPrefetchCount(1);
//        return messageListenerContainer;
//    }


//    @Bean
//    public RabbitListenerContainerFactory rabbitListenerContainerFactory(@Autowired ConnectionFactory connectionFactory) {
//
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        return factory;
//
//    }
}
