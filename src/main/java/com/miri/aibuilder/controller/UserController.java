package com.miri.aibuilder.controller;

import cn.hutool.core.bean.BeanUtil;
import com.miri.aibuilder.annotation.AuthCheck;
import com.miri.aibuilder.common.BaseResponse;
import com.miri.aibuilder.common.DeleteRequest;
import com.miri.aibuilder.common.ResultUtils;
import com.miri.aibuilder.constant.UserConstant;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.exception.ThrowUtils;
import com.miri.aibuilder.model.dto.user.AccountLoginRequest;
import com.miri.aibuilder.model.dto.user.EmailLoginRequest;
import com.miri.aibuilder.model.dto.user.UserRegisterRequest;
import com.miri.aibuilder.model.dto.user.UserAddRequest;
import com.miri.aibuilder.model.dto.user.UserQueryRequest;
import com.miri.aibuilder.model.dto.user.UserUpdateRequest;
import com.miri.aibuilder.model.vo.LoginUserVO;
import com.miri.aibuilder.model.vo.UserVO;
import com.miri.aibuilder.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.miri.aibuilder.model.entity.User;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author <a href="https://github.com/miriiiiiiiii">程序员小马</a>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest registerRequest) {
        ThrowUtils.throwIf(registerRequest == null, ErrorCode.PARAMS_ERROR);
        String nickName = registerRequest.getNickName();
        String userAccount = registerRequest.getUserAccount();
        String userEmail= registerRequest.getUserEmail();
        String userPassword =registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();
        long result = userService.userRegister(nickName, userAccount, userEmail, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 账号登录
     */
    @PostMapping("/account/login")
    public BaseResponse<LoginUserVO> loginByAccount(@RequestBody AccountLoginRequest loginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(loginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.loginByAccount(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);

    }

    /**
     * 邮箱登录
     */
    @PostMapping("/email/login")
    public BaseResponse<LoginUserVO> loginByEmail(@RequestBody EmailLoginRequest loginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(loginRequest == null, ErrorCode.PARAMS_ERROR);
        String userEmail = loginRequest.getUserEmail();
        String code = loginRequest.getCode();
        LoginUserVO loginUserVO = userService.loginByEmail(userEmail, code, request);
        return ResultUtils.success(loginUserVO);

    }


    @PostMapping("/email/code")
    public BaseResponse<Boolean> sendEmailCode(@RequestBody EmailLoginRequest loginRequest) {
        ThrowUtils.throwIf(loginRequest == null, ErrorCode.PARAMS_ERROR);
        userService.sendEmailCode(loginRequest.getUserEmail());
        return ResultUtils.success(true);
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    // region 管理员增删改查用户
    /**
     * 添加用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole= UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest addRequest) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        Long userId = userService.addUser(addRequest);
        return ResultUtils.success(userId);
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole= UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <=0, ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole= UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() <=0, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 根据id获取用户
     */
    @GetMapping("/get")
    @AuthCheck(mustRole= UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id <=0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取脱敏后的用户
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole= UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserVO(Long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }


    @PostMapping("/list/vo")
    @AuthCheck(mustRole= UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        //获取分页参数
        int pageNum = userQueryRequest.getPageNum();
        int pageSize = userQueryRequest.getPageSize();
        // 获取分页后的用户数据
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize), userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
    // endregion


    /**
     * 保存用户。
     *
     * @param user 用户
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @AuthCheck(mustRole= UserConstant.ADMIN_ROLE)
    @PostMapping("save")
    public boolean save(@RequestBody User user) {
        return userService.save(user);
    }
//    /**
//     * 根据主键删除用户。
//     *
//     * @param id 主键
//     * @return {@code true} 删除成功，{@code false} 删除失败
//     */
//    @DeleteMapping("remove/{id}")
//    public boolean remove(@PathVariable Long id) {
//        return userService.removeById(id);
//    }
//
//    /**
//     * 根据主键更新用户。
//     *
//     * @param user 用户
//     * @return {@code true} 更新成功，{@code false} 更新失败
//     */
//    @PutMapping("update")
//    public boolean update(@RequestBody User user) {
//        return userService.updateById(user);
//    }
//
//    /**
//     * 查询所有用户。
//     *
//     * @return 所有数据
//     */
//    @GetMapping("list")
//    public List<User> list() {
//        return userService.list();
//    }
//
//    /**
//     * 根据主键获取用户。
//     *
//     * @param id 用户主键
//     * @return 用户详情
//     */
//    @GetMapping("getInfo/{id}")
//    public User getInfo(@PathVariable Long id) {
//        return userService.getById(id);
//    }
//
//    /**
//     * 分页查询用户。
//     *
//     * @param page 分页对象
//     * @return 分页对象
//     */
//    @GetMapping("page")
//    public Page<User> page(Page<User> page) {
//        return userService.page(page);
//    }

}
