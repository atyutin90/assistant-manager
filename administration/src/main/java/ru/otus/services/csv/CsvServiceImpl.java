package ru.otus.services.csv;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.otus.dto.CsvTechnologyDto;
import ru.otus.dto.CsvUserDto;
import ru.otus.dto.CsvQuestionDto;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.dto.filter.TechnologyFilter;
import ru.otus.dto.filter.UserFilter;
import ru.otus.entity.CareerLevel;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.Question;
import ru.otus.entity.Skill;
import ru.otus.entity.Technology;
import ru.otus.entity.User;
import ru.otus.entity.enums.UserRole;
import ru.otus.exceptions.UploadFileException;
import ru.otus.repositories.CareerLevelRepository;
import ru.otus.repositories.ProjectRoleRepository;
import ru.otus.repositories.QuestionRepository;
import ru.otus.repositories.SkillRepository;
import ru.otus.repositories.TechnologyRepository;
import ru.otus.repositories.UserRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.dto.filter.specification.TechnologySpecification.technologyFilterSpecification;
import static ru.otus.dto.filter.specification.UserSpecification.userFilterSpecification;
import static ru.otus.dto.filter.specification.QuestionSpecification.questionFilterSpecification;
import static ru.otus.entity.enums.UserRole.TEAM_LEAD;

@Service
@RequiredArgsConstructor
public class CsvServiceImpl implements CsvService {

    @Value("${app.default-password}")
    private String defaultPassword;

    private final QuestionRepository questionRepository;

    private final UserRepository userRepository;

    private final TechnologyRepository technologyRepository;

    private final ProjectRoleRepository projectRoleRepository;

    private final SkillRepository skillRepository;

    private final CareerLevelRepository careerLevelRepository;

    private final PasswordEncoder passwordEncoder;

    private final MessageSource messageSource;

    @Transactional
    @Override
    public void uploadQuestions(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try (var reader = bufferedReaderOf(file)) {
                List<CsvQuestionDto> csvQuestions =  new CsvToBeanBuilder<CsvQuestionDto>(reader)
                    .withType(CsvQuestionDto.class)
                    .withSeparator(';')
                    .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                    .build()
                    .parse();
                var uploadQuestions = csvQuestions.stream().map(this::questionOf).toList();
                var questions = questionRepository.findAll();
                modifyUploadQuestions(uploadQuestions, questions);
                questionRepository.saveAll(uploadQuestions);
            } catch (Exception e) {
                throw new UploadFileException(messageSource.getMessage("error.upload-data", null, getLocale()), e);
            }
        }
    }

    @Override
    public void uploadUsers(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try (var reader = bufferedReaderOf(file)
            ) {
                List<CsvUserDto> csvEmployees =  new CsvToBeanBuilder<CsvUserDto>(reader)
                    .withType(CsvUserDto.class)
                    .withSeparator(';')
                    .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                    .build()
                    .parse();
                var uploadUsers = csvEmployees.stream().map(this::userOf).toList();
                var users = userRepository.findAll();
                modifyUploadUsers(uploadUsers, users);
                userRepository.saveAll(uploadUsers);
            } catch (Exception e) {
                throw new UploadFileException(messageSource.getMessage("error.upload-data", null, getLocale()), e);
            }
        }
    }

    @Override
    public void uploadTechnologies(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try (var reader = bufferedReaderOf(file)
            ) {
                List<CsvTechnologyDto> csvTechnologies =  new CsvToBeanBuilder<CsvTechnologyDto>(reader)
                    .withType(CsvTechnologyDto.class)
                    .withSeparator(';')
                    .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                    .build()
                    .parse();
                var uploadTechnologies = csvTechnologies.stream().map(this::technologyOf).distinct().toList();
                var technologies = technologyRepository.findAll();
                modifyUploadTechnologies(uploadTechnologies, technologies);
                technologyRepository.saveAll(uploadTechnologies);
            } catch (Exception e) {
                throw new UploadFileException(messageSource.getMessage("error.upload-data", null, getLocale()), e);
            }
        }
    }

    @Override
    public List<CsvQuestionDto> downloadQuestions(QuestionFilter filter) {
        return questionRepository.findAll(questionFilterSpecification(filter), Sort.by("id")).stream()
            .map(this::csvQuestionOf).toList();
    }

    @Override
    public List<CsvUserDto> downloadUsers(UserFilter filter) {
        return userRepository.findAll(userFilterSpecification(filter), Sort.by("id")).stream()
            .map(this::csvEmployeeOf).toList();
    }

    @Override
    public List<CsvTechnologyDto> downloadTechnologies(TechnologyFilter filter) {
        return technologyRepository.findAll(technologyFilterSpecification(filter), Sort.by("name")).stream()
            .map(this::csvTechnologyOf).toList();
    }

    private static BufferedReader bufferedReaderOf(MultipartFile file) throws IOException {
        return new BufferedReader(
            new InputStreamReader(BOMInputStream.builder().setInputStream(file.getInputStream()).get(), UTF_8));
    }

    private static void modifyUploadQuestions(List<Question> uploadQuestions, List<Question> questions) {
        uploadQuestions.forEach(uq ->
            questions.stream().filter(q -> q.getUuid().equals(uq.getUuid()))
                .findFirst()
                .ifPresent(q -> uq.setId(q.getId()))
        );
    }

    private static void modifyUploadTechnologies(List<Technology> uploadTechnologies, List<Technology> technologies) {
        uploadTechnologies.forEach(ut ->
            technologies.stream().filter(t -> t.getName().equalsIgnoreCase(ut.getName()))
                .findFirst()
                .ifPresent(t -> ut.setId(t.getId()))
        );
    }

    private void modifyUploadUsers(List<User> uploadUsers, List<User> users) {
        uploadUsers.forEach(uu -> {
            users.stream()
                .filter(u -> u.getUsername().equals(uu.getUsername()))
                .findFirst()
                .ifPresent(u -> {
                    uu.setId(u.getId());
                    uu.setPassword(resolvePassword(u, uu));
                });
            if (isEmpty(uu.getPassword())) {
                uu.setPassword(passwordEncoder.encode(defaultPassword));
            }
        });
    }

    private String resolvePassword(User user, User uploadUser) {
        if (StringUtils.isNotEmpty(user.getPassword()) && StringUtils.isNotEmpty(uploadUser.getPassword())) {
            return passwordEncoder.encode(uploadUser.getPassword());
        } else if (StringUtils.isNotEmpty(user.getPassword()) && isEmpty(uploadUser.getPassword())) {
            return user.getPassword();
        } else if (isEmpty(user.getPassword()) && StringUtils.isNotEmpty(uploadUser.getPassword())) {
            return passwordEncoder.encode(uploadUser.getPassword());
        } else {
            return passwordEncoder.encode(defaultPassword);
        }
    }

    private CsvUserDto csvEmployeeOf(User user) {
        return CsvUserDto.builder()
            .username(user.getUsername())
            .lastName(user.getLastName())
            .middleName(user.getMiddleName())
            .firstName(user.getFirstName())
            .projectRoles(projectRoleCodesOf(user))
            .currentLevel(user.getCurrentLevel() != null ? user.getCurrentLevel().getCode() : null)
            .laborCodePosition(user.getLaborCodePosition())
            .userRoles(userRoleNamesOf(user))
            .email(user.getEmail())
            .responsibleUsername(user.getResponsible() != null ? user.getResponsible().getUsername() : null)
            .build();
    }

    private static Set<String> userRoleNamesOf(User user) {
        return isNotEmpty(user.getRoles()) ? user.getRoles().stream().map(Enum::name).collect(toSet()) : null;
    }

    private static Set<String> projectRoleCodesOf(User user) {
        return isNotEmpty(user.getProjectRoles()) ?
            user.getProjectRoles().stream()
                .map(ProjectRole::getCode)
                .collect(toSet()) :
            null;
    }

    private User userOf(CsvUserDto csvUserDto) {
        return User.builder()
            .username(csvUserDto.getUsername())
            .lastName(csvUserDto.getLastName())
            .middleName(csvUserDto.getMiddleName())
            .firstName(csvUserDto.getFirstName())
            .projectRoles(projectRolesOf(csvUserDto.getProjectRoles()))
            .currentLevel(careerLevelOf(csvUserDto.getCurrentLevel()))
            .laborCodePosition(csvUserDto.getLaborCodePosition())
            .email(csvUserDto.getEmail())
            .roles(csvUserDto.getUserRoles().stream()
                .map(UserRole::userRoleOf)
                .map(it -> it.orElse(null))
                .filter(Objects::nonNull)
                .collect(toSet()))
            .password(csvUserDto.getPassword())
            .responsible(responsibleOf(csvUserDto))
            .build();
    }

    private Technology technologyOf(CsvTechnologyDto csvTechnologyDto) {
        return Technology.builder()
            .enabled(true)
            .name(csvTechnologyDto.getName().trim())
            .build();
    }

    private CsvTechnologyDto csvTechnologyOf(Technology technology) {
        return CsvTechnologyDto.builder()
            .name(technology.getName())
            .build();
    }

    private User responsibleOf(CsvUserDto csvUserDto) {
        User responsible = null;
        if (StringUtils.isNotEmpty(csvUserDto.getResponsibleUsername())) {
            responsible = userRepository.findByRolesContainsAndUsername(TEAM_LEAD, csvUserDto.getResponsibleUsername())
                .orElse(null);
        }
        return responsible;
    }

    private Question questionOf(CsvQuestionDto csvQuestionDto) {
        return Question.builder()
            .enabled(csvQuestionDto.getEnabled())
            .uuid(csvQuestionDto.getUuid())
            .projectRole(projectRoleOf(csvQuestionDto.getProjectRole()))
            .skill(skillOf(csvQuestionDto.getSkill()))
            .areaKnowledge(csvQuestionDto.getAreaKnowledge())
            .section(csvQuestionDto.getSection())
            .text(csvQuestionDto.getText())
            .build();
    }

    private CsvQuestionDto csvQuestionOf(Question question) {
        return CsvQuestionDto.builder()
            .enabled(question.getEnabled())
            .uuid(question.getUuid())
            .projectRole(question.getProjectRole() != null ? question.getProjectRole().getCode() : null)
            .skill(question.getSkill() != null ? question.getSkill().getCode() : null)
            .areaKnowledge(question.getAreaKnowledge())
            .section(question.getSection())
            .text(question.getText())
            .build();
    }

    private ProjectRole projectRoleOf(String code) {
        return projectRoleRepository.findByCodeIgnoreCase(code).orElse(null);
    }

    private Set<ProjectRole> projectRolesOf(Set<String> codes) {
        return isNotEmpty(codes) ?
            codes.stream()
                .map(this::projectRoleOf)
                .filter(Objects::nonNull)
                .collect(toSet()) :
            Set.of();
    }

    private Skill skillOf(String code) {
        return skillRepository.findByCodeIgnoreCase(code).orElse(null);
    }

    private CareerLevel careerLevelOf(String code) {
        return careerLevelRepository.findByCodeIgnoreCase(code).orElse(null);
    }
}
