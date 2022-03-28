package ru.baby_benz.kontur.intern.chartographer.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "service.image")
@ConstructorBinding
public class ImageProperties {
    private final String type;
    private final String parentPath;
    private final Charta charta;
    private final Fragment fragment;

    @Getter
    @RequiredArgsConstructor
    public static class Charta {
        private final MaxDimensions maxDimensions;

        @Getter
        @RequiredArgsConstructor
        public static class MaxDimensions {
            private final int width;
            private final int height;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class Fragment {
        private final MaxDimensions maxDimensions;

        @Getter
        @RequiredArgsConstructor
        public static class MaxDimensions {
            private final int width;
            private final int height;
        }
    }
}
