package com.ps.controller;

import com.ps.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.img.path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {

        //原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID生成新的文件名，防止文件名称重复，覆盖文件
        String fileName = UUID.randomUUID().toString() + suffix;
        //创建一个目录对象
        File dir = new File(basePath + fileName);
        //防止目录不存在
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //将临时文件转存到别的位置
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return R.success(fileName);
    }

    @GetMapping("/download")
    public R<String> download(String name, HttpServletResponse response) {

        response.setContentType("image/jpeg");

        FileInputStream fis = null;
        ServletOutputStream os = null;

        try {
            fis = new FileInputStream(new File(basePath + name));
            os = response.getOutputStream();

            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) != -1) {
                os.write(buf, 0, len);
                os.flush();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                fis.close();
                os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return R.success("图片下载成功");
    }

}
