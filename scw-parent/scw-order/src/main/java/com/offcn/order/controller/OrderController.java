package com.offcn.order.controller;

import com.offcn.dycommon.response.AppResponse;
import com.offcn.order.config.AppSwaggerConfig;
import com.offcn.order.po.TOrder;
import com.offcn.order.service.OrderService;
import com.offcn.order.vo.req.OrderInfoSubmitVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags ="保存订单模块")
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private OrderService orderService;

    @ApiOperation("保存订单")
    @PostMapping("/carateOrder")
    public AppResponse<Object> createOrder(@RequestBody OrderInfoSubmitVo vo){
        String memberId = stringRedisTemplate.opsForValue().get(vo.getAccessToken());
        //验证是否登录
        if(memberId == null){
            return AppResponse.fail("请登录");
        }
        try {
            TOrder tOrder = orderService.saveOrder(vo);
            return AppResponse.ok(tOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return AppResponse.fail(null);
        }


    }
}
