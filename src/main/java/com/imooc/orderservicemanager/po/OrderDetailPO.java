package com.imooc.orderservicemanager.po;

import com.imooc.orderservicemanager.enummeration.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description：
 * @Author： Rhine
 * @Date： 2020/11/21 16:31
 **/
@Getter
@Setter
@ToString
public class OrderDetailPO {
    private Integer id;
    private OrderStatus status;
    private String address;
    private Integer accountId;
    private Integer productId;
    private Integer deliverymanId;
    private Integer settlementId;
    private Integer rewardId;
    private BigDecimal price;
    private Date date;

}
