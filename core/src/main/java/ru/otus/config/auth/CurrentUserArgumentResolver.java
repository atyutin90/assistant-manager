package ru.otus.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.dto.UserDto;
import ru.otus.services.UserService;

import javax.annotation.Nonnull;

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserService userService;

    private final MessageSource messageSource;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserParam.class)
            && parameter.getParameterType().equals(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  @Nonnull NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException(
                messageSource.getMessage("error.user-not-authorized", new Object[]{}, getLocale())
            );
        }
        return userService.findByUsername(authentication.getName()).map(this::currentUserOf)
            .orElseThrow(() -> new UsernameNotFoundException(
                messageSource.getMessage("error.user-not-found", new Object[]{}, getLocale()))
            );
    }

    private CurrentUser currentUserOf(UserDto userDto) {
        return CurrentUser.builder()
            .id(userDto.id())
            .lastName(userDto.lastName())
            .middleName(userDto.middleName())
            .firstName(userDto.firstName())
            .username(userDto.username())
            .email(userDto.email())
            .projectRole(userDto.projectRole())
            .currentLevel(userDto.currentLevel())
            .laborCodePosition(userDto.laborCodePosition())
            .responsibleId(userDto.responsibleId())
            .userRoles(userDto.userRoles())
            .build();
    }
}
