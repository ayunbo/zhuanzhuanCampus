package com.zhuanzhuan.service.sellerauth.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.AdminSellerAuthPageQueryDTO;
import com.zhuanzhuan.dto.SellerAuthApplyDTO;
import com.zhuanzhuan.dto.SellerAuthAuditDTO;
import com.zhuanzhuan.entity.SellerAuth;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.mapper.sellerauth.SellerAuthMapper;
import com.zhuanzhuan.mapper.user.UserMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.sellerauth.SellerAuthService;
import com.zhuanzhuan.utils.IdGenerator;
import com.zhuanzhuan.vo.SellerAuthResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 閸楁牕顔嶇拋銈堢槈娑撴艾濮熺€圭偟骞?
 */
@Service
public class SellerAuthServiceImpl implements SellerAuthService {

    @Autowired
    private SellerAuthMapper sellerAuthMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void submitSellerAuth(SellerAuthApplyDTO sellerAuthApplyDTO) {
        //1閵嗕焦鐗庢宀€鏁电拠宄板弳閸欏倸顕挒?
        if (sellerAuthApplyDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2閵嗕浇骞忛崣鏍ц嫙閺嶏繝鐛欒ぐ鎾冲閻ц缍嶉悽銊﹀煕
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }
        if (!User.STATUS_NORMAL.equals(currentUser.getStatus())) {
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3閵嗕焦鐗庢灞界秼閸撳秷澶勯崣鐤潡閼瑰弶妲搁崥锕€鍘戠拋鍝ユ暤鐠?
        if (!User.ROLE_NORMAL.equals(currentUser.getRole())) {
            if (User.ROLE_SELLER.equals(currentUser.getRole())) {
                throw new BaseException(MessageConstant.ALREADY_SELLER);
            }
            throw new BaseException(MessageConstant.ROLE_NOT_ALLOW_APPLY);
        }

        //4閵嗕焦褰侀崣鏍ц嫙鐟欏嫯瀵栭崠鏍暤鐠囧嘲鐡у▓?
        String realName = trimToNull(sellerAuthApplyDTO.getRealName());
        String phone = trimToNull(sellerAuthApplyDTO.getPhone());
        String material = trimToNull(sellerAuthApplyDTO.getMaterial());

        //5閵嗕焦鐗庢灞筋潣閸氬秲鈧焦澧滈張鍝勫娇閵嗕焦娼楅弬娆忕箑婵?
        if (!StringUtils.hasText(realName)) {
            throw new BaseException(MessageConstant.REAL_NAME_EMPTY);
        }

        if (!StringUtils.hasText(phone)) {
            throw new BaseException(MessageConstant.PHONE_EMPTY);
        }

        if (!StringUtils.hasText(material)) {
            throw new BaseException(MessageConstant.MATERIAL_EMPTY);
        }

        //6閵嗕焦鐗庢灞藉坊閸欒尙鏁电拠椋庡Ц閹緤绱欐稉宥呭讲闁插秴顦插鍛吀/闁插秴顦查柅姘崇箖閿?
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth != null && SellerAuth.STATUS_PENDING.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.AUTH_ALREADY_PENDING);
        }
        if (latestAuth != null && SellerAuth.STATUS_APPROVED.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.AUTH_ALREADY_APPROVED);
        }

        //7閵嗕胶绮嶇憗鍛暤鐠囧嘲鐤勬担鎾宠嫙閹绘劒姘?
        SellerAuth sellerAuth = new SellerAuth();
        sellerAuth.setId(IdGenerator.nextId());
        sellerAuth.setUserId(userId);
        sellerAuth.setRealName(realName);
        sellerAuth.setStudentNo(currentUser.getStudentNo());
        sellerAuth.setPhone(phone);
        sellerAuth.setMaterial(material);
        sellerAuth.setStatus(SellerAuth.STATUS_PENDING);

        int rows = sellerAuthMapper.insert(sellerAuth);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.SELLER_AUTH_SUBMIT_FAILED);
        }
    }

    @Override
    public SellerAuthResultVO getCurrentSellerAuthResult() {
        //1閵嗕浇骞忛崣鏍х秼閸撳秶娅ヨぐ鏇犳暏閹寸īD
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        //2閵嗕焦鐓＄拠銏℃付鏉╂垳绔村▎陇顓荤拠浣筋唶瑜版洖鑻熼弽锟犵崣
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth == null) {
            throw new BaseException(MessageConstant.NO_SELLER_AUTH_RECORD);
        }

        //3閵嗕浇娴嗛幑銏犺嫙鏉╂柨娲栫拋銈堢槈缂佹挻鐏?
        SellerAuthResultVO vo = new SellerAuthResultVO();
        BeanUtils.copyProperties(latestAuth, vo);
        vo.setStatusDesc(toStatusDesc(latestAuth.getStatus()));
        return vo;
    }

    @Override
    public void checkCurrentUserIsSeller() {
        //1閵嗕浇骞忛崣鏍х秼閸撳秶娅ヨぐ鏇犳暏閹寸īD
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        //2閵嗕焦鐗庢宀€鏁ら幋宄扮摠閸︺劋绗栫憴鎺曞娑撳搫宕犵€?
        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }
        if (!User.ROLE_SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
    }

    @Override
    public PageResult pageQuerySellerAuth(AdminSellerAuthPageQueryDTO pageQueryDTO) {
        //1閵嗕礁顦╅悶鍡楀瀻妞ら潧鍙嗛崣鍌欑瑢姒涙顓婚崐?
        AdminSellerAuthPageQueryDTO queryDTO = pageQueryDTO == null ? new AdminSellerAuthPageQueryDTO() : pageQueryDTO;
        int page = (queryDTO.getPage() == null || queryDTO.getPage() < 1) ? 1 : queryDTO.getPage();
        int pageSize = (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) ? 10 : queryDTO.getPageSize();

        //2閵嗕焦鐗庢灞借嫙婢跺嫮鎮婄拋銈堢槈閻樿埖鈧胶鐡柅澶嬫蒋娴?
        if (queryDTO.getStatus() == null) {
            queryDTO.setStatus(SellerAuth.STATUS_PENDING);
        } else if (!isValidSellerAuthStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.SELLER_AUTH_STATUS_INVALID);
        }

        //3閵嗕浇顫夐懠鍐ㄥ閸欘垶鈧鐡柅澶婄摟濞?
        queryDTO.setName(trimToNull(queryDTO.getName()));
        queryDTO.setPhone(trimToNull(queryDTO.getPhone()));
        queryDTO.setStudentNo(trimToNull(queryDTO.getStudentNo()));

        //4閵嗕焦澧界悰灞藉瀻妞ゅ灚鐓＄拠銏犺嫙鐞涖儱鍘栭悩鑸碘偓浣瑰伎鏉?
        PageHelper.startPage(page, pageSize);
        List<SellerAuthResultVO> records = sellerAuthMapper.pageQuery(queryDTO);
        Page<SellerAuthResultVO> pageInfo = (Page<SellerAuthResultVO>) records;

        for (SellerAuthResultVO record : records) {
            record.setStatusDesc(toStatusDesc(record.getStatus()));
        }

        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    @Transactional
    public void auditSellerAuth(SellerAuthAuditDTO sellerAuthAuditDTO) {
        //1閵嗕焦鐗庢灞筋吀閺嶇鍙嗛崣鍌欑瑢韫囧懓顩︾€涙顔?
        if (sellerAuthAuditDTO == null
                || sellerAuthAuditDTO.getAuthId() == null
                || sellerAuthAuditDTO.getStatus() == null) {
            throw new BaseException(MessageConstant.AUDIT_PARAM_INCOMPLETE);
        }

        //2閵嗕焦鐗庢灞筋吀閺嶅摜娲伴弽鍥╁Ц閹礁鎮庡▔鏇熲偓?
        Integer targetStatus = sellerAuthAuditDTO.getStatus();
        if (!SellerAuth.STATUS_APPROVED.equals(targetStatus)
                && !SellerAuth.STATUS_REJECTED.equals(targetStatus)) {
            throw new BaseException(MessageConstant.AUDIT_STATUS_INVALID);
        }

        //3閵嗕線鈹忛崶鐐叉簚閺咁垱鐗庢宀勨攺閸ョ偛甯崶?
        String rejectReason = trimToNull(sellerAuthAuditDTO.getReason());
        if (SellerAuth.STATUS_REJECTED.equals(targetStatus)) {
            if (!StringUtils.hasText(rejectReason)) {
                throw new BaseException(MessageConstant.REJECT_REASON_REQUIRED);
            }
        }

        //4閵嗕焦鐓＄拠銏犺嫙閺嶏繝鐛欑拋銈堢槈閻㈠疇顕悩鑸碘偓?
        SellerAuth sellerAuth = sellerAuthMapper.getById(sellerAuthAuditDTO.getAuthId());
        if (sellerAuth == null) {
            throw new BaseException(MessageConstant.AUTH_NOT_FOUND);
        }
        if (!SellerAuth.STATUS_PENDING.equals(sellerAuth.getStatus())) {
            throw new BaseException(MessageConstant.AUTH_ALREADY_AUDITED);
        }

        //5閵嗕焦鐗庢宀€顓搁悶鍡楁喅閻ц缍嶉幀?
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        //6閵嗕線鈧俺绻冮崷鐑樻珯閺嶏繝鐛欑悮顐㈩吀閺嶅摜鏁ら幋椋庡Ц閹?
        if (SellerAuth.STATUS_APPROVED.equals(targetStatus)) {
            User authUser = userMapper.getById(sellerAuth.getUserId());
            if (authUser == null) {
                throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
            }
            if (!User.STATUS_NORMAL.equals(authUser.getStatus())) {
                throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
            }
        }

        //7閵嗕焦娲块弬鎷岊吇鐠囦礁顓搁弽鍝ョ波閺?
        SellerAuth updateEntity = new SellerAuth();
        updateEntity.setId(sellerAuthAuditDTO.getAuthId());
        updateEntity.setStatus(targetStatus);
        updateEntity.setReason(SellerAuth.STATUS_REJECTED.equals(targetStatus) ? rejectReason : null);
        updateEntity.setAuditAdminId(adminId);
        updateEntity.setAuditTime(LocalDateTime.now());

        int rows = sellerAuthMapper.updateAuditById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.AUDIT_FAILED);
        }

        //8閵嗕礁顓搁弽鎼佲偓姘崇箖閸氬骸宕岀痪褏鏁ら幋铚傝礋閸楁牕顔?
        if (SellerAuth.STATUS_APPROVED.equals(targetStatus)) {
            int roleRows = userMapper.updateRoleById(sellerAuth.getUserId(), User.ROLE_SELLER);
            if (roleRows <= 0) {
                throw new BaseException(MessageConstant.AUDIT_FAILED);
            }
        }
    }

    private boolean isValidSellerAuthStatus(Integer status) {
        return SellerAuth.STATUS_PENDING.equals(status)
                || SellerAuth.STATUS_APPROVED.equals(status)
                || SellerAuth.STATUS_REJECTED.equals(status)
                || SellerAuth.STATUS_REVOKED.equals(status);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String toStatusDesc(Integer status) {
        if (SellerAuth.STATUS_PENDING.equals(status)) {
            return "待审核";
        }
        if (SellerAuth.STATUS_APPROVED.equals(status)) {
            return "已通过";
        }
        if (SellerAuth.STATUS_REJECTED.equals(status)) {
            return "已驳回";
        }
        if (SellerAuth.STATUS_REVOKED.equals(status)) {
            return "已撤回";
        }
        return "未知状态";
    }
}
