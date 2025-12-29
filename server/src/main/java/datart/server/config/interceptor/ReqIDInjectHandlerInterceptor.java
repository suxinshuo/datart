package datart.server.config.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import datart.core.entity.SystemConstant;
import datart.core.utils.RequestIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求 id 注入拦截器.
 *
 * @author suxinshuo
 * @date 2024/6/26 14:57
 */
@Slf4j
@Component
public class ReqIDInjectHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            MDC.remove(SystemConstant.REQUEST_ID);
            String reqId = request.getHeader(SystemConstant.REQUEST_ID);
            if (StringUtils.isBlank(reqId)) {
                reqId = RequestIDUtil.getCurrentRequestID();
                if (StringUtils.isNotBlank(reqId)) {
                    log.debug("REQUEST_ID from thread context -> {}", reqId);
                }
            }
            if (StringUtils.isBlank(reqId)) {
                reqId = RequestIDUtil.generateRequestID();
                log.debug("REQUEST_ID local generated -> {}", reqId);
            }
            RequestIDUtil.setCurrentRequestID(reqId);
            MDC.put(SystemConstant.REQUEST_ID, reqId);
            response.setHeader(SystemConstant.REQUEST_ID, reqId);
            log.debug("REQUEST_ID inject success. REQUEST_ID: {}", reqId);
        } catch (Exception e) {
            log.error("注入 request id 失败.", e);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RequestIDUtil.removeCurrentRequestID();
        MDC.remove(SystemConstant.REQUEST_ID);
    }

}
