package com.zhuanzhuan.platform.review.controller.user;

import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.dto.ReviewPageQueryDTO;
import com.zhuanzhuan.dto.ReviewSubmitDTO;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.review.service.ReviewService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.utils.AliOssUtil;
import com.zhuanzhuan.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Tag(name = "用户评价接口")
@RestController
@RequestMapping("/user/review")
public class ReviewController {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> IMAGE_SUFFIX_SET = Set.of("jpg", "jpeg", "png", "webp", "gif");

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Operation(summary = "上传评价图片")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(MessageConstant.FILE_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BaseException(MessageConstant.FILE_SIZE_EXCEEDED);
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BaseException(MessageConstant.REVIEW_IMAGE_TYPE_NOT_ALLOWED);
        }

        String suffix = resolveFileSuffix(file.getOriginalFilename());
        if (!IMAGE_SUFFIX_SET.contains(suffix)) {
            throw new BaseException(MessageConstant.REVIEW_IMAGE_TYPE_NOT_ALLOWED);
        }

        try {
            String url = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename(), "review");
            return Result.success(url);
        } catch (Exception ex) {
            throw new BaseException(MessageConstant.FILE_UPLOAD_FAILED);
        }
    }

    @Operation(summary = "提交评价")
    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody ReviewSubmitDTO dto) {
        reviewService.submit(dto);
        return Result.success();
    }

    @Operation(summary = "查询订单评价")
    @GetMapping("/order/{orderId}")
    public Result<ReviewVO> getOrderReview(@PathVariable Long orderId) {
        return Result.success(reviewService.getOrderReview(orderId));
    }

    @Operation(summary = "分页查询商品评价")
    @GetMapping("/goods/{goodsId}")
    public Result<PageResult> pageByGoodsId(@PathVariable Long goodsId, ReviewPageQueryDTO dto) {
        return Result.success(reviewService.pageByGoodsId(goodsId, dto));
    }

    private String resolveFileSuffix(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).trim().toLowerCase(Locale.ROOT);
    }
}
