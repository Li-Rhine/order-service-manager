package com.imooc.orderservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.orderservicemanager.dao.OrderDetailDao;
import com.imooc.orderservicemanager.dto.OrderMessageDTO;
import com.imooc.orderservicemanager.enummeration.OrderStatus;
import com.imooc.orderservicemanager.po.OrderDetailPO;
import com.imooc.orderservicemanager.vo.OrderCreateVO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * 处理用户关于订单的业务请求
 **/
@Slf4j
@Service
public class OrderService {

    @Autowired
    OrderDetailDao orderDetailDao;

    @Autowired
    ObjectMapper objectMapper;

    public void createOrder(OrderCreateVO orderCreateVO) throws IOException, TimeoutException, InterruptedException {
        OrderDetailPO orderDetailPO = new OrderDetailPO();
        orderDetailPO.setAddress(orderCreateVO.getAddress());
        orderDetailPO.setAccountId(orderCreateVO.getAccountId());
        orderDetailPO.setProductId(orderCreateVO.getProductId());
        orderDetailPO.setStatus(OrderStatus.ORDER_CREATING);
        orderDetailPO.setDate(new Date());
        orderDetailDao.insert(orderDetailPO);

        OrderMessageDTO orderMessageDTO = new OrderMessageDTO();
        orderMessageDTO.setOrderId(orderDetailPO.getId());
        orderMessageDTO.setProductId(orderDetailPO.getProductId());
        orderMessageDTO.setAccountId(orderDetailPO.getAccountId());

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("101.132.104.74");

        try(/******** 创建连接，使用之后并关闭channel **********/
            Connection connnection = connectionFactory.newConnection();
            Channel channel = connnection.createChannel();) {

            /************ RabbitMQ传递的是字符，所以需要转换为json **************/
            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
            channel.confirmSelect();

            ConfirmListener confirmListener = new ConfirmListener() {
                @Override
                //deliveryTag发送端的消息序号 multiple true确认单条消息 false确认单条消息
                public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                    log.info("Ack, deliveryTag:{}, multiple:{}", deliveryTag, multiple);
                }

                @Override
                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                    log.info("Nack, deliveryTag:{}, multiple:{}", deliveryTag, multiple);
                }
            };
            channel.addConfirmListener(confirmListener);

            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().expiration("15000").build();
            channel.basicPublish(
                    "exchange.order.restaurant",
                    "key.restaurant",
                    null,
                    messageToSend.getBytes()
            );
            log.info("message sent");

            //线程不能结束，所以需要sleep。一结束就收不到确认消息
            Thread.sleep(1000000);

        }
    }

}
