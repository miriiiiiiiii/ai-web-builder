package com.miri.aibuilder.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class MybatisFlexCodegen {
    //要生成的表名
    public static final String[] TABLE_NAMES = {"chat_history"};

    public static void main(String[] args) {
        //获取数据元信息
        Dict dict = YamlUtil.loadByPath("application.yml");
        Map<String, Object> datasourceConfig = dict.getByPath("spring.datasource");
        String url = String.valueOf(datasourceConfig.get("url"));
        String username = String.valueOf(datasourceConfig.get("username"));
        String password = String.valueOf(datasourceConfig.get("password"));
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //创建配置内容
        GlobalConfig globalConfig = createGlobalConfig();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfig() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        //先生成到临时目录，代码确定之后，再移动到对应的目录
        globalConfig.getPackageConfig()
                .setBasePackage("com.miri.aibuilder.genresult");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAMES)
                //设置逻辑删除字段
                .setLogicDeleteColumn("isDelete");

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        //设置生成 service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();

        //设置生成 controller
        globalConfig.enableController();

        //设置生成注释，比如作者和时间，避免后续多余代码的改动
        globalConfig.getJavadocConfig()
                .setAuthor("<a href=\"https://github.com/miriiiiiiiii\">程序员小马</a>")
                .setSince("");

        return globalConfig;
    }
}