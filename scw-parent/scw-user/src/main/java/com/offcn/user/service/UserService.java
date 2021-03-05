package com.offcn.user.service;

import com.offcn.user.po.TMember;
import com.offcn.user.po.TMemberAddress;

import java.util.List;

public interface UserService {
    public void registerUser(TMember member);
    public TMember login(String name,String password);
    //根据用户id查询用户信息
    public TMember findMemberById(Integer id);
    List<TMemberAddress> findAddressList(Integer memberId);
}
