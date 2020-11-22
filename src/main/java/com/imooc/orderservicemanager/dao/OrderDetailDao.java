package com.imooc.orderservicemanager.dao;

import com.imooc.orderservicemanager.po.OrderDetailPO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @Description：
 * @Author： Rhine
 * @Date： 2020/11/21 16:38
 **/
@Mapper
@Repository
public interface OrderDetailDao {

    @Insert("INSERT INTO order_detail (status, address, account_id, product_id, deliveryman_id, settlement_id, reward_id, price, date)" +
            "VALUES (#{status}, #{address}, #{accountId}, #{productId}, #{deliverymanId}, #{settlementId}, #{rewardId}, #{price}, #{date})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(OrderDetailPO orderDetailPO);

    @Update("UPDATE order_detail SET status = #{status}, address = #{address}, account_id = #{accountId}, " +
            "product_id = #{productId}, deliveryman_id = #{deliverymanId}, settlement_id = #{settlementId}, reward_id = #{rewardId}, price = #{price}, date = #{date} WHERE id = #{id}")
    void update(OrderDetailPO orderDetailPO);

    @Select("SELECT status, address, account_id accountId, product_id productId, deliveryman_id deliverymanId, settlement_id settlementId, reward_id rewardId, price, date " +
            "FROM order_detail WHERE id = #{id}")
    OrderDetailPO selectOrder(Integer id);

}
