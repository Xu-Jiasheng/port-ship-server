package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.entity.FileInfo;
import cn.edu.seig.portalship.model.vo.FileInfoVO;
import cn.edu.seig.portalship.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFileInfoService extends IService<FileInfo> {

    Result<FileInfoVO> uploadFile(MultipartFile file, Long relateId, String relateType);

    Result<List<FileInfoVO>> getFilesByRelate(Long relateId, String relateType);

    Result deleteFile(Long fileId);

    Result deleteFileByUrl(String fileUrl);
}
