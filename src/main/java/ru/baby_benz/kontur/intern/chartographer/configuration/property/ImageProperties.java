package ru.baby_benz.kontur.intern.chartographer.configuration.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "image")
public class ImageProperties {
    private String parentPath;
    private String type;
}
