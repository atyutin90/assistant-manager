package ru.otus.services.csv;

import org.springframework.web.multipart.MultipartFile;
import ru.otus.dto.CsvProjectQuestionDto;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.exceptions.UploadFileException;
import ru.otus.exceptions.WebApplicationException;

import java.util.List;

public interface ProjectQuestionCsvService {

    /**
     * Выгружает вопросы проекта в виде CSV DTO для указанного пользователя.
     *
     * @param projectId идентификатор проекта
     * @param userId    идентификатор пользователя, запрашивающего выгрузку
     * @param filter    критерии фильтрации вопросов
     * @return список DTO вопросов проекта для CSV
     * @throws DataNotFoundException данные на найдены
     */
    List<CsvProjectQuestionDto> download(Long projectId, Long userId, QuestionFilter filter);

    /**
     * Загружает вопросы проекта из CSV-файла.
     * @param projectId идентификатор проекта
     * @param userId    идентификатор пользователя, выполняющего загрузку
     * @param file      CSV-файл с данными вопросов (разделитель — точка с запятой)
     * @throws WebApplicationException если произошла ошибка при обработке полученных данных
     * @throws UploadFileException если произошла ошибка при чтении или парсинге файла
     */
    void upload(Long projectId, Long userId, MultipartFile file);
}
