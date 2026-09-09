package ru.otus.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.entity.enums.TechnologyLevel;
import ru.otus.services.UserTechnologyService;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class UserTechnologyRestController {

    private final UserTechnologyService userTechnologyService;

    @PatchMapping("/api/technologies/{id}/level")
    public ResponseEntity<Void> changeLevel(@PathVariable Long id,
                                            @RequestBody TechnologyLevel level,
                                            @CurrentUserParam CurrentUser currentUser) {
        userTechnologyService.changeLevel(currentUser.id(), id, level);
        return ok().build();
    }
}
