package com.imooc.orderservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.orderservicemanager.dao.OrderDetailDao;
import com.imooc.orderservicemanager.dto.OrderMessageDTO;
import com.imooc.orderservicemanager.enummeration.OrderStatus;
import com.imooc.orderservicemanager.po.OrderDetailPO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Description：
 * @Author： Rhine
 * @Date： 2020/11/21 16:55
 **/

/**
 * 消息处理相关业务逻辑
 */
@Slf4j
@Service
public class OrderMessageService {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OrderDetailDao orderDetailDao;

    /**
     * 声明消息队列、交换机、绑定、消息的处理
     *  @Async起一个异步线程（有异步线程一定要有线程池）
     */
    @Async
    public void handleMessage() throws IOException, TimeoutException, InterruptedException {
        Thread.sleep(5000);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("101.132.104.74");

        // connection用完一定要关闭连接，否则会大量的消耗资源
        //用try(){}写法相当于在finally里面手动的加上connection.close()方法了
        try(Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel()) {



            // 声明exchange、queue后，绑定两者，然后设置监听队列
//            channel.basicConsume("queue.order", true, deliverCallback, consumerTag -> {});
            // 异步线程一结束就停止监听队列，所以需要让线程一直sleep下去，保持存活
            while (true) {
                Thread.sleep(10000000);
            }

        }

    }

    DeliverCallback deliverCallback = ((consumerTag, message) -> {
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("101.132.104.74");

        try {
            //将消息体反序列化成DTO
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody, OrderMessageDTO.class);
            //数据库中读取订单PO
            OrderDetailPO orderPO = orderDetailDao.selectOrder(orderMessageDTO.getOrderId());
            //consumerTag可以辨别是哪个服务发来的消息
            switch (orderPO.getStatus()) {

                case ORDER_CREATING:
                    if (orderMessageDTO.getConfirmed() && null != orderMessageDTO.getPrice()) {
                        orderPO.setStatus(OrderStatus.RESTAURANT_CONFIRMED);
                        orderPO.setPrice(orderMessageDTO.getPrice());
                        orderDetailDao.update(orderPO);
                        try(Connection connection = connectionFactory.newConnection();
                            Channel channel = connection.createChannel()) {
                            //确认标志
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish(
                                    "exchange.order.deliveryman",
                                    "key.deliveryman",
                                    null,
                                    messageToSend.getBytes());
                        }
                    }else {
                        orderPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderPO);
                    }
                    break;
                case RESTAURANT_CONFIRMED:
                    if (null != orderMessageDTO.getDeliverymanId()) {
                        orderPO.setStatus(OrderStatus.DELIVERMAN_CONFIRMED);
                        orderPO.setDeliverymanId(orderMessageDTO.getDeliverymanId());
                        orderDetailDao.update(orderPO);
                        try(Connection connection = connectionFactory.newConnection();
                            Channel channel = connection.createChannel();
                        ){
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish(
                                    "exchange.order.settlement",
                                    "key.settlement",
                                    null,
                                    messageToSend.getBytes()
                            );
                        }
                    }else {
                        orderPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderPO);
                    }
                    break;
                case DELIVERMAN_CONFIRMED:
                    if (null != orderMessageDTO.getSettlementId()) {
                        orderPO.setStatus(OrderStatus.SETTLEMENT_CONFIRMED);
                        orderPO.setSettlementId(orderMessageDTO.getSettlementId());
                        orderDetailDao.update(orderPO);
                        try(Connection connection = connectionFactory.newConnection();
                            Channel channel = connection.createChannel();
                        ){
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish(
                                    "exchange.order.reward",
                                    "key.reward",
                                    null,
                                    messageToSend.getBytes()
                            );
                        }




                    }else {
                        orderPO.setStatus(OrderStatus.FAILED);
                    }


                    break;
                case SETTLEMENT_CONFIRMED:
                    if (null != orderMessageDTO.getRewardId()) {
                        orderPO.setStatus(OrderStatus.ORDER_CREATED);
                        orderPO.setRewardId(orderMessageDTO.getRewardId());
                        orderDetailDao.update(orderPO);
                    }else {
                        orderPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderPO);
                    }
                    break;
            }



        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    });

}
