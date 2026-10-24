package com.izimboa.delivery.conditions;

import com.izimboa.common.CurrentUser;
import com.izimboa.delivery.geo.GeoPoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Bouton de l'application livreur : « il pleut ici », « gros bouchon », « ça roule ». */
@RestController
@RequestMapping("/v1/delivery/conditions")
class RoadConditionController {

    private final RoadConditionRepository repository;
    private final Clock clock;

    RoadConditionController(RoadConditionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    record ReportRequest(@NotNull RoadConditionReport.Kind kind, @NotNull @Valid GeoPoint position) {}

    record ReportView(long id, Instant reportedAt) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('livreur')")
    ReportView report(@Valid @RequestBody ReportRequest body, CurrentUser user) {
        var saved = repository.save(new RoadConditionReport(user.id(), body.kind(),
                body.position().lat(), body.position().lng(), clock.instant()));
        return new ReportView(saved.id(), saved.reportedAt());
    }
}
