package com.offcn.project.service;

import com.offcn.project.po.TProject;
import com.offcn.project.po.TProjectImages;
import com.offcn.project.po.TReturn;
import com.offcn.project.po.TType;

import java.util.List;

public interface ProjectInfoService {
   /**
    * 获取项目回报列表
    * @param projectId
    * @return
    */
   public List<TReturn> getReturnList(Integer projectId);
   List<TProjectImages> getProjectImages(Integer id);
   public List<TProject> findAllProject();
   TProject findProjectInfo(Integer projectId);
   List<TType> findAllType();
   TReturn findReturnInfo (Integer returnId);

}
