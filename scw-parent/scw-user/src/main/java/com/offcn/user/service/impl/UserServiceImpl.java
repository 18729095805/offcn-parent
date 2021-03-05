package com.offcn.user.service.impl;

import com.offcn.user.enums.UserExceptionEnum;
import com.offcn.user.exception.UserException;
import com.offcn.user.mapper.TMemberAddressMapper;
import com.offcn.user.mapper.TMemberMapper;
import com.offcn.user.po.TMember;
import com.offcn.user.po.TMemberAddress;
import com.offcn.user.po.TMemberAddressExample;
import com.offcn.user.po.TMemberExample;
import com.offcn.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private TMemberMapper memberMapper;
    @Autowired
    private TMemberAddressMapper tMemberAddressMapper;

    @Override
    public void registerUser(TMember member) {
        // 1、检查系统中此手机号是否存在
        TMemberExample example = new TMemberExample();
        example.createCriteria().andLoginacctEqualTo(member.getLoginacct());
        long l = memberMapper.countByExample(example); //查询多少条
        if (l > 0) {
            throw new UserException(UserExceptionEnum.LOGINACCT_EXIST);
        }
        // 2、不存在，保存信息；设置默认信息
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(member.getUserpswd()); //密码加密
        //设置密码
        member.setUserpswd(encode);
        member.setUsername(member.getLoginacct()); //设置用户名 数据库里username不能为空,否则报错
        member.setEmail(member.getEmail());
        //实名认证状态 0 - 未实名认证， 1 - 实名认证申请中， 2 - 已实名认证
        member.setAuthstatus("0");
        //用户类型: 0 - 个人， 1 - 企业
        member.setUsertype("0");
        //账户类型: 0 - 企业， 1 - 个体， 2 - 个人， 3 - 政府
        member.setAccttype("2");
        System.out.println("插入数据:"+member.getLoginacct());
        memberMapper.insertSelective(member);

    }

    @Override
    //登录验证
    public TMember login(String username, String password) {
        //将用户输入的密码进行加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
       // String encode = encoder.encode(password);
        //通过用户名查询数据库密码,得到用户集合
        TMemberExample example = new TMemberExample();
         example.createCriteria().andLoginacctEqualTo(username);
        List<TMember> tMembers = memberMapper.selectByExample(example);
        if(tMembers !=null && tMembers.size()==1) {
            TMember tMember = tMembers.get(0); //得到用户对象
            //密码比较,由于相同的密码,不同时间使用uuid加密生成不一样,需要使用matches方法
            boolean matches = encoder.matches(password, tMember.getUserpswd()); //判断BCrypt加密的两个密码是否一致
            return matches?tMember:null;
        }

        return null;
    }

    @Override
    public TMember findMemberById(Integer id) {
        return memberMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<TMemberAddress> findAddressList(Integer memberId) {
        TMemberAddressExample example = new TMemberAddressExample();
        example.createCriteria().andMemberidEqualTo(memberId);
        return tMemberAddressMapper.selectByExample(example);

    }
}

