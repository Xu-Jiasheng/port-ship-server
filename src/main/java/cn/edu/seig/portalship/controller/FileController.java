package cn.edu.seig.portalship.controller;

import cn.edu.seig.portalship.model.vo.FileInfoVO;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IFileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private IFileInfoService fileInfoService;

    @PostMapping("/upload")
    public Result<FileInfoVO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("relateId") Long relateId,
            @RequestParam("relateType") String relateType) {
        return fileInfoService.uploadFile(file, relateId, relateType);
    }

    @GetMapping("/getFiles")
    public Result<List<FileInfoVO>> getFiles(
            @RequestParam("relateId") Long relateId,
            @RequestParam("relateType") String relateType) {
        return fileInfoService.getFilesByRelate(relateId, relateType);
    }

    @DeleteMapping("/deleteFile/{id}")
    public Result deleteFile(@PathVariable("id") Long fileId) {
        return fileInfoService.deleteFile(fileId);
    }
}
