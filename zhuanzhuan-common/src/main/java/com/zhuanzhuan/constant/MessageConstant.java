package com.zhuanzhuan.constant;

/**
 * 统一业务提示常量
 */
public final class MessageConstant {

    private MessageConstant() {
    }

    public static final String UNKNOWN_ERROR = "未知错误，请稍后重试";
    public static final String REQUEST_PARAM_NULL = "请求参数不能为空";
    public static final String STATUS_PARAM_INVALID = "状态参数不合法";

    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String ADMIN_NOT_LOGIN = "管理员未登录";
    public static final String CURRENT_USER_NOT_FOUND = "当前用户不存在";

    public static final String LOGIN_PARAM_EMPTY = "账号或密码不能为空";
    public static final String ACCOUNT_FORMAT_INVALID = "账号格式不正确";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_LOCKED = "账号已被禁用";
    public static final String PASSWORD_EMPTY = "密码不能为空";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String PASSWORD_FORMAT_INVALID = "密码格式不正确，长度应为 6-20 且不能包含空格";

    public static final String STUDENT_NO_EMPTY = "学号不能为空";
    public static final String STUDENT_NO_FORMAT_INVALID = "学号格式不正确，应为 6-20 位字母或数字";
    public static final String STUDENT_NO_ALREADY_EXISTS = "学号已存在";
    public static final String PHONE_EMPTY = "手机号不能为空";
    public static final String PHONE_FORMAT_INVALID = "手机号格式不正确";
    public static final String PHONE_ALREADY_BOUND = "手机号已被绑定";
    public static final String NAME_FORMAT_INVALID = "姓名或昵称格式不正确";
    public static final String REAL_NAME_EMPTY = "真实姓名不能为空";
    public static final String REAL_NAME_FORMAT_INVALID = "真实姓名格式不正确";
    public static final String AVATAR_URL_INVALID = "头像地址格式不正确";
    public static final String CAMPUS_FORMAT_INVALID = "校区长度不能超过 50";
    public static final String INTRO_TOO_LONG = "个人简介长度不能超过 255";
    public static final String PROFILE_UPDATE_EMPTY = "请至少提交一项要修改的资料";
    public static final String USER_DELETE_FORBIDDEN_SELLER = "卖家账号暂不支持自主注销，请联系管理员";
    public static final String USER_DELETE_FORBIDDEN_PENDING_AUTH = "存在待审核的卖家认证申请，暂不能注销";

    public static final String ADMIN_ACCOUNT_NOT_FOUND = "管理员账号不存在";
    public static final String ADMIN_ACCOUNT_DISABLED = "管理员账号已被禁用";
    public static final String ADMIN_ALREADY_EXISTS = "管理员账号已存在";
    public static final String ADMIN_NOT_FOUND = "管理员不存在";
    public static final String ADMIN_DELETE_SELF_NOT_ALLOWED = "不能删除当前登录管理员";
    public static final String ADMIN_DISABLE_SELF_NOT_ALLOWED = "不能禁用当前登录管理员";
    public static final String LAST_ACTIVE_ADMIN_NOT_ALLOWED = "至少保留一个正常状态的管理员";

    public static final String SELLER_ONLY = "仅卖家可访问该接口";
    public static final String ROLE_NOT_ALLOW_APPLY = "当前角色不允许提交卖家认证申请";
    public static final String ALREADY_SELLER = "当前账号已是卖家，无需重复申请";
    public static final String AUTH_ALREADY_PENDING = "已有待审核申请，请勿重复提交";
    public static final String AUTH_ALREADY_APPROVED = "你的卖家认证已通过，无需重复申请";
    public static final String SELLER_AUTH_STATUS_INVALID = "卖家认证状态不合法";
    public static final String SELLER_AUTH_STUDENT_NO_MISMATCH = "提交的学号与当前登录账号不一致";
    public static final String MATERIAL_EMPTY = "认证材料不能为空";
    public static final String MATERIAL_URL_INVALID = "认证材料地址格式不正确";

    public static final String NO_SELLER_AUTH_RECORD = "暂无卖家认证申请记录";
    public static final String AUDIT_PARAM_INCOMPLETE = "审核参数不完整";
    public static final String AUDIT_STATUS_INVALID = "审核状态只允许通过或驳回";
    public static final String REJECT_REASON_REQUIRED = "驳回时必须填写原因";
    public static final String REJECT_REASON_TOO_LONG = "驳回原因长度不能超过 255";
    public static final String AUTH_NOT_FOUND = "认证申请不存在";
    public static final String AUTH_ALREADY_AUDITED = "该申请已审核，请勿重复操作";
    public static final String AUDIT_FAILED = "审核失败";

    public static final String FILE_EMPTY = "上传文件不能为空";
    public static final String FILE_SIZE_EXCEEDED = "上传文件大小不能超过 5MB";
    public static final String FILE_TYPE_NOT_ALLOWED = "仅支持 jpg/jpeg/png/webp/gif/pdf 格式文件";
    public static final String UPLOAD_CATEGORY_INVALID = "上传分类不合法";
    public static final String FILE_UPLOAD_FAILED = "文件上传失败，请稍后重试";

    public static final String ALREADY_EXISTS = "已存在";
    public static final String GOODS_NOT_FOUND = "商品不存在";
    public static final String GOODS_CREATE_FAILED = "商品草稿创建失败";
    public static final String GOODS_UPDATE_FAILED = "商品信息更新失败";
    public static final String GOODS_CATEGORY_REQUIRED = "商品分类不能为空";
    public static final String GOODS_TITLE_REQUIRED = "商品标题不能为空";
    public static final String GOODS_TITLE_TOO_LONG = "商品标题长度不能超过100";
    public static final String GOODS_PRICE_INVALID = "商品价格必须大于0";
    public static final String GOODS_OLD_PRICE_INVALID = "原价不能小于售价";
    public static final String GOODS_QUALITY_INVALID = "商品成色仅允许1到5";
    public static final String GOODS_LOCATION_TOO_LONG = "面交地点长度不能超过120";
    public static final String GOODS_COVER_INVALID = "商品封面地址格式不正确";
    public static final String GOODS_STATUS_INVALID = "商品状态不合法";
    public static final String GOODS_EDIT_FORBIDDEN = "当前状态下不允许编辑商品";
    public static final String GOODS_SUBMIT_AUDIT_STATUS_INVALID = "只有草稿或已驳回商品才能提交审核";
    public static final String GOODS_SUBMIT_AUDIT_FAILED = "商品提交审核失败";
    public static final String GOODS_AUDIT_STATUS_INVALID = "商品审核结果只允许通过或驳回";
    public static final String GOODS_AUDIT_STATUS_FLOW_INVALID = "当前商品不处于待审核状态";
    public static final String GOODS_AUDIT_FAILED = "商品审核失败";
    public static final String GOODS_ON_SHELF_STATUS_INVALID = "只有已下架商品才能重新上架";
    public static final String GOODS_ON_SHELF_FAILED = "商品上架失败";
    public static final String GOODS_OFF_SHELF_STATUS_INVALID = "只有在售商品才能下架";
    public static final String GOODS_OFF_SHELF_FAILED = "商品下架失败";
    public static final String GOODS_MARK_SOLD_STATUS_INVALID = "只有在售或锁定商品才能标记售出";
    public static final String GOODS_MARK_SOLD_FAILED = "商品售出状态更新失败";
    public static final String GOODS_STATS_UPDATE_EMPTY = "请至少提供一个商品统计增量";
    public static final String GOODS_STATS_UPDATE_FAILED = "商品统计更新失败";
    public static final String FAVORITE_ALREADY_EXISTS = "请勿重复收藏";
    public static final String FAVORITE_NOT_SUPPORTED_STATUS = "当前商品状态不支持收藏";
    public static final String CATEGORY_NAME_EMPTY = "分类名称不能为空";
    public static final String CATEGORY_PARENT_ID_INVALID = "父分类ID不合法";
    public static final String CATEGORY_NAME_ALREADY_EXISTS = "同级分类名称已存在";
    public static final String CATEGORY_PARENT_DISABLED_FOR_CREATE = "父分类未启用，不能新增启用状态的子分类";
    public static final String CATEGORY_PARENT_DISABLED_FOR_MOVE = "父分类未启用，不能挂载启用状态的子分类";
    public static final String CATEGORY_PARENT_DISABLED_FOR_ENABLE = "父分类未启用，不能单独启用当前分类";
    public static final String CATEGORY_CREATE_FAILED = "新增分类失败";
    public static final String CATEGORY_NOT_FOUND = "分类不存在";
    public static final String CATEGORY_HAS_CHILDREN_CANNOT_CHANGE_PARENT = "存在子分类，不允许修改父分类";
    public static final String CATEGORY_UPDATE_FAILED = "更新分类失败";
    public static final String CATEGORY_SORT_UPDATE_FAILED = "更新分类排序失败";
    public static final String CATEGORY_HAS_CHILDREN_CANNOT_DELETE = "存在子分类，不能删除";
    public static final String CATEGORY_HAS_GOODS_CANNOT_DELETE = "分类下存在商品，不能删除";
    public static final String CATEGORY_DELETE_FAILED = "删除分类失败";
    public static final String CATEGORY_PARENT_NOT_FOUND = "父分类不存在";
    public static final String CATEGORY_LEVEL_EXCEEDED = "分类层级不能超过3级";
    public static final String REVIEW_ORDER_ID_REQUIRED = "orderId is required";
    public static final String REVIEW_GOODS_ID_REQUIRED = "goodsId is required";
    public static final String REVIEW_SCORE_INVALID = "review score must be between 1 and 5";
    public static final String REVIEW_CONTENT_TOO_LONG = "review content cannot exceed 500 characters";
    public static final String REVIEW_ANONYMOUS_INVALID = "anonymous flag must be 0 or 1";
    public static final String REVIEW_IMAGES_TOO_MANY = "review images cannot exceed 6";
    public static final String REVIEW_IMAGE_URL_INVALID = "review image url is invalid";
    public static final String REVIEW_IMAGE_TYPE_NOT_ALLOWED = "only image files are allowed";
    public static final String REVIEW_ORDER_NOT_COMPLETED = "only completed orders can be reviewed";
    public static final String REVIEW_ALREADY_EXISTS = "this order has already been reviewed";
    public static final String REVIEW_NOT_ALLOWED = "you are not allowed to view this review";
    public static final String REVIEW_NOT_FOUND = "review not found";
    public static final String REVIEW_SUBMIT_FAILED = "review submit failed";
}
