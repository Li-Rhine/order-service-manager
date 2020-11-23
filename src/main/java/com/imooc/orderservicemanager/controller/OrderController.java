package com.imooc.orderservicemanager.controller;

import com.imooc.orderservicemanager.service.OrderService;
import com.imooc.orderservicemanager.vo.OrderCreateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Description：
 * @Author： Rhine
 * @Date： 2020/11/21 19:17
 **/
@Slf4j
@RestController
@RequestMapping("api/v1")
public class OrderController {

    @Autowired
    OrderService orderService;


    @PostMapping("/orders")
    public void createOrder(@RequestBody OrderCreateVO orderCreateVO) throws IOException, TimeoutException, InterruptedException {
        log.info("createOrder: orderCreateVO:{}", orderCreateVO);
        orderService.createOrder(orderCreateVO);
    }
}
