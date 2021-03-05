package com.offcn.order.service.impl;

import com.offcn.dycommon.enums.OrderStatusEnumes;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.order.mapper.TOrderMapper;
import com.offcn.order.po.TOrder;
import com.offcn.order.po.TReturn;
import com.offcn.order.service.OrderService;
import com.offcn.order.service.ProjectServiceFeign;
import com.offcn.order.vo.req.OrderInfoSubmitVo;
import com.offcn.utils.AppDateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProjectServiceFeign projectServiceFeign;
    @Autowired
    private TOrderMapper tOrderMapper;


    @Override
    public TOrder saveOrder(OrderInfoSubmitVo vo) {
        //1.创建订单对象
        TOrder order = new TOrder();
        //通过vo里的用户令牌获取用户id  memberId
        String memberId = stringRedisTemplate.opsForValue().get(vo.getAccessToken());
        order.setMemberid(Integer.parseInt(memberId)); //转为int类型 设置订单的用户id
        //拷贝vo里的属性到order  类型一样才能拷贝
        BeanUtils.copyProperties(vo,order);  //前面的是被拷贝的
        //设置订单号
        String orderNum = UUID.randomUUID().toString().replace("-", ""); //生成唯一订单号
        order.setOrdernum(orderNum);
        //设置订单创建时间
        order.setCreatedate(AppDateUtils.getFormatTime());//使用工具类
        //设置订单支付状态
        order.setStatus(OrderStatusEnumes.UNPAY.getCode()+""); //加""转换为String类型
        //设置订单发票状态  //类型不一样不能BeanUtils拷贝
        order.setInvoice(vo.getInvoice().toString());
        //此处是为了练习远程调用.实际上可以不用远程调用
        //远程调用
        //获得回赠列表
        AppResponse<List<TReturn>> response = projectServiceFeign.getReturnList(vo.getProjectid());
        List<TReturn> returnsList = response.getData();
        TReturn myReturn=null;
        for (TReturn tReturn : returnsList) {
            if(tReturn.getId().intValue() == vo.getReturnid().intValue()){
                myReturn=tReturn;
                break;
            }
        }
        //设置订单钱  回报数量*支持金额+运费
        Integer money=order.getRtncount()*myReturn.getSupportmoney()+myReturn.getFreight(); //计算
        order.setMoney(money); //保存到订单

        tOrderMapper.insertSelective(order);


        return order;
    }
}
