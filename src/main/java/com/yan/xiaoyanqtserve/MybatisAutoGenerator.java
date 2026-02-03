package com.yan.xiaoyanqtserve;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.ITableFieldAnnotationHandler;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.builder.Entity;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.model.AnnotationAttributes;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MybatisPlus代码生成器
 * V3.5.11
 *
 * @author yanchen
 */
public class MybatisAutoGenerator {
    /**
     * 数据库配置
     * dbTableNames：要生成的数据库表名，按需要修改
     */
    public static final String dataBaseName = "db_xiaoyan_ai_qt";
    public static final String db_url = "jdbc:mysql://localhost:3306/" + dataBaseName +
            "?useSSL=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    public static final String db_username = "root";
    public static final String db_password = "root";

    /**
     * 要生成的数据库表名，按需要修改
     */
    public static final String[] tableNameList = {"admin_user","key_info","request_log"};
    /**
     * 设置父包名
     */
    public static final String parentPackageName = "com.yan.xiaoyanqtserve";
    /**
     * 调整注释日期时间格式
     */
    public static final String commentDate = "yyyy-MM-dd";


    public static void main(String[] args) {
        // 项目根路径 E:\Project\CompletedProject\xiaoyanAI\xiaoyanAI
        String rootPath = System.getProperty("user.dir");
        // 子模块名
        String subModuleName = "";
        //  自定义mapper.xml文件输出目录
        String mapperXmlSrc =  rootPath + "\\src\\main\\resources\\mapper";
        //  指定输出目录
        String outputDir = rootPath + "\\src\\main\\java";
        if(!subModuleName.isEmpty()){
            mapperXmlSrc =  rootPath + "\\" + subModuleName + "\\src\\main\\resources\\mapper";
            outputDir = rootPath + "\\" + subModuleName + "\\src\\main\\java";
        }

        for (String tableName : tableNameList) {
            create(tableName, outputDir, mapperXmlSrc);
        }

    }

    /**
     * 创建
     * @param tableName 表名
     * @param outputDir 输出目录
     * @param mapperXmlSrc mapper.xml文件输出目录
     */
    public static void create(String tableName,String outputDir,String mapperXmlSrc) {
        FastAutoGenerator.create(db_url, db_username, db_password)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author("烟尘") // 设置作者
                            .outputDir(outputDir) // 指定输出目录
                            .commentDate(commentDate)  // 调整注释日期时间格式
                            .dateType(DateType.TIME_PACK)  // 时间策略
                            // .enableSwagger() //开启实体属性注解（swagger2）
                            .disableOpenDir()   // 禁止打开输出目录
                            .build();
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent(parentPackageName) // 设置父包名
                            .entity("entity") // 实体层
                            .controller("controller.sys")  // 控制层
                            .mapper("mapper")  // mapper层
                            .service("service") // 服务层
                            .serviceImpl("service.impl") // 服务实现类
                            .pathInfo(Collections.singletonMap(OutputFile.xml, mapperXmlSrc))
                            .build();
                })

                // 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude(tableName)                  // 设置需要生成的表名
                            .addTablePrefix("t_", "c_")         // 设置过滤表前缀

                            // entity 配置策略
                            .entityBuilder()
                            .enableFileOverride()               // 覆盖已生成文件
                            .enableLombok()                     // 开启Lombok
                            .versionColumnName("version")       // 标注乐观锁字段
                            .logicDeleteColumnName("deleted")   // 说明逻辑删除（软删除）是哪个字段
                            .enableTableFieldAnnotation()       // 属性上加说明注解(swagger)
                            // 自定义字段注解处理器
                            .tableFieldAnnotationHandler(new MyAnnotationHandler())


                            // mapper 策略配置
                            .mapperBuilder()
                            .enableFileOverride()               // 覆盖已生成文件
                            .superClass(BaseMapper.class)       // 继承哪个父类
                            .formatMapperFileName("%sMapper")
                            .mapperAnnotation(Mapper.class)        // @mapper 开启
                            .formatXmlFileName("%sMapper")      // xml名

                            // service 配置策略
                            .serviceBuilder()
                            .enableFileOverride()               // 覆盖已生成文件
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")

                            // controller 策略配置
                            .controllerBuilder()
                            .enableFileOverride()               // 覆盖已生成文件
                            .formatFileName("%sController")
                            .enableRestStyle()                  // 开启 RestController
                            .build();

                })

                .execute();
    }

    /**
     * 自定义字段注解处理器
     */
    static class MyAnnotationHandler implements ITableFieldAnnotationHandler {
        @Override
        public List<AnnotationAttributes> handle(TableInfo tableInfo, TableField tableField) {
            List<AnnotationAttributes> annotationAttributesList = new ArrayList<>();
            GlobalConfig globalConfig = tableInfo.getGlobalConfig();
            Entity entity = tableField.getEntity();
            String comment = tableField.getComment();
            if (StringUtils.isNotBlank(comment)) {
                if (globalConfig.isSpringdoc()) {
                    //@Schema(description = "${field.comment}")
                    String displayName = String.format("@Schema(description = \"%s\")", comment);
                    annotationAttributesList.add(new AnnotationAttributes(displayName, "io.swagger.v3.oas.annotations.media.Schema"));
                } else if (globalConfig.isSwagger()) {
                    String displayName = String.format("@ApiModelProperty(\"%s\")", comment);
                    annotationAttributesList.add(new AnnotationAttributes(displayName, "io.swagger.annotations.ApiModelProperty"));
                }
            }
            if (tableField.isKeyFlag()) {
                IdType idType = entity.getIdType();
                if (tableField.isKeyIdentityFlag()) {
                    //@TableId(value = "${field.annotationColumnName}", type = IdType.AUTO)
                    String displayName = String.format("@TableId(value = \"%s\", type = IdType.AUTO)", tableField.getAnnotationColumnName());
                    annotationAttributesList.add(new AnnotationAttributes(displayName, TableId.class.getName()));
                } else if (idType != null) {
                    //@TableId(value = "${field.annotationColumnName}", type = IdType.${idType})
                    String displayName = String.format("@TableId(value = \"%s\", type = IdType.%s)", tableField.getAnnotationColumnName(), idType);
                    annotationAttributesList.add(new AnnotationAttributes(displayName, TableId.class.getName()));
                } else if (tableField.isConvert()) {
                    //@TableId("${field.annotationColumnName}")
                    String displayName = String.format("@TableId(\"%s\")", tableField.getAnnotationColumnName());
                    annotationAttributesList.add(new AnnotationAttributes(displayName, TableId.class.getName()));
                }
            } else {
                String fill = tableField.getFill();
                if (StringUtils.isNotBlank(fill)) {
                    if (tableField.isConvert()) {
                        //@TableField(value = "${field.annotationColumnName}", fill = FieldFill.${field.fill})
                        String displayName = String.format("@TableField(value = \"%s\", fill = FieldFill.%s)", tableField.getAnnotationColumnName(), fill);
                        annotationAttributesList.add(new AnnotationAttributes(displayName, com.baomidou.mybatisplus.annotation.TableField.class.getName()));
                    } else {
                        //@TableField(fill = FieldFill.${field.fill})
                        String displayName = String.format("@TableField(fill = FieldFill.%s)", fill);
                        annotationAttributesList.add(new AnnotationAttributes(displayName, com.baomidou.mybatisplus.annotation.TableField.class.getName()));
                    }
                } else {
                    if (tableField.isConvert()) {
                        //@TableField("${field.annotationColumnName}")
                        String displayName = String.format("@TableField(\"%s\")", tableField.getAnnotationColumnName());
                        annotationAttributesList.add(new AnnotationAttributes(displayName, com.baomidou.mybatisplus.annotation.TableField.class.getName()));
                    }
                }
                if (tableField.isVersionField()) {
                    // @Version
                    annotationAttributesList.add(new AnnotationAttributes(Version.class));
                }
                if (tableField.isLogicDeleteField()) {
                    //@TableLogic
                    annotationAttributesList.add(new AnnotationAttributes(TableLogic.class));
                }

                String pkg = tableField.getColumnType().getPkg();
                if (pkg != null && pkg.equals("java.time.LocalDateTime")) {
                    // 添加LocalDateTime的JsonFormat注解
                    annotationAttributesList.add(new AnnotationAttributes(
                            "@JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\", timezone = \"GMT+8\")",
                            "com.fasterxml.jackson.annotation.JsonFormat"));

                }
            }
            return annotationAttributesList;
        }
    }

}


