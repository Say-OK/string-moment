package com.stringmoment.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果类
 * 使用泛型T，可以返回任意类型的数据
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class Result<T> implements Serializable {  // 实现序列化接口
    
    private Integer code;    // 状态码，如200成功，400失败
    private String message;  // 提示信息
    private T data;          // 响应数据
    
    // 私有构造方法，通过静态方法创建对象（工厂模式）
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    // 成功响应 - 无数据
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    // 成功响应 - 有数据
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }
    
    // 成功响应 - 自定义消息
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }
    
    // 失败响应
    public static <T> Result<T> error(String message) {
        return new Result<>(400, message, null);
    }
    
    // 失败响应 - 自定义状态码
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}