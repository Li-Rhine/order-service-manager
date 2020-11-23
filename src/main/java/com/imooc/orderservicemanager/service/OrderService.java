package com.imooc.orderservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.orderservicemanager.dao.OrderDetailDao;
import com.imooc.orderservicemanager.dto.OrderMessageDTO;
import com.imooc.orderservicemanager.enummeration.OrderStatus;
import com.imooc.orderservicemanager.po.OrderDetailPO;
import com.imooc.orderservicemanager.vo.OrderCreateVO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
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
            channel.basicPublish(
                    "exchange.order.restaurant",
                    "key.restaurant",
                    null,
                    messageToSend.getBytes()
                    );
            log.info("message sent");
            if ( channel.waitForConfirms() ) {
                log.info("RabbitMQ confirm success");
            } else {
                log.info("RabbitMQ confirm failed");
            }

        }
    }

}
