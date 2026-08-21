package ru.otus.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import ru.otus.dto.UserDto;
import ru.otus.dto.filter.UserFilter;
import ru.otus.services.CareerLevelService;
import ru.otus.services.JwtService;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.UserService;
import ru.otus.services.ValueListService;
import ru.otus.services.csv.CsvService;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.CAREER_LEVELS;
import static ru.otus.controllers.pages.AbstractPageController.FILTER;
import static ru.otus.controllers.pages.AbstractPageController.IS_EDIT;
import static ru.otus.controllers.pages.AbstractPageController.PASSWORD_CHANGE;
import static ru.otus.controllers.pages.AbstractPageController.PROJECT_ROLES;
import static ru.otus.controllers.pages.AbstractPageController.SUCCESS_CHANGE_PASSWORD;
import static ru.otus.controllers.pages.AbstractPageController.TEAM_LEADS;
import static ru.otus.controllers.pages.AbstractPageController.USER;
import static ru.otus.controllers.pages.AbstractPageController.USERS;
import static ru.otus.controllers.pages.AbstractPageController.USER_ROLES;

@DisplayName("Контроллер для пользователей ")
@WebMvcTest(UserPageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CareerLevelService careerLevelService;

    @MockitoBean
    private ProjectRoleService projectRoleService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ValueListService valueListService;

    @MockitoBean
    private CsvService csvService;

    @DisplayName("страница пользователей должна отображаться с фильтром и справочниками")
    @Test
    void shouldRenderUserList() throws Exception {
        var page = new PageImpl<UserDto>(List.of());
        when(userService.findAll(any(UserFilter.class), any())).thenReturn(page);

        mvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/user/list"))
            .andExpect(model().attribute(USERS, page))
            .andExpect(model().attributeExists(FILTER, PROJECT_ROLES, CAREER_LEVELS, USER_ROLES));
    }

    @DisplayName("форма новой пользователя должна отображаться с пустым DTO")
    @Test
    void shouldRenderNewCareerLevelPage() throws Exception {
        mvc.perform(get("/users/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/user/form"))
            .andExpect(model().attribute(IS_EDIT, false))
            .andExpect(model().attributeExists(USER, PROJECT_ROLES, CAREER_LEVELS, USER_ROLES));
    }

    @DisplayName("форма пользователя должна отображаться с данными смены пароля")
    @Test
    void shouldRenderUserEditPage() throws Exception {
        var user = user(1L);
        when(userService.findById(1L)).thenReturn(user);

        mvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/user/form"))
            .andExpect(model().attribute(USER, user))
            .andExpect(model().attribute(IS_EDIT, true))
            .andExpect(model().attributeExists(PASSWORD_CHANGE, TEAM_LEADS, CAREER_LEVELS, USER_ROLES));
    }

    @DisplayName("пользователь должен создаваться")
    @Test
    void shouldCreateUserAndRedirect() throws Exception {
        when(userService.create(any(UserDto.class))).thenReturn(user(15L));

        mvc.perform(post("/users")
                .param("lastName", "Иванов")
                .param("middleName", "Иванович")
                .param("firstName", "Иван")
                .param("username", "ivan")
                .param("email", "ivan@example.com")
                .param("userRoles", "USER"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/users/15"));
        verify(userService).create(any(UserDto.class));
    }

    @DisplayName("изменяемый пользователь должен создаваться")
    @Test
    void shouldChangeUserAndRedirect() throws Exception {
        when(userService.update(any(UserDto.class))).thenReturn(user(1L));

        mvc.perform(post("/users")
                .param("id", "1")
                .param("lastName", "Иванов")
                .param("middleName", "Иванович")
                .param("firstName", "Иван")
                .param("username", "ivan")
                .param("email", "ivan@example.com")
                .param("userRoles", "USER"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/users/1"));
        verify(userService).update(any(UserDto.class));
    }

    @DisplayName("пароль пользователя должен изменяться")
    @Test
    void shouldChangeUserPassword() throws Exception {
        mvc.perform(post("/users/1")
                .param("newPassword", "newpass")
                .param("passwordConfirmation", "newpass"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/users/1"))
            .andExpect(flash().attribute(SUCCESS_CHANGE_PASSWORD, true));
        verify(userService).changePassword(1L, "newpass");
    }

    @DisplayName("пользователь должен удаляться")
    @Test
    void shouldDeleteUser() throws Exception {
        mvc.perform(delete("/users/3/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/users"));
        verify(userService).deleteById(3L);
    }

    @DisplayName("CSV с пользователями должен загружаться")
    @Test
    void shouldUploadUsers() throws Exception {
        mvc.perform(multipart("/users/upload")
                .file("file", "username;last-name".getBytes()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/users"));
        verify(csvService).uploadUsers(any(MultipartFile.class));
    }

    @DisplayName("CSV с пользователями должен скачиваться")
    @Test
    void shouldDownloadQuestions() throws Exception {
        when(csvService.downloadUsers(any(UserFilter.class))).thenReturn(List.of());

        mvc.perform(get("/users/download"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"users.csv\""))
            .andExpect(content().contentType("text/csv; charset=UTF-8"));
    }

    private UserDto user(Long id) {
        return UserDto.builder()
            .id(id)
            .lastName("Иванов")
            .middleName("Иванович")
            .firstName("Иван")
            .username("ivan")
            .email("ivan@example.com")
            .userRoles(Set.of("USER"))
            .build();
    }
}
