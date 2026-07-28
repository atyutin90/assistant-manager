package ru.otus.controllers.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.exceptions.WebApplicationException;

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@ControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(DataNotFoundException.class)
    public ModelAndView handeDataNotFoundException(DataNotFoundException e) {
        ModelAndView modelAndView = new ModelAndView("page/error/error");
        modelAndView.addObject("message", e.getMessage());
        modelAndView.addObject("status", NOT_FOUND.value());
        return modelAndView;
    }

    @ExceptionHandler(WebApplicationException.class)
    public ModelAndView handleWebApplicationException(WebApplicationException exception) {
        var modelAndView = new ModelAndView("page/error/error");
        modelAndView.addObject("message", exception.getMessage());
        modelAndView.addObject("status", exception.getStatus().value());
        modelAndView.setStatus(exception.getStatus());
        return modelAndView;
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException() {
        var modelAndView = new ModelAndView("page/error/error");
        modelAndView.addObject("message",
            messageSource.getMessage("error.insufficient-permissions-for-page", new Object[]{}, getLocale())
        );
        modelAndView.addObject("status", FORBIDDEN.value());
        return modelAndView;
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException() {
        var modelAndView = new ModelAndView("page/error/error");
        modelAndView.addObject("message",
            messageSource.getMessage("error.internal-error", new Object[]{}, getLocale())
        );
        modelAndView.addObject("status", INTERNAL_SERVER_ERROR.value());
        return modelAndView;
    }
}
