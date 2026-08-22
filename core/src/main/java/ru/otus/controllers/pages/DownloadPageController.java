package ru.otus.controllers.pages;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static com.opencsv.ICSVWriter.DEFAULT_QUOTE_CHARACTER;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public interface DownloadPageController extends AbstractPageController {

    char SEMICOLON = ';';

    default <T> void download(HttpServletResponse response, List<T> values,
                                   Class<T> classType, String filename
    ) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(filename));
        // BOM для корректного отображения русских букв в Excel, этапе чтения файла убираем его через BOMInputStream
        response.getWriter().write("\uFEFF");

        if (isEmpty(values)) {
            var mappingStrategy = new HeaderColumnNameMappingStrategy<T>();
            mappingStrategy.setType(classType);

            try (var write = new CSVWriterBuilder(response.getWriter())
                .withSeparator(SEMICOLON)
                .withQuoteChar(DEFAULT_QUOTE_CHARACTER)
                .build()) {
                write.writeNext(mappingStrategy.generateHeader(null));
            }
            return;
        }

        var writer = new StatefulBeanToCsvBuilder<T>(response.getWriter())
            .withSeparator(SEMICOLON)
            .withQuotechar(DEFAULT_QUOTE_CHARACTER)
            .build();
        writer.write(values);
    }
}
