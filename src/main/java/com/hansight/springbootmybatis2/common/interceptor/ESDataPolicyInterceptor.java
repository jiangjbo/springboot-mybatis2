package com.hansight.springbootmybatis2.common.interceptor;

import com.hansight.springbootmybatis2.common.pojo.User;
import com.hansight.springbootmybatis2.common.utils.CookieUtil;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import net.minidev.json.JSONObject;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ESDataPolicyInterceptor implements HandlerInterceptor{
    private static final Logger logger = LoggerFactory.getLogger(ESDataPolicyInterceptor.class);

    public static final InheritableThreadLocal<String> userId = new InheritableThreadLocal<>();
    public static final String USER_ID = "user_id";

    //在请求处理之前进行调用（Controller方法调用之前
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        try{
            User user = getCurrentUser(httpServletRequest);
            if (user!=null){
                userId.set(user.getId());
            }
        }catch (Exception e){
            logger.error("set user_id error",e);
        }
        return true;
    }

    private User getCurrentUser(HttpServletRequest request) {
        String token = CookieUtil.getCookie(request, "TOKEN");
        return userFromToken(token);
    }

    private User userFromToken(String token) {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            Payload payload = jwsObject.getPayload();
            JSONObject jsonOBj = payload.toJSONObject();
            User user = new User();
            user.setId((String) jsonOBj.get("uid"));
            user.setLoginName((String) jsonOBj.get("ln"));
            user.setRealName((String) jsonOBj.get("rn"));
            user.setSuperAdmin(Boolean.valueOf(jsonOBj.get("sadm").toString()));
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    public static Header getUserIdHeader(){
        if(userId.get() != null){
            return new BasicHeader(USER_ID, userId.get());
        }else {
            return null;
        }
    }

}
