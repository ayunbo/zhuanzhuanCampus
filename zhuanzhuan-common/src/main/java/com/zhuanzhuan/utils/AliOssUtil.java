package com.zhuanzhuan.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;

import com.zhuanzhuan.properties.AliOssProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@Slf4j
@Component
public class AliOssUtil {

    private static final String DEFAULT_CATEGORY = "common";
    private static final String DEFAULT_SUFFIX = ".bin";
    private static final Pattern INVALID_CATEGORY_CHAR_PATTERN = Pattern.compile("[^a-z0-9/_-]");
    private static final Pattern MULTIPLE_SLASH_PATTERN = Pattern.compile("/{2,}");

    @Autowired
    private AliOssProperties aliOssProperties;

    public String upload(byte[] content, String originalFilename) throws Exception {
        return upload(content, originalFilename, DEFAULT_CATEGORY);
    }

    public String upload(byte[] content, String originalFilename, String category) throws Exception {

        String endpoint = aliOssProperties.getEndpoint();
        String bucketName = aliOssProperties.getBucketName();
        String region = aliOssProperties.getRegion();


        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        String objectName = buildObjectName(originalFilename, category);

        // 创建OSSClient实例。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        } finally {
            ossClient.shutdown();
        }

        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
    }

    private String buildObjectName(String originalFilename, String category) {
        String normalizedCategory = normalizeCategory(category);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileSuffix = resolveFileSuffix(originalFilename);
        String newFileName = UUID.randomUUID().toString().replace("-", "") + fileSuffix;

        return normalizedCategory + "/" + datePath + "/" + newFileName;
    }

    private String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return DEFAULT_CATEGORY;
        }

        String normalized = category
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace('\\', '/');

        normalized = INVALID_CATEGORY_CHAR_PATTERN.matcher(normalized).replaceAll("-");
        normalized = MULTIPLE_SLASH_PATTERN.matcher(normalized).replaceAll("/");

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return StringUtils.hasText(normalized) ? normalized : DEFAULT_CATEGORY;
    }

    private String resolveFileSuffix(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return DEFAULT_SUFFIX;
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return DEFAULT_SUFFIX;
        }

        String suffix = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
        suffix = suffix.replaceAll("[^a-z0-9.]", "");
        if (!suffix.startsWith(".") || suffix.length() > 10) {
            return DEFAULT_SUFFIX;
        }

        return suffix;
    }
}
