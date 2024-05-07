package com.su.wemedia.config;

import com.su.utils.common.AppJwtUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 抽出去 也放在了commmon中 因为多个地方会用  获取当前登录的用户id
 */
public class RequestContextUtil {
    /**
     * 获取请求头
     * @param key
     * @return
     */
    //RequestContextHolder 是Spring 提供的一个用来暴露Request 对象的工具
    // 利用RequestContextHolder可以在一个请求线程中获取到Request，避免了Request 从头传到尾的情况。
    public static String getHeader(String key){
        //获取HttpServletRequest
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        return request.getHeader(key);
    }

    /**
     * 获取token
     * @return
     */

    public static Map<String,Object> token(){
        String token = getHeader("token");
        return AppJwtUtil.getClaimsBody(token);
    }

    /**
     * 获取token指定的key
     */
    public static <T>T get(String key){
        Map<String, Object> token = token();
        return (T) token.get(key);
    }
}
