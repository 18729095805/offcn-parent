package com.offcn.order.service;

import com.offcn.order.po.TOrder;
import com.offcn.order.vo.req.OrderInfoSubmitVo;

public interface OrderService {
    /**
     * 保存订单方法
     * @param vo
     * @return
     */

    public TOrder saveOrder(OrderInfoSubmitVo vo); //前端传vo
}

