//package com.heima.gateway.app.filter;
//
//
//import com.heima.gateway.app.util.AppJwtUtil;
//import io.jsonwebtoken.Claims;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.util.ObjectUtils;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.nio.charset.StandardCharsets;
//
//@Configuration
//@Order(1)
//public class AuthorizeFilter implements GlobalFilter {
//
//    /**
//     * 过滤方法
//     *
//     * @param exchange
//     * @param chain
//     * @return
//     */
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        //1)获取请求对象Request、阿应对象Response
//        ServerHttpRequest request = exchange.getRequest();
//        ServerHttpResponse response = exchange.getResponse();
//        //1.1.判断是否是登录
//        if (request.getURI().getPath().contains("/login")) {
//            //放行
//            return chain.filter(exchange);
//        }
//
//
//        //2)获欣访求头中的令牌数据 authorization/token
//        String token = request.getHeaders().getFirst("Authorization");
//
//        //3)判断令牌是否有效
//        if (!ObjectUtils.isEmpty(token)) {
//            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
//            int status = AppJwtUtil.verifyToken(claimsBody);
//            if (status == -1 || status == 0) {
//                return chain.filter(exchange);//交给下一个过滤器
//            }
//
//        }
//        //5)否则拦截
//        response.setStatusCode(HttpStatus.UNAUTHORIZED);
//        // 设置字符编码，否则在浏览器中会出现中文乱码
//        response.getHeaders().add("Content-Type", "text/plain; charset=UTF-8");
//        return response.writeWith(Mono.just(response.bufferFactory().wrap("请先登录!".getBytes(StandardCharsets.UTF_8))));
//
//
//    }
//}
