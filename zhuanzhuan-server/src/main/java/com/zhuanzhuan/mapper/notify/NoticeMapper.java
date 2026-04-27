package com.zhuanzhuan.mapper.notify;

import com.zhuanzhuan.entity.notify.Notice;
import com.zhuanzhuan.vo.notify.NoticeMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NoticeMapper {

    int insert(Notice notice);

    Notice getLatestByUserId(@Param("userId") Long userId);

    List<NoticeMessageVO> listByUserId(@Param("userId") Long userId,
                                       @Param("readStatus") Integer readStatus,
                                       @Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

    Integer countUnreadByUserId(@Param("userId") Long userId);

    Notice getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int markRead(@Param("id") Long id,
                 @Param("userId") Long userId,
                 @Param("readTime") LocalDateTime readTime);

    int markAllRead(@Param("userId") Long userId,
                    @Param("readTime") LocalDateTime readTime);

    int markChatNoticesReadByBizId(@Param("userId") Long userId,
                                   @Param("bizId") Long bizId,
                                   @Param("readTime") LocalDateTime readTime);
}
