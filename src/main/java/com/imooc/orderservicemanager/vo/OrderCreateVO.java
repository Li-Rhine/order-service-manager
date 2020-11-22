package com.imooc.orderservicemanager.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description：
 * @Author： Rhine
 * @Date： 2020/11/21 15:27
 **/
@Getter
@Setter
@ToString
public class OrderCreateVO {
    /**
     * 用户ID
     */
    private Integer accountId;
    /**
     * 地址
     */
    private String address;
    /**
     * 产品ID
     */
    private Integer productId;
}
