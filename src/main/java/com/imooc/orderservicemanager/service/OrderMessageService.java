package com.imooc.orderservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.orderservicemanager.dao.OrderDetailDao;
import com.imooc.orderservicemanager.dto.OrderMessageDTO;
import com.imooc.orderservicemanager.enummeration.OrderStatus;
import com.imooc.orderservicemanager.po.OrderDetailPO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
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
@RabbitListener(
        containerFactory = "rabbitListenerContainerFactory",
        queues = "queue.order"
)
public class OrderMessageService {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OrderDetailDao orderDetailDao;


    //必须要叫handleMessage方法
    @RabbitHandler(isDefault = true)
    public void handleMessage(@Payload Message message){
        log.info("deliverCallback:messageBody:{}", new String(message.getBody()));
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("101.132.104.74");

        try {
            //将消息体反序列化成DTO
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(message.getBody(), OrderMessageDTO.class);
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
    };

}
