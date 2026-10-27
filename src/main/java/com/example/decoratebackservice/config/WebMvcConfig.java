package com.example.decoratebackservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源映射：把上传目录暴露成可访问的 URL。
 * <p>
 * 图片本体存放在服务器磁盘（而非数据库 BLOB），数据库只保存 URL 字符串，
 * 这样列表查询、备份、浏览器缓存都不会被二进制内容拖累。
 * <p>
 * 映射两个路径：
 * <ul>
 *     <li>/uploads/** —— 后端直连访问</li>
 *     <li>/api/uploads/** —— 与前端 axios 的 /api 前缀保持一致，避免前端还要额外拼路径</li>
 * </ul>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 图片存放目录，与 FileUploadController 使用同一配置项
     */
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**", "/api/uploads/**")
                .addResourceLocations(toResourceLocation(uploadDir))
                // 浏览器缓存 1 小时；文件名带 UUID，内容变更必然是新的 URL
                .setCachePeriod(3600);
    }

    /**
     * 把目录转成 Spring 资源定位串，必须是 file: 结尾带斜杠的 URI 形式
     *
     * @param dir 相对或绝对目录
     * @return 形如 file:///F:/xxx/uploads/ 的定位串
     */
    private String toResourceLocation(String dir) {
        Path path = Paths.get(dir).toAbsolutePath().normalize();
        String uri = path.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
