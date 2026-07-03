package com.miri.aibuilder.service.impl;

import com.miri.aibuilder.mapper.AppMapper;
import com.miri.aibuilder.model.entity.App;
import com.miri.aibuilder.service.AppService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/miriiiiiiiii">程序员小马</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

}
