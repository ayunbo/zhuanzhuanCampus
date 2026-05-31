package com.zhuanzhuan.platform.history.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.entity.SearchHistory;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.SearchHistoryItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchHistoryMapper {

    @AutoFill(OperationType.INSERT)
    @Insert("""
            insert into searchhistory (
                user_id, query_sign, keyword, category_id, seller_id, status, quality, location,
                min_price, max_price, sort_by, search_count, last_search_time,
                create_time, update_time, create_user, update_user
            ) values (
                #{userId}, #{querySign}, #{keyword}, #{categoryId}, #{sellerId}, #{status}, #{quality}, #{location},
                #{minPrice}, #{maxPrice}, #{sortBy}, #{searchCount}, #{lastSearchTime},
                #{createTime}, #{updateTime}, #{createUser}, #{updateUser}
            )
            on duplicate key update
                keyword = values(keyword),
                category_id = values(category_id),
                seller_id = values(seller_id),
                status = values(status),
                quality = values(quality),
                location = values(location),
                min_price = values(min_price),
                max_price = values(max_price),
                sort_by = values(sort_by),
                search_count = search_count + 1,
                last_search_time = values(last_search_time),
                update_time = values(update_time),
                update_user = values(update_user)
            """)
    int upsert(SearchHistory history);

    List<SearchHistoryItemVO> listByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Delete("delete from searchhistory where user_id = #{userId} and id = #{historyId}")
    int deleteByUserIdAndId(@Param("userId") Long userId, @Param("historyId") Long historyId);

    @Delete("delete from searchhistory where user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
