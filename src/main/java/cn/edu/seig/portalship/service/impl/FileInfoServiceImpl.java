package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.constant.JwtClaimsConstant;
import cn.edu.seig.portalship.constant.MessageConstant;
import cn.edu.seig.portalship.mapper.FileInfoMapper;
import cn.edu.seig.portalship.model.entity.FileInfo;
import cn.edu.seig.portalship.model.vo.FileInfoVO;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IFileInfoService;
import cn.edu.seig.portalship.service.MinioService;
import cn.edu.seig.portalship.util.ThreadLocalUtil;
import cn.edu.seig.portalship.util.TypeConversionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements IFileInfoService {

    @Autowired
    private FileInfoMapper fileInfoMapper;
    @Autowired
    private MinioService minioService;

    @Override
    public Result<FileInfoVO> uploadFile(MultipartFile file, Long relateId, String relateType) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));

        String folder = "portal-ship/" + relateType;
        String fileUrl = minioService.uploadFile(file, folder);

        FileInfo fileInfo = new FileInfo();
        fileInfo.setRelateId(relateId);
        fileInfo.setRelateType(relateType);
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFilePath(fileUrl);
        fileInfo.setFileType(getFileType(file.getContentType()));
        fileInfo.setFileSize(file.getSize());
        fileInfo.setUploadBy(userId);
        fileInfo.setUploadTime(LocalDateTime.now());

        fileInfoMapper.insert(fileInfo);

        FileInfoVO vo = new FileInfoVO();
        BeanUtils.copyProperties(fileInfo, vo);
        return Result.success(vo);
    }

    @Override
    public Result<List<FileInfoVO>> getFilesByRelate(Long relateId, String relateType) {
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("relate_id", relateId).eq("relate_type", relateType)
                .orderByDesc("upload_time");

        List<FileInfoVO> voList = fileInfoMapper.selectList(wrapper).stream().map(f -> {
            FileInfoVO vo = new FileInfoVO();
            BeanUtils.copyProperties(f, vo);
            return vo;
        }).toList();

        return Result.success(voList);
    }

    @Override
    public Result deleteFile(Long fileId) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo != null) {
            minioService.deleteFile(fileInfo.getFilePath());
            fileInfoMapper.deleteById(fileId);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    public Result deleteFileByUrl(String fileUrl) {
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("file_path", fileUrl);
        FileInfo fileInfo = fileInfoMapper.selectOne(wrapper);
        if (fileInfo != null) {
            minioService.deleteFile(fileUrl);
            fileInfoMapper.deleteById(fileInfo.getId());
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    private String getFileType(String contentType) {
        if (contentType == null) return "other";
        if (contentType.startsWith("image")) return "image";
        if (contentType.equals("application/pdf")) return "pdf";
        if (contentType.contains("excel") || contentType.contains("spreadsheet")) return "excel";
        return "other";
    }
}
