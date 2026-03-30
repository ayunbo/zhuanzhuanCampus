package com.zhuanzhuan.platform.history.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.entity.BrowseHistory;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.UserBrowseHistoryItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BrowseHistoryMapper {

    @AutoFill(OperationType.INSERT)
    @Insert("""
            insert into browsehistory (
                user_id, goods_id, browse_count, last_browse_time,
                create_time, update_time, create_user, update_user
            ) values (
                #{userId}, #{goodsId}, #{browseCount}, #{lastBrowseTime},
                #{createTime}, #{updateTime}, #{createUser}, #{updateUser}
            )
            on duplicate key update
                browse_count = browse_count + 1,
                last_browse_time = values(last_browse_time),
                update_time = values(update_time),
                update_user = values(update_user)
            """)
    int upsert(BrowseHistory browseHistory);

    @Select("select count(1) from goods where id = #{goodsId}")
    Integer countGoodsById(Long goodsId);

    List<UserBrowseHistoryItemVO> listByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Delete("delete from browsehistory where user_id = #{userId} and id = #{historyId}")
    int deleteByUserIdAndId(@Param("userId") Long userId, @Param("historyId") Long historyId);

    @Delete("delete from browsehistory where user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
