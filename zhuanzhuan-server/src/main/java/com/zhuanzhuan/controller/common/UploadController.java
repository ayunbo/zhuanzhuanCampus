package com.zhuanzhuan.controller.common;

import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Tag(name = "用户文件上传接口")
@RestController
@Slf4j
@RequestMapping({"/user", "/admin"})
public class UploadController {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Operation(summary = "上传用户相关文件到 OSS")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "category", required = false) String category) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(MessageConstant.FILE_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BaseException(MessageConstant.FILE_SIZE_EXCEEDED);
        }

        String normalizedCategory = normalizeCategory(category);

        try {
            String url = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename(), normalizedCategory);
            return Result.success(url);
        } catch (Exception ex) {
            log.error("上传文件失败, category={}", normalizedCategory, ex);
            throw new BaseException(MessageConstant.FILE_UPLOAD_FAILED);
        }
    }

    private String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return "common";
        }
        return category.trim().toLowerCase(Locale.ROOT);
    }
}
