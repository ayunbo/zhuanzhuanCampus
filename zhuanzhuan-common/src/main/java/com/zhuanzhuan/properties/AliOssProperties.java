package com.zhuanzhuan.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "zhuanzhuan.alioss") //配置属性类：作用读取配置文件的配置项，封装成java对象
@Data
public class AliOssProperties {

    private String endpoint;
    private String region;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;

}
