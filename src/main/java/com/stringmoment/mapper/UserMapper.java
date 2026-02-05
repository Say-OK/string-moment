package com.stringmoment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stringmoment.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承BaseMapper<User>后，就有了基本的CRUD方法：
    // selectById(id), selectList(query), insert(entity), updateById(entity), deleteById(id)

}