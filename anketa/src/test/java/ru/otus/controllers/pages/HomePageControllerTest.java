package ru.otus.controllers.pages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import ru.otus.dto.ProfileDto;
import ru.otus.dto.UserDto;
import ru.otus.services.JwtService;
import ru.otus.services.ProfileService;
import ru.otus.services.StaffEvaluationUserService;
import ru.otus.services.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.PASSWORD_CHANGE;
import static ru.otus.controllers.pages.AbstractPageController.SHOW_PASSWORD_MODAL;
import static ru.otus.controllers.pages.AbstractPageController.STAFF_EVALUATIONS;
import static ru.otus.controllers.pages.AbstractPageController.SUCCESS_CHANGE_PASSWORD;
import static ru.otus.controllers.pages.AbstractPageController.USER;
import static ru.otus.controllers.pages.HomePageControllerTest.EMPLOYEE;

@DisplayName("Контроллер домашней страницы анкеты")
@WebMvcTest(HomePageController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = EMPLOYEE)
class HomePageControllerTest {

    static final String EMPLOYEE = "employee";

    private static final String SECRET = "secret";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private StaffEvaluationUserService staffEvaluationUserService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUpCurrentUser() {
        when(userService.findByUsername(EMPLOYEE)).thenReturn(Optional.of(currentUser()));
        when(profileService.getProfile(7L))
            .thenReturn(ProfileDto.builder().id(7L).username(EMPLOYEE).build());
        when(staffEvaluationUserService.findActive(7L)).thenReturn(List.of());
    }

    @Test
    @DisplayName("главная страница должна содержать профиль и активные оценки")
    void shouldRenderHomePage() throws Exception {
        var profile = ProfileDto.builder().id(7L).username(EMPLOYEE).build();
        when(profileService.getProfile(7L)).thenReturn(profile);
        mvc.perform(get("/").with(user(EMPLOYEE)))
            .andExpect(status().isOk())
            .andExpect(view().name("page/home/edit"))
            .andExpect(model().attribute("profile", profile))
            .andExpect(model().attribute(STAFF_EVALUATIONS, List.of()))
            .andExpect(model().attributeExists(PASSWORD_CHANGE))
            .andExpect(model().attribute(SHOW_PASSWORD_MODAL, false));
    }

    @Test
    @DisplayName("валидный пароль должен изменяться")
    void shouldChangePassword() throws Exception {
        mvc.perform(post("/")
                .with(user(EMPLOYEE))
                .param("newPassword", SECRET)
                .param("passwordConfirmation", SECRET))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute(SUCCESS_CHANGE_PASSWORD, true));

        verify(profileService).changePassword(7L, SECRET);
    }

    @Test
    @DisplayName("невалидный пароль должен возвращать форму с открытым окном")
    void shouldRejectInvalidPassword() throws Exception {
        mvc.perform(post("/")
                .with(user(EMPLOYEE))
                .param("newPassword", "x")
                .param("passwordConfirmation", "different"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/home/edit"))
            .andExpect(model().attributeHasErrors(PASSWORD_CHANGE))
            .andExpect(model().attribute(SHOW_PASSWORD_MODAL, true));

        verify(profileService, never()).changePassword(7L, "x");
    }

    private UserDto currentUser() {
        return UserDto.builder()
            .id(7L)
            .username(EMPLOYEE)
            .userRoles(Set.of(USER))
            .build();
    }
}
