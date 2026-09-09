package ru.otus.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.services.JwtService;
import ru.otus.services.UserService;
import ru.otus.services.ai.ManagerAiSettingService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Контроллер страницы AI-поиска сотрудников")
@WebMvcTest(ManagerEmployeeSearchPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "manager")
class ManagerEmployeeSearchPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ManagerAiSettingService managerAiSettingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("страница должна отображаться при наличии AI-настроек")
    void shouldRenderPageWhenAiSettingExists() throws Exception {
        when(managerAiSettingService.hasSetting("manager")).thenReturn(true);

        mvc.perform(get("/employees/search").principal(() -> "manager"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/employee/search"));
    }

    @Test
    @DisplayName("страница должна перенаправлять в настройки, если AI-настроек нет")
    void shouldRedirectToSettingsWhenAiSettingDoesNotExist() throws Exception {
        when(managerAiSettingService.hasSetting("manager")).thenReturn(false);

        mvc.perform(get("/employees/search").principal(() -> "manager"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/settings/ai"));
    }
}
