package com.offcn.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OSSTest {
    public static void main(String[] args) throws IOException {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "http://oss-cn-beijing.aliyuncs.com";
// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        String accessKeyId = "LTAI4GC1FwrRvZbQ5VmwRgU4";
        String accessKeySecret = "OQloQbzxeNWw4zpJ37ahWVfj5djlR1";

// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

// 上传文件流。
        InputStream inputStream = new FileInputStream(new File("E:\\upload\\0a2704cc-2b56-4097-9541-ca423118fedc.jpg")); //本地文件位置
        ossClient.putObject("506a18729095805", "pic/506.jpg", inputStream); //上传后文件名字
        System.out.println("上传成功");

// 关闭OSSClient。
        ossClient.shutdown();

    }
}
