package com.zhuanzhuan.platform.collect.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.entity.Favorite;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.UserFavoritePageVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @AutoFill(OperationType.INSERT)
    @Insert("""
            insert ignore into favorite (
                user_id, goods_id, create_time, update_time, create_user, update_user
            ) values (
                #{userId}, #{goodsId}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser}
            )
            """)
    int insertIgnore(Favorite favorite);

    @Delete("delete from favorite where user_id = #{userId} and goods_id = #{goodsId}")
    int deleteByUserIdAndGoodsId(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    @Select("select count(1) from favorite where user_id = #{userId} and goods_id = #{goodsId}")
    Integer countByUserIdAndGoodsId(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    List<UserFavoritePageVO> pageByUserId(@Param("userId") Long userId, @Param("sortType") String sortType);
}
