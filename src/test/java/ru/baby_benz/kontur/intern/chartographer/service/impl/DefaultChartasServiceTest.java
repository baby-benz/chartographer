package ru.baby_benz.kontur.intern.chartographer.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.core.io.InputStreamResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.baby_benz.kontur.intern.chartographer.configuration.ImageProperties;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.*;
import ru.baby_benz.kontur.intern.chartographer.service.ChartasService;
import ru.baby_benz.kontur.intern.chartographer.service.IOService;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(ImageProperties.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Slf4j
class DefaultChartasServiceTest {
    @Autowired
    private ImageProperties imageProperties;
    private ChartasService chartasService;
    @Mock
    private IOService ioService;

    @BeforeAll
    void init() {
        mockReadCharta();
        mockWriteCharta();
        mockRead();
        chartasService = new DefaultChartasService(imageProperties, ioService);
    }

    @BeforeEach
    void setUp() {
        File chartasFolder = new File(imageProperties.getParentPath());
        if (!chartasFolder.exists()) {
            chartasFolder.mkdir();
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(Path.of(imageProperties.getParentPath()))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach((File fileToBeDeleted) -> {
                    if (!fileToBeDeleted.delete()) {
                        log.error(
                                "Error while deleting test folder " + imageProperties.getParentPath()
                                        + ". Please, delete it manually"
                        );
                    }
                });
    }

    @Test
    void givenNegativeWidthAndHeight_whenCreateCharta_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.createCharta(-1, -1));
    }

    @Test
    void givenNegativeWidth_whenCreateCharta_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.createCharta(-1, 1));
    }

    @Test
    void givenNegativeHeight_whenCreateCharta_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.createCharta(1, -1));
    }

    @Test
    void givenZeroWidthAndHeight_whenCreateCharta_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.createCharta(0, 0));
    }

    @Test
    void givenZeroWidth_whenCreateCharta_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.createCharta(0, 1));
    }

    @Test
    void givenZeroHeight_whenCreateCharta_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.createCharta(1, 0));
    }

    @Test
    void givenExceedMaximumWidthAndHeight_whenCreateCharta_thenTooBigChartaExceptionIsThrown() {
        assertThrows(TooBigChartaException.class, () -> chartasService.createCharta(
                getChartaMaxWidth() + 1,
                getChartaMaxHeight() + 1
        ));
    }

    @Test
    void givenExceedMaximumWidth_whenCreateCharta_thenTooBigChartaExceptionIsThrown() {
        assertThrows(TooBigChartaException.class, () -> chartasService.createCharta(
                getChartaMaxWidth() + 1,
                1
        ));
    }

    @Test
    void givenExceedMaximumHeight_whenCreateCharta_thenTooBigChartaExceptionIsThrown() {
        assertThrows(TooBigChartaException.class, () -> chartasService.createCharta(
                1,
                getChartaMaxHeight() + 1
        ));
    }

    @Test
    void givenUpperThresholdWidthAndHeight_whenCreateCharta_thenUUIDFromStringWorks() {
        assertDoesNotThrow(() ->
                UUID.fromString(chartasService.createCharta(
                        getChartaMaxWidth(),
                        getChartaMaxHeight())
                )
        );
    }

    @Test
    void givenUpperThresholdWidth_whenCreateCharta_thenUUIDFromStringWorks() {
        assertDoesNotThrow(() ->
                UUID.fromString(chartasService.createCharta(
                        getChartaMaxWidth(),
                        1)
                )
        );
    }

    @Test
    void givenUpperThresholdHeight_whenCreateCharta_thenUUIDFromStringWorks() {
        assertDoesNotThrow(() ->
                UUID.fromString(chartasService.createCharta(
                        1,
                        getChartaMaxHeight())
                )
        );
    }

    @Test
    void givenMinimalWidthAndHeight_whenCreateCharta_thenUUIDFromStringWorks() {
        assertDoesNotThrow(() ->
                UUID.fromString(chartasService.createCharta(
                        1,
                        1)
                )
        );
    }

    @Test
    void givenNegativePlaneXAndY_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(FragmentNegativePlaneException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                -1,
                -1,
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenNegativePlaneX_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(FragmentNegativePlaneException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                -1,
                1,
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenNegativePlaneY_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(FragmentNegativePlaneException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                1,
                -1,
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenZeroXAndY_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(FragmentNegativePlaneException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                0,
                0,
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenZeroX_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(FragmentNegativePlaneException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                0,
                1,
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenZeroY_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(FragmentNegativePlaneException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                1,
                0,
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenMaxXAndY_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(NoIntersectionException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                getChartaMaxWidth(),
                getChartaMaxHeight(),
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenMaxX_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(NoIntersectionException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                getChartaMaxWidth(),
                1,
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenMaxY_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(NoIntersectionException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                1,
                getChartaMaxHeight(),
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenOutOfMaxXAndY_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(NoIntersectionException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                getChartaMaxWidth() + 1,
                getChartaMaxHeight() + 1,
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenOutOfMaxX_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(NoIntersectionException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                getChartaMaxWidth() + 1,
                1,
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenOutOfMaxY_whenPutFragment_thenExceptionIsThrown() {
        assertThrows(NoIntersectionException.class, () -> chartasService.putFragment(
                createMaxCharta(),
                1,
                getChartaMaxHeight() + 1,
                0,
                0,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenNegativeXAndYFragmentIntersects_whenPutFragment_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> chartasService.putFragment(
                createMaxCharta(),
                -1,
                -1,
                2,
                2,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenNegativeXFragmentIntersects_whenPutFragment_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> chartasService.putFragment(
                createMaxCharta(),
                -1,
                0,
                2,
                2,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenNegativeYFragmentIntersects_whenPutFragment_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> chartasService.putFragment(
                createMaxCharta(),
                0,
                -1,
                2,
                2,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenPositiveXAndYFragmentIntersects_whenPutFragment_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> chartasService.putFragment(
                createMaxCharta(),
                getChartaMaxWidth() - 1,
                getChartaMaxHeight() - 1,
                2,
                2,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenPositiveXFragmentIntersects_whenPutFragment_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> chartasService.putFragment(
                createMaxCharta(),
                getChartaMaxWidth() - 1,
                0,
                2,
                2,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenPositiveYFragmentIntersects_whenPutFragment_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> chartasService.putFragment(
                createMaxCharta(),
                0,
                getChartaMaxHeight() - 1,
                2,
                2,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenNonZeroXAndYInBoundsFragment_whenPutFragment_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> chartasService.putFragment(
                createMaxCharta(),
                1,
                1,
                2,
                2,
                new InputStreamResource(new ByteArrayInputStream(new byte[]{})))
        );
    }

    @Test
    void givenExceedMaximumWidth_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(TooBigFragmentException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                1,
                1,
                getFragmentMaxWidth() + 1,
                0)
        );
    }

    @Test
    void givenExceedMaximumHeight_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(TooBigFragmentException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                1,
                1,
                0,
                getFragmentMaxHeight() + 1)
        );
    }

    @Test
    void givenExceedMaximumWidthAndHeight_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(TooBigFragmentException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                1,
                1,
                getFragmentMaxWidth() + 1,
                getFragmentMaxHeight() + 1)
        );
    }

    @Test
    void givenNegativeWidthAndHeight_whenGetFragment_thenExceptionIsThrown() {

        assertThrows(NegativeDimensionsException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                0,
                0,
                -1,
                -1)
        );
    }

    @Test
    void givenNegativeWidth_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                0,
                0,
                -1,
                1)
        );
    }

    @Test
    void givenNegativeHeight_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                0,
                0,
                1,
                -1)
        );
    }

    @Test
    void givenZeroWidthAndHeight_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                0,
                0,
                0,
                0)
        );
    }

    @Test
    void givenZeroWidth_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                0,
                0,
                0,
                1)
        );
    }

    @Test
    void givenZeroHeight_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(NegativeDimensionsException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                0,
                0,
                1,
                0)
        );
    }

    @Test
    void givenNegativePlaneXAndY_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(FragmentNegativePlaneException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                -1,
                -1,
                1,
                1)
        );
    }

    @Test
    void givenNegativePlaneX_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(FragmentNegativePlaneException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                -1,
                0,
                1,
                1)
        );
    }

    @Test
    void givenNegativePlaneY_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(FragmentNegativePlaneException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                0,
                -1,
                1,
                1)
        );
    }

    @Test
    void givenOutOfMaxXAndY_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(NoIntersectionException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                getChartaMaxWidth() + 1,
                getChartaMaxHeight() + 1,
                1,
                1)
        );
    }

    @Test
    void givenOutOfMaxX_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(NoIntersectionException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                getChartaMaxWidth() + 1,
                1,
                1,
                1)
        );
    }

    @Test
    void givenOutOfMaxY_whenGetFragment_thenExceptionIsThrown() {
        assertThrows(NoIntersectionException.class, () -> chartasService.getFragment(
                createMaxCharta(),
                1,
                getChartaMaxHeight() + 1,
                1,
                1)
        );
    }

    @Test
    void givenNegativeXAndYFragmentIntersects_whenGetFragment_thenInstanceOfInputStreamResourceReturns() {
        assertInstanceOf(InputStreamResource.class, chartasService.getFragment(
                createMaxCharta(),
                -1,
                -1,
                2,
                2)
        );
    }

    @Test
    void givenNegativeXFragmentIntersects_whenGetFragment_thenInstanceOfInputStreamResourceReturns() {
        assertInstanceOf(InputStreamResource.class, chartasService.getFragment(
                createMaxCharta(),
                -1,
                0,
                2,
                2)
        );
    }

    @Test
    void givenNegativeYFragmentIntersects_whenGetFragment_thenInstanceOfInputStreamResourceReturns() {
        assertInstanceOf(InputStreamResource.class, chartasService.getFragment(
                createMaxCharta(),
                0,
                -1,
                2,
                2)
        );
    }

    @Test
    void givenPositiveXAndYFragmentIntersects_whenGetFragment_thenInstanceOfInputStreamResourceReturns() {
        assertInstanceOf(InputStreamResource.class, chartasService.getFragment(
                createMaxCharta(),
                getChartaMaxWidth() - 1,
                getChartaMaxHeight() - 1,
                2,
                2)
        );
    }

    @Test
    void givenPositiveXFragmentIntersects_whenGetFragment_thenInstanceOfInputStreamResourceReturns() {
        assertInstanceOf(InputStreamResource.class, chartasService.getFragment(
                createMaxCharta(),
                getChartaMaxWidth() - 1,
                0,
                2,
                2)
        );
    }

    @Test
    void givenPositiveYFragmentIntersects_whenPutFragment_whenGetFragment_thenInstanceOfInputStreamResourceReturns() {
        assertInstanceOf(InputStreamResource.class, chartasService.getFragment(
                createMaxCharta(),
                0,
                getChartaMaxHeight() - 1,
                2,
                2)
        );
    }

    @Test
    void givenNonZeroXAndYInBoundsFragment_whenPutFragment_thenInstanceOfInputStreamResourceReturns() {
        assertInstanceOf(InputStreamResource.class, chartasService.getFragment(
                createMaxCharta(),
                1,
                1,
                2,
                2)
        );
    }

    private String createMaxCharta() {
        return chartasService.createCharta(getChartaMaxWidth(), getChartaMaxHeight());
    }

    private int getChartaMaxWidth() {
        return imageProperties.getCharta().getMaxDimensions().getWidth();
    }

    private int getChartaMaxHeight() {
        return imageProperties.getCharta().getMaxDimensions().getHeight();
    }

    private int getFragmentMaxWidth() {
        return imageProperties.getFragment().getMaxDimensions().getWidth();
    }

    private int getFragmentMaxHeight() {
        return imageProperties.getFragment().getMaxDimensions().getHeight();
    }

    private void mockRead() {
        Mockito.when(ioService.read(Mockito.any())).thenReturn(
                new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB)
        );
    }

    private void mockReadCharta() {
        Mockito.when(ioService.readCharta(Mockito.anyString())).thenReturn(
                new BufferedImage(getChartaMaxWidth(),
                        getChartaMaxHeight(),
                        BufferedImage.TYPE_INT_RGB)
        );
    }

    private void mockWriteCharta() {
        Mockito.when(ioService.writeCharta(Mockito.any())).thenReturn(new ByteArrayOutputStream());
    }
}