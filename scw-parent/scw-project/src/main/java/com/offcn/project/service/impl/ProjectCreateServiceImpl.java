package com.offcn.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.dycommon.enums.ProjectStatusEnume;
import com.offcn.project.contants.ProjectContant;
import com.offcn.project.enums.ProjectImageTypeEnume;
import com.offcn.project.mapper.*;
import com.offcn.project.po.*;
import com.offcn.project.service.ProjectCreateService;
import com.offcn.project.vo.req.ProjectRedisStorageVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectCreateServiceImpl implements ProjectCreateService {

    @Autowired
    private TProjectMapper tProjectMapper;
    @Autowired
    private TProjectImagesMapper tProjectImagesMapper;
    @Autowired
    private TProjectTypeMapper tProjectTypeMapper;
    @Autowired
    private TProjectTagMapper tProjectTagMapper;
    @Autowired
    private TReturnMapper tReturnMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public String initCreateProject(Integer memberId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        ProjectRedisStorageVo projectRedisStorageVo = new ProjectRedisStorageVo();
        //存项目临时令牌到redis
        //存memberId
        projectRedisStorageVo.setMemberid(memberId);
        projectRedisStorageVo.setProjectToken(token);
        String jsonString = JSON.toJSONString(projectRedisStorageVo);//转换为json字符串类型
        stringRedisTemplate.opsForValue().set(ProjectContant.TEMP_PROJECT_PREFIX+token,jsonString); //存redis
        //
        return token;
    }

    @Override
    public void saveProjectInfo(ProjectStatusEnume auth, ProjectRedisStorageVo projectVo) {
      //1.创建项目
        TProject project = new TProject();
        BeanUtils.copyProperties(projectVo,project);
        //设置时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        project.setCreatedate(format);
        //设置项目状态
        project.setStatus(auth.getCode()+""); //byte+""转字符串类型
        //2.保存到数据库
        tProjectMapper.insertSelective(project);
        //拿到项目id
        Integer projectId = project.getId();
        //3.保存图片
        String headerImage = projectVo.getHeaderImage();
        TProjectImages tProjectImages = new TProjectImages(null, projectId, headerImage, ProjectImageTypeEnume.HEADER.getCode());//创建了构造器后可以直接创建,不用set一个一个加
        tProjectImagesMapper.insertSelective(tProjectImages); //保存头图到数据库
        //--详图
        List<String> detailsImage = projectVo.getDetailsImage();
        if(!CollectionUtils.isEmpty(detailsImage)){
            for (String s : detailsImage) {
                TProjectImages detail = new TProjectImages(null, projectId, s, ProjectImageTypeEnume.DETAILS.getCode());//创建了构造器后可以直接创建,不用set一个一个加
                tProjectImagesMapper.insertSelective(detail); //保存详图到数据库
            }
        }
        //4.保存标签
        List<Integer> tagids = projectVo.getTagids();
        if(!CollectionUtils.isEmpty(tagids)){
            for (Integer tagid : tagids) {
                TProjectTag tProjectTag = new TProjectTag(null, projectId, tagid);
                tProjectTagMapper.insertSelective(tProjectTag);
            }
        }
        //保存分类
        List<Integer> typeids = projectVo.getTypeids();
        if(!CollectionUtils.isEmpty(typeids)){
            for (Integer typeid : typeids) {
                TProjectType tProjectType = new TProjectType(null, projectId, typeid);
                tProjectTypeMapper.insertSelective(tProjectType);
            }
        }
        //6.保存回报
        List<TReturn> projectReturns = projectVo.getProjectReturns();
        if(!CollectionUtils.isEmpty(projectReturns)){
            for (TReturn tReturn : projectReturns) {
                tReturn.setProjectid(projectId);
                tReturnMapper.insertSelective(tReturn);

            }
        }
        //7.清空redis
        stringRedisTemplate.delete(ProjectContant.TEMP_PROJECT_PREFIX+projectVo.getProjectToken());
    }

}
