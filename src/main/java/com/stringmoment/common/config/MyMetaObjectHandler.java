package com.stringmoment.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充处理器
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        // 所有表的插入操作都会走到这里
        // 使用 fillStrategy 而不是 strictInsertFill
        // fillStrategy 会自动检查字段是否存在，不存在就跳过
        this.fillStrategy(metaObject, "createTime", LocalDateTime.now());
        this.fillStrategy(metaObject, "updateTime", LocalDateTime.now());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");
        // 所有表的更新操作都会走到这里
        this.fillStrategy(metaObject, "updateTime", LocalDateTime.now());
    }
}