package ru.otus.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@UtilityClass
public class FileUtils {

    public static String resourceAsString(String fileName) {
        String result = null;
        try (InputStream inputStream = FileUtils.class.getResourceAsStream(fileName)) {
            if (inputStream != null) {
                result = StreamUtils.copyToString(inputStream, UTF_8);
            }
        } catch (IOException e) {
            log.error("Error reading file {}", fileName, e);
        }
        return result;
    }
}
