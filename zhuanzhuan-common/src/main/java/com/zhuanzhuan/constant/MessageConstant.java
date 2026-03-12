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

    public static final String REGISTER_FAILED = "注册失败";
    public static final String UPDATE_PROFILE_FAILED = "个人资料修改失败";
    public static final String DELETE_USER_FAILED = "注销用户失败";
    public static final String USER_DELETE_FORBIDDEN_SELLER = "卖家账号暂不支持自主注销，请联系管理员";
    public static final String USER_DELETE_FORBIDDEN_PENDING_AUTH = "存在待审核的卖家认证申请，暂不能注销";

    public static final String ADMIN_ACCOUNT_NOT_FOUND = "管理员账号不存在";
    public static final String ADMIN_ACCOUNT_DISABLED = "管理员账号已被禁用";
    public static final String ADMIN_USERNAME_EMPTY = "管理员账号不能为空";
    public static final String ADMIN_USERNAME_FORMAT_INVALID = "管理员账号格式不正确，应为 4-32 位字母数字下划线且字母开头";
    public static final String ADMIN_NAME_EMPTY = "管理员名称不能为空";
    public static final String ADMIN_STATUS_INVALID = "管理员状态不合法";
    public static final String ADMIN_ALREADY_EXISTS = "管理员账号已存在";
    public static final String ADMIN_NOT_FOUND = "管理员不存在";
    public static final String ADMIN_CREATE_FAILED = "管理员创建失败";
    public static final String ADMIN_UPDATE_FAILED = "管理员修改失败";
    public static final String ADMIN_DELETE_FAILED = "管理员删除失败";
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
    public static final String SELLER_AUTH_SUBMIT_FAILED = "卖家认证申请提交失败";
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
}
