package com.zhuanzhuan.controller;

import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.goods.service.SellerGoodsService;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.UploadService;
import com.zhuanzhuan.utils.AliOssUtil;
import com.zhuanzhuan.utils.ValidationRuleUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Tag(name = "用户文件上传接口")
@RestController
@Slf4j
@RequestMapping({"/user", "/admin"})
public class UploadController {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final int MAX_FILE_COUNT = 9;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private SellerGoodsService sellerGoodsService;

    @Operation(summary = "上传用户相关文件到 OSS")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "category", required = false) String category,
                                 @RequestParam(value = "goodsId", required = false) Long goodsId) {
        String normalizedCategory = normalizeCategory(category);
        validateSingleFile(file);

        try {
            String url = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename(), normalizedCategory);
            persistGoodsImagesIfNecessary(normalizedCategory, goodsId, List.of(url));
            return Result.success(url);
        } catch (Exception ex) {
            log.error("上传文件失败, category={}", normalizedCategory, ex);
            throw new BaseException(MessageConstant.FILE_UPLOAD_FAILED);
        }
    }

    @Operation(summary = "批量上传图片到 OSS")
    @PostMapping("/uploads")
    public Result<List<String>> uploadBatch(@RequestParam("files") MultipartFile[] files,
                                            @RequestParam(value = "category", required = false) String category,
                                            @RequestParam(value = "goodsId", required = false) Long goodsId) {
        String normalizedCategory = normalizeCategory(category);
        validateBatchFiles(files);

        List<String> urls = new ArrayList<>(files.length);
        try {
            for (MultipartFile file : files) {
                urls.add(aliOssUtil.upload(file.getBytes(), file.getOriginalFilename(), normalizedCategory));
            }
            persistGoodsImagesIfNecessary(normalizedCategory, goodsId, urls);
            return Result.success(urls);
        } catch (Exception ex) {
            log.error("批量上传文件失败, category={}", normalizedCategory, ex);
            throw new BaseException(MessageConstant.FILE_UPLOAD_FAILED);
        }
    }

    @Operation(summary = "按 URL 删除 OSS 图片")
    @DeleteMapping("/upload")
    public Result<Void> delete(@RequestParam("url") String url) {
        if (!ValidationRuleUtil.isAllowedImageUrl(url)) {
            throw new BaseException(MessageConstant.FILE_TYPE_NOT_ALLOWED);
        }
        uploadService.deleteImage(url);
        return Result.success();
    }

    private String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return "common";
        }
        String normalized = category.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9/_-]{1,32}")) {
            throw new BaseException(MessageConstant.UPLOAD_CATEGORY_INVALID);
        }
        return normalized;
    }

    private void validateSingleFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(MessageConstant.FILE_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BaseException(MessageConstant.FILE_SIZE_EXCEEDED);
        }
        validateImageFile(file);
    }

    private void validateBatchFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BaseException(MessageConstant.FILE_EMPTY);
        }
        if (files.length > MAX_FILE_COUNT) {
            throw new BaseException(MessageConstant.FILE_COUNT_EXCEEDED);
        }
        for (MultipartFile file : files) {
            validateSingleFile(file);
        }
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BaseException(MessageConstant.FILE_TYPE_NOT_ALLOWED);
        }
        if (!ValidationRuleUtil.isAllowedImageFilename(file.getOriginalFilename())) {
            throw new BaseException(MessageConstant.FILE_TYPE_NOT_ALLOWED);
        }
    }

    private void persistGoodsImagesIfNecessary(String category, Long goodsId, List<String> imageUrls) {
        if (!"goods".equals(category) || goodsId == null || imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        sellerGoodsService.appendImages(goodsId, imageUrls);
    }
}
