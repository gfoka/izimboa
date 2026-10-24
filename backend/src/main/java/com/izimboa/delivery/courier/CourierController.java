package com.izimboa.delivery.courier;

import com.izimboa.common.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Application livreur : passer en ligne / hors ligne, envoyer sa position. */
@RestController
@RequestMapping("/v1/delivery/courier")
@PreAuthorize("hasRole('livreur')")
class CourierController {

    private final CourierService couriers;

    CourierController(CourierService couriers) {
        this.couriers = couriers;
    }

    record PointRequest(
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double lat,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double lng,
            @PositiveOrZero Float accuracy,
            @PositiveOrZero Float speed,
            @DecimalMin("0") @DecimalMax("360") Float heading,
            @NotNull Instant recordedAt) {

        CourierService.Point toPoint() {
            return new CourierService.Point(lat, lng, accuracy, speed, heading, recordedAt);
        }
    }

    record OnlineRequest(@Valid PointRequest position) {}

    record PositionsRequest(@NotEmpty @Size(max = 200) List<@Valid @NotNull PointRequest> points) {}

    record PositionsResponse(int saved) {}

    @GetMapping("/status")
    CourierService.Status status(CurrentUser user) {
        return couriers.status(user.id());
    }

    @PostMapping("/online")
    CourierService.Status online(@Valid @RequestBody(required = false) OnlineRequest body, CurrentUser user) {
        return couriers.goOnline(user.id(), body == null || body.position() == null ? null : body.position().toPoint());
    }

    @PostMapping("/offline")
    CourierService.Status offline(CurrentUser user) {
        return couriers.goOffline(user.id());
    }

    @PostMapping("/positions")
    PositionsResponse positions(@Valid @RequestBody PositionsRequest body, CurrentUser user) {
        return new PositionsResponse(couriers.report(user.id(), body.points().stream().map(PointRequest::toPoint).toList()));
    }
}
