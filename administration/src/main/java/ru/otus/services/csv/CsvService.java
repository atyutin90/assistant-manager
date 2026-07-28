package ru.otus.services.csv;

import org.springframework.web.multipart.MultipartFile;
import ru.otus.dto.CsvUserDto;
import ru.otus.dto.CsvQuestionDto;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.dto.filter.UserFilter;

import java.util.List;

public interface CsvService {

    void uploadQuestions(MultipartFile file);

    void uploadUsers(MultipartFile file);

    List<CsvQuestionDto> downloadQuestions(QuestionFilter filter);

    List<CsvUserDto> downloadUsers(UserFilter filter);
}
