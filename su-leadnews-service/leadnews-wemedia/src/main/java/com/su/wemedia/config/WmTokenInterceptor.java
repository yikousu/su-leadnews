package com.su.wemedia.config;

import com.alibaba.fastjson.JSON;
import com.su.model.common.wemedia.pojos.WmUser;
import com.su.utils.common.AppJwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Component
public class WmTokenInterceptor implements HandlerInterceptor {
    /**
     * 获取请求头
     * 解析请求头
     * 将请求头存入到ThreadLocal中
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getHeader("token");
        if (!ObjectUtils.isEmpty(token)) {
            Claims body = AppJwtUtil.getClaimsBody(token);
            /**
             * 法一
             */
            // Integer id = Integer.valueOf(body.get("id").toString());
            //
            // WmUser wmUser = new WmUser();
            // wmUser.setId(id);
            //
            // WmThreadLocalUtils.set(wmUser);

            /**
             * 法二
             */
            WmUser wmUser = JSON.parseObject(JSON.toJSONString(body), WmUser.class);
            WmThreadLocalUtils.set(wmUser);
        }
        return true;
    }

    /**
     * 移除ThreadLocal防止内存溢出
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        WmThreadLocalUtils.remove();
    }
}
