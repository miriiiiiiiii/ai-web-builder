package com.miri.aibuilder.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.miri.aibuilder.config.EmailClient;
import com.miri.aibuilder.constant.UserConstant;
import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.mapper.UserMapper;
import com.miri.aibuilder.model.entity.User;
import com.miri.aibuilder.model.enums.UserRoleEnum;
import com.miri.aibuilder.model.vo.LoginUserVO;
import com.miri.aibuilder.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

import static com.miri.aibuilder.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/miriiiiiiiii">程序员小马</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Resource
    private UserMapper userMapper;
    
    @Resource
    private EmailClient emailClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public long userRegister(String nickName, String userAccount, String userEmail, String userPassword, String checkPassword) {
        // 1. 参数校验
        if (StringUtils.isAnyBlank(nickName, userAccount, userEmail, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (nickName.length() < 4 || nickName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称长度需为 4-20 个字符");
        }
        if (!userAccount.matches("^(?=.*[A-Za-z])[A-Za-z\\d]{4,16}$")) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "账号需为 4-16 位字母和数字，且不能全为数字"
            );
        }
        if (!userEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        if (!userPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$")) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "密码需为 8-20 位，且至少包含字母和数字"
            );
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2.查询账号是否存在
        synchronized (userAccount.intern()) {
            QueryWrapper qw = new QueryWrapper();
            qw.eq("userAccount", userAccount);
            long count = this.count(qw);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 3.加密
            String encryptPassword = getEncryptPassword(userPassword);
            // 4.创建用户，插入数据库
            User user = new User();
            user.setNickName(nickName);
            user.setUserEmail(userEmail);
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserRole(UserRoleEnum.USER.getValue());
            //设置默认头像
            user.setUserAvatar("https://timibucket.oss-cn-guangzhou.aliyuncs.com/5/default.jpg");
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }



    @Override
    public String getEncryptPassword(String userPassword) {
        final String SALT = "miri";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO loginByAccount(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 2.加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3.查询用户是否存在
        QueryWrapper qw = new QueryWrapper();
        qw.eq("userAccount", userAccount);
        qw.eq("userPassword", encryptPassword);
        User user = this.getOne(qw);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 4.记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 5.返回脱敏后的用户信息
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO loginByEmail(String userEmail, String code, HttpServletRequest request) {
        // 1.校验
        if (StringUtils.isAnyBlank(userEmail, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 从 Redis 获取验证码
        String key = "email:code:" + userEmail;
        String cacheCode = (String)redisTemplate.opsForValue().get(key);
        if (cacheCode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不存在或已过期");
        }
        if (!cacheCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        // 2.查询用户是否存在
        QueryWrapper qw = new QueryWrapper();
        qw.eq("userEmail", userEmail);
        User user = this.getOne(qw);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        // 3.记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // 4.删除 Redis 中的验证码
        redisTemplate.delete(key);

        // 5.返回脱敏后的用户信息
        return this.getLoginUserVO(user);
    }

    @Override
    public boolean sendEmailCode(String userEmail) {
        //1.校验
        if (StringUtils.isBlank(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱为空");
        }
        //2.客户端发送验证码
        String code = emailClient.sendEmailCode(userEmail);
        //3.将验证码存入redis
        String key = "email:code:" + userEmail;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        return false;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 1.判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        // 2.移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }


    @Override
    public User getLoginUser(HttpServletRequest request) {
        //  1.判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 2.从数据库查询（
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}
