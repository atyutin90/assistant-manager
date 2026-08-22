package ru.otus.services;

import org.reflections.Reflections;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.otus.annotations.ValueList;
import ru.otus.dto.CodeAndValue;
import ru.otus.entity.enums.OrderedEnum;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class ValueListServiceImpl implements ValueListService {

    private static final String PACKAGE = "ru.otus.entity.enums";

    private final MessageSource messageSource;

    private final Map<String, Class<? extends Enum<?>>> classMap;

    public ValueListServiceImpl(
        MessageSource messageSource) {
        this.messageSource = messageSource;
        this.classMap = getAnnotatedEnumsMap(PACKAGE);
    }

    public List<CodeAndValue> getValues(String type) {
        List<CodeAndValue> values = List.of();
        if (classMap.containsKey(type)) {
            values = getFromMessageSource(classMap.get(type));
        }
        return values;
    }

    private List<CodeAndValue> getFromMessageSource(Class<? extends Enum<?>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
            .filter(OrderedEnum.class::isInstance)
            .map(OrderedEnum.class::cast)
            .sorted(Comparator.comparingInt(OrderedEnum::getOrder))
            .map(it -> new CodeAndValue(((Enum<?>)it).name(), getHeader((Enum<?>) it)))
            .filter(it -> it.value() != null)
            .collect(toList());
    }

    private String getHeader(Enum<?> enumValue) {
        String resourceId = enumValue.getClass().getSimpleName() + "." + enumValue.name();
        return messageSource.getMessage(resourceId, null, LocaleContextHolder.getLocale());
    }

    private Map<String, Class<? extends Enum<?>>> getAnnotatedEnumsMap(String packageName) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getTypesAnnotatedWith(ValueList.class).stream()
            .filter(Class::isEnum)
            .map(aClass -> (Class<? extends Enum<?>>) aClass)
            .collect(
                toMap(
                    aClass -> aClass.getAnnotation(ValueList.class).type(),
                    aClass -> aClass)
            );
    }
}
