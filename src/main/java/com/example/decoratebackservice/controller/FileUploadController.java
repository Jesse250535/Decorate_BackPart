package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 图片上传接口。
 * <p>
 * 设计要点：
 * <ul>
 *     <li>文件落磁盘，数据库只存返回的 URL，避免把二进制塞进 MySQL</li>
 *     <li>文件名用 UUID 重新生成，不使用原始文件名，从根上杜绝 ../ 目录穿越与重名覆盖</li>
 *     <li>扩展名由 Content-Type 推导，并校验文件头，防止改后缀上传脚本类文件</li>
 *     <li>按年月日分目录，避免单目录文件过多导致 ls / 备份变慢</li>
 * </ul>
 * 该接口需登录后访问（沿用全局 anyRequest().authenticated() 策略）。
 */
@Tag(name = "文件上传接口")
@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileUploadController {

    /**
     * 允许的图片类型 → 落盘扩展名
     */
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    /**
     * 单文件上限 5MB，与 application.properties 中的 max-file-size 保持一致
     */
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/api/uploads}")
    private String urlPrefix;

    @Operation(summary = "上传图片（返回可直接访问的 URL）")
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            return Result.error(400, "图片不能超过 5MB");
        }

        // 类型白名单：不信任原始文件名的后缀，只认 Content-Type
        String contentType = file.getContentType();
        String ext = contentType == null ? null : ALLOWED_TYPES.get(contentType.toLowerCase());
        if (ext == null) {
            return Result.error(400, "只支持 JPG / PNG / WEBP / GIF 格式的图片");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            return Result.error(500, "读取上传文件失败");
        }
        // Content-Type 可以伪造，再核对一次真实文件头
        if (!isSupportedImage(bytes)) {
            return Result.error(400, "文件内容不是有效的图片");
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetDir = baseDir.resolve(datePath).normalize();
        Path target = targetDir.resolve(fileName).normalize();
        // 防御性校验：最终路径必须仍在 upload 根目录内
        if (!target.startsWith(baseDir)) {
            return Result.error(400, "非法的文件路径");
        }

        try {
            Files.createDirectories(targetDir);
            Files.write(target, bytes);
        } catch (IOException e) {
            return Result.error(500, "图片保存失败：" + e.getMessage());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("url", urlPrefix + "/" + datePath + "/" + fileName);
        data.put("name", fileName);
        data.put("size", file.getSize());
        return Result.success("上传成功", data);
    }

    /**
     * 超过 multipart 上限时 Spring 会抛异常，默认返回 500 与一段堆栈信息；
     * 这里转成统一的 JSON，让前端能显示「图片过大」而不是「网络错误」
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Map<String, Object>> handleMaxSize(MaxUploadSizeExceededException e) {
        return Result.error(413, "图片不能超过 5MB");
    }

    /**
     * 校验文件头是否为受支持的图片格式
     *
     * @param b 文件内容
     * @return 是有效图片返回 true
     */
    private boolean isSupportedImage(byte[] b) {
        if (b.length < 12) {
            return false;
        }
        // JPEG：FF D8 FF
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG：89 50 4E 47
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return true;
        }
        // GIF：GIF8
        if (b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') {
            return true;
        }
        // WEBP：RIFF....WEBP
        return b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }
}
