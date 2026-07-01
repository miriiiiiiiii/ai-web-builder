package com.miri.aibuilder.aop;

import com.miri.aibuilder.annotation.AuthCheck;
import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.model.entity.User;
import com.miri.aibuilder.model.enums.UserRoleEnum;
import com.miri.aibuilder.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1.获取要访问的方法所需的权限
        String mustRole = authCheck.mustRole();
        UserRoleEnum  roleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 2.方法不需要权限，直接放行
        if (roleEnum == null) {
            return joinPoint.proceed();
        }
        // 3.必须有该权限才通过
        // 获取当前用户具有的权限
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum useRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 没有权限，拒绝
        if (useRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 方法要求管理员权限，而用户没有管理员权限
        if (UserRoleEnum.ADMIN.equals(roleEnum) && !UserRoleEnum.ADMIN.equals(useRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 4.通过权限校验，放行
        return joinPoint.proceed();
    }
}

