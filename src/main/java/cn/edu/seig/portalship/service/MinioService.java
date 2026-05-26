package cn.edu.seig.portalship.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    String uploadFile(MultipartFile file, String folder);

    void deleteFile(String fileUrl);
}
