package ru.otus.controllers.pages;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

import java.io.Writer;
import java.util.List;

import static com.opencsv.ICSVWriter.DEFAULT_QUOTE_CHARACTER;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public interface DownloadPageController extends AbstractPageController {

    char SEMICOLON = ';';

    /**
     * Единый формат выгрузки: UTF-8 c BOM (EF BB BF) и разделитель ';'.
     * <p>
     * BOM — единственный вариант, который одинаково правильно открывается:
     * <ul>
     *   <li>Excel на Windows (в т.ч. русская локаль) — определяет UTF-8 по BOM;</li>
     *   <li>Excel на macOS — определяет UTF-8 по BOM;</li>
     *   <li>LibreOffice / Numbers / любой редактор на Unix — UTF-8 по умолчанию;</li>
     *   <li>повторная загрузка файла обратно — BOM отрезается через BOMInputStream.</li>
     * </ul>
     * Windows-1251 писать нельзя: на Unix/macOS и в Excel с UTF-8 по умолчанию он даёт битые символы.
     */
    default <T> void download(HttpServletResponse response, List<T> values,
                              Class<T> classType, String filename) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(filename));

        // BOM для корректного отображения русских букв в Excel, этапе чтения файла убираем его через BOMInputStream
        Writer writer = response.getWriter();
        writer.write('\uFEFF');

        if (isEmpty(values)) {
            var mappingStrategy = new HeaderColumnNameMappingStrategy<T>();
            mappingStrategy.setType(classType);
            try (var csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(SEMICOLON)
                .withQuoteChar(DEFAULT_QUOTE_CHARACTER)
                .build()) {
                csvWriter.writeNext(mappingStrategy.generateHeader(null));
            }
            return;
        }

        var beanToCsv = new StatefulBeanToCsvBuilder<T>(writer)
            .withSeparator(SEMICOLON)
            .withQuotechar(DEFAULT_QUOTE_CHARACTER)
            .build();
        beanToCsv.write(values);
        writer.flush();
    }
}
