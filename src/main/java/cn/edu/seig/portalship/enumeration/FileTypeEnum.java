package cn.edu.seig.portalship.enumeration;

import lombok.Getter;

@Getter
public enum FileTypeEnum {

    IMAGE("image"),
    PDF("pdf"),
    EXCEL("excel"),
    OTHER("other");

    private final String type;

    FileTypeEnum(String type) {
        this.type = type;
    }
}
