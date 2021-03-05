package com.offcn.project.controller;

import com.alibaba.fastjson.JSON;
import com.offcn.dycommon.enums.ProjectStatusEnume;
import com.offcn.dycommon.response.AppResponse;
//import com.offcn.project.contants.ProjectContant;
import com.offcn.project.contants.ProjectContant;
import com.offcn.project.po.TProject;
import com.offcn.project.po.TProjectImages;
import com.offcn.project.po.TReturn;
import com.offcn.project.po.TType;
import com.offcn.project.service.ProjectCreateService;
import com.offcn.project.service.ProjectInfoService;
import com.offcn.project.vo.BaseVo;
import com.offcn.project.vo.req.ProjectBaseInfoVo;
import com.offcn.project.vo.req.ProjectRedisStorageVo;
import com.offcn.project.vo.req.ProjectReturnVo;
import com.offcn.project.vo.resp.ProjectDetailVo;
import com.offcn.project.vo.resp.ProjectVo;
import com.offcn.utils.OSSTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "项目模块(文件上传) 项目初始化 项目的保存")
@Slf4j //日志注解
@RestController
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    private ProjectInfoService projectInfoService;
    @Autowired
    private OSSTemplate ossTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProjectCreateService projectCreateService;


    @ApiOperation(value = "这是一个文件上传的方法")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "文件地址", name = "files", required = true)
    })
    @GetMapping("/upload")
    public AppResponse upload(@RequestParam("file") MultipartFile[] files) throws IOException {
        ArrayList<String> list = new ArrayList<>();
        if (files != null && files.length > 0) {  //判断是否为空
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String upload = ossTemplate.upload(file.getInputStream(), file.getOriginalFilename());//上传会返回url地址
                    list.add(upload);
                }
            }
        }
        Map<Object, Object> map = new HashMap<>();
        map.put("urls", list);
        log.debug("oss的信息:{},图片的地址:{}", ossTemplate, list); //{}会被后面的字段替代,然后打印出来
        return AppResponse.ok(map);
    }

    @ApiOperation(value = "这是一个项目初始化第一步,阅读协议")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "会员的令牌", name = "accessToken", required = true)
    })
    @GetMapping("/init")
    public AppResponse<String> init(BaseVo vo) {
        //通过令牌,判断是否登录
        //获取令牌
        String accessToken = vo.getAccessToken();
        //去redis里通过令牌获取用户id
        String memberId = stringRedisTemplate.opsForValue().get(accessToken);
        if (StringUtils.isEmpty(memberId)) {
            //为空,未登录
            return AppResponse.fail("未登录");
        }
        int id = Integer.parseInt(memberId); //字符串转int类型
        String projectToken = projectCreateService.initCreateProject(id); //执行初始化
        System.out.println(projectToken);
        return AppResponse.ok(projectToken);

    }

    //保存项目的基本信息
    @ApiOperation("项目发起第二步-保存项目的基本信息")
    @PostMapping("/savebaseInfo")
    public AppResponse<String> savebaseInfo(ProjectBaseInfoVo vo) {
        //1、取得redis中之前存储JSON结构的项目信息
        String orignal = stringRedisTemplate.opsForValue().get(ProjectContant.TEMP_PROJECT_PREFIX + vo.getProjectToken());
        //2、转换为redis存储对应的vo
        ProjectRedisStorageVo projectRedisStorageVo = JSON.parseObject(orignal, ProjectRedisStorageVo.class);
        //3、将页面收集来的数据，复制到和redis映射的vo中
        BeanUtils.copyProperties(vo, projectRedisStorageVo);
        //临时对象存入redis
        //4、将这个Vo对象再转换为json字符串
        String jsonString = JSON.toJSONString(projectRedisStorageVo);
        //5、重新更新到redis
        stringRedisTemplate.opsForValue().set(ProjectContant.TEMP_PROJECT_PREFIX + vo.getProjectToken(), jsonString);
        return AppResponse.ok(vo.getProjectToken());

    }

    //
    @ApiOperation(value = "保存项目的第三步:保存项目的回报信息")
    @PostMapping("/savereturn")
    public AppResponse saveReturnInfo(@RequestBody List<ProjectReturnVo> pro) {
        //拿到令牌
        ProjectReturnVo projectReturnVo = pro.get(0);
        String projectToken = projectReturnVo.getProjectToken(); //获取项目令牌
        //根据项目令牌 从redis获取临时对象
        String s = stringRedisTemplate.opsForValue().get(ProjectContant.TEMP_PROJECT_PREFIX + projectToken);
        //json字符串转临时对象
        ProjectRedisStorageVo projectRedisStorageVo = JSON.parseObject(s, ProjectRedisStorageVo.class);
        //将页面的数据封装到临时对象的回报列表的对象
        ArrayList<TReturn> returns = new ArrayList<>();
        for (ProjectReturnVo returnVo : pro) {
            TReturn tReturn = new TReturn();
            BeanUtils.copyProperties(returnVo, tReturn);
            returns.add(tReturn);
        }
        projectRedisStorageVo.setProjectReturns(returns);
        //临时对象存入redis
        String jsonString = JSON.toJSONString(projectRedisStorageVo);
        stringRedisTemplate.opsForValue().set(ProjectContant.TEMP_PROJECT_PREFIX + projectToken, jsonString);
        return AppResponse.ok(projectToken);

    }

    //保存项目
    @ApiOperation("项目发起第4步-保存项目")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken",value = "用户令牌",required = true),
            @ApiImplicitParam(name = "projectToken",value="项目标识",required = true),
            @ApiImplicitParam(name="ops",value="用户操作类型 0-保存草稿 1-提交审核",required = true)})
    @PostMapping("/submit")
    public AppResponse saveProjectInfo(String accessToken, String projectToken, String ops) {
        //根据令牌查看用户在不在
        String memberId = stringRedisTemplate.opsForValue().get(accessToken);
        if (memberId == null) {
            return AppResponse.fail("请登录");
        }
        //通过项目令牌获取项目的临时对象
        String s = stringRedisTemplate.opsForValue().get(ProjectContant.TEMP_PROJECT_PREFIX + projectToken);
        //把json字符串转化为存储的临时对象
        ProjectRedisStorageVo projectRedisStorageVo = JSON.parseObject(s, ProjectRedisStorageVo.class);
        //判断ops 决定对项目如何操作  0--保存草稿  1-- 提交
        if (StringUtils.isNotEmpty(ops)) {
            if (ops.equals("0")) { //0草稿
                projectCreateService.saveProjectInfo(ProjectStatusEnume.DRAFT, projectRedisStorageVo);
                return AppResponse.ok("保存草稿成功");
            } else if (ops.equals("1")) { //1提交
                projectCreateService.saveProjectInfo(ProjectStatusEnume.SUBMIT_AUTH, projectRedisStorageVo);
                return AppResponse.ok("提交项目成功");
            } else {  //非法操作
                return AppResponse.fail("非法操作");
            }
        }
        return AppResponse.fail("操作失败");
    }

    //根据项目id查询回报列表
    @ApiOperation("得到回报列表")
    @GetMapping("/details/returns/{projectId}")
    public AppResponse<List<TReturn>> getReturnList(@PathVariable("projectId") Integer projectId){
        List<TReturn> returnList = projectInfoService.getReturnList(projectId);
        return AppResponse.ok(returnList);

    }

    @ApiOperation("获取系统所有的项目")
    @GetMapping("/all")
    public AppResponse<List<ProjectVo>> findAllProject() {
        // 1、创建集合存储全部项目的VO
        List<ProjectVo> prosVo = new ArrayList<>();
        // 2、查询全部项目
        List<TProject> pros = projectInfoService.findAllProject();
        //3、遍历项目集合
        for (TProject tProject : pros) {
//获取项目编号
            Integer id = tProject.getId();
//根据项目编号获取项目配图
            List<TProjectImages> images = projectInfoService.getProjectImages(id);
            ProjectVo projectVo = new ProjectVo();
            BeanUtils.copyProperties(tProject, projectVo);
            //遍历项目配图集合
            for (TProjectImages tProjectImages : images) {
                //如果图片类型是头部图片，则设置头部图片路径到项目VO
                if (tProjectImages.getImgtype() == 0) {
                    projectVo.setHeaderImage(tProjectImages.getImgurl());
                }
            }
//把项目vo添加到项目vo集合
            prosVo.add(projectVo);
        }
        return AppResponse.ok(prosVo);
    }

    @ApiOperation("获取项目信息详情")
    @GetMapping("/findProjectInfo/{projectId}")
    public AppResponse<ProjectDetailVo> findProjectInfo(@PathVariable("projectId") Integer projectId) {
        TProject projectInfo = projectInfoService. findProjectInfo(projectId);
        ProjectDetailVo projectVo = new ProjectDetailVo();
        // 1、查出这个项目的所有图片
        List<TProjectImages> projectImages = projectInfoService.getProjectImages(projectInfo.getId());
        List<String> detailsImage = projectVo.getDetailsImage();
        if(detailsImage==null){
            detailsImage=new ArrayList<>();
        }
        for (TProjectImages tProjectImages : projectImages) {
            if (tProjectImages.getImgtype() == 0) {
                projectVo.setHeaderImage(tProjectImages.getImgurl());
            } else {
                detailsImage.add(tProjectImages.getImgurl());
            }
        }
        projectVo.setDetailsImage(detailsImage);

        // 2、项目的所有支持回报；
        List<TReturn> returns = projectInfoService.getReturnList(projectInfo.getId());
        projectVo.setProjectReturns(returns);
        BeanUtils.copyProperties(projectInfo, projectVo);
        return AppResponse.ok(projectVo);
    }

    @ApiOperation("获取系统所有的项目分类")
    @GetMapping("/findAllType")
    public AppResponse<List<TType>> findAllType() {
        List<TType> types = projectInfoService. findAllType();
        return AppResponse.ok(types);
    }

    @ApiOperation("获取回报信息")
    @GetMapping("/returns/info/{returnId}")
    public AppResponse<TReturn> findReturnInfo(@PathVariable("returnId") Integer returnId){
        TReturn tReturn = projectInfoService. findReturnInfo(returnId);
        return AppResponse.ok(tReturn);
    }




}