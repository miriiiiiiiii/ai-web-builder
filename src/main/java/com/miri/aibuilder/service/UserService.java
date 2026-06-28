package com.miri.aibuilder.service;

import com.miri.aibuilder.model.vo.LoginUserVO;
import com.mybatisflex.core.service.IService;
import com.miri.aibuilder.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/miriiiiiiiii">程序员小马</a>
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param nickName   用户昵称
     * @param userAccount   用户账户
     * @param userEmail 用户邮箱
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String nickName, String userAccount, String userEmail, String userPassword, String checkPassword);

    /**
     * 加密用户密码
     * @param userPassword 原始密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    LoginUserVO getLoginUserVO(User user);
    /**
     * 账号登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request 用于记录登录态
     * @return 脱敏后的用户信息
     */
    LoginUserVO loginByAccount(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 邮箱登录
     * @param userEmail 用户邮箱
     * @param code 邮箱验证码
     * @param request 用于记录登录态
     * @return 脱敏后的用户信息
     */
    LoginUserVO loginByEmail(String userEmail, String code, HttpServletRequest request);

    /**
     * 发送邮箱验证码
     * @param userEmail 用户邮箱
     * @return 验证码
     */
    boolean sendEmailCode(String userEmail);

    /**
     * 用户注销
     * @param request
     * @return 是否登录成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);
}
