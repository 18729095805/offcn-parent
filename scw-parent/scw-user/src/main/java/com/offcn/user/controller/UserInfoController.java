package com.offcn.user.controller;

import com.offcn.dycommon.response.AppResponse;
import com.offcn.user.po.TMemberAddress;
import com.offcn.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags="获取会员信息/更新个人信息/获取用户收货地址")
@RestController
@RequestMapping("/user")
public class UserInfoController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserService userService;


    @ApiOperation(value = "获取用户地址")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "用户令牌" ,name ="accessToken", required = true)
    })
    @GetMapping("/findAddressList")
    public AppResponse findAddressList(String accessToken){
        String memberIdString = stringRedisTemplate.opsForValue().get(accessToken);
        if(StringUtils.isEmpty(memberIdString)){
            return AppResponse.fail("请登录");
        }
        List<TMemberAddress> addressList = userService.findAddressList(Integer.parseInt(memberIdString));
        return AppResponse.ok(addressList);

    }


}
