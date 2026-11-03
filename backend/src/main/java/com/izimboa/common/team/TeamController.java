package com.izimboa.common.team;

import com.izimboa.common.CurrentUser;
import com.izimboa.common.IdentityAdmin;
import com.izimboa.common.Roles;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Équipe Izimboa gérée depuis Izimboa, sans la console Keycloak (décision 74) : l'administrateur donne ou retire
 * les rôles administrateur, validateur et opérateur à un compte existant (retrouvé par numéro, e-mail ou identifiant).
 */
@RestController
@RequestMapping("/v1/admin/team")
@PreAuthorize("hasRole('admin')")
class TeamController {

    static final List<String> TEAM_ROLES = List.of(Roles.ADMIN, Roles.VALIDATEUR, Roles.OPERATEUR);

    private final IdentityAdmin identity;

    TeamController(IdentityAdmin identity) {
        this.identity = identity;
    }

    record Member(UUID id, String name, String login, String phone, String email, Set<String> roles) {}

    record AddRequest(String login, String role) {}

    @GetMapping
    List<Member> list() {
        Map<UUID, Member> members = new LinkedHashMap<>();
        for (String role : TEAM_ROLES) {
            for (var a : call(() -> identity.withRole(role))) {
                var m = members.computeIfAbsent(a.id(), id -> new Member(id, name(a), a.username(), a.phone(), a.email(), new TreeSet<>()));
                m.roles().add(role);
            }
        }
        return new ArrayList<>(members.values());
    }

    @PostMapping
    List<Member> add(@RequestBody AddRequest body) {
        String role = checkRole(body.role());
        var account = call(() -> identity.findByLogin(body.login())).orElseThrow(() -> problem(HttpStatus.NOT_FOUND,
                "Aucun compte avec ce numéro ou cet e-mail. La personne doit d'abord se connecter une fois à Izimboa."));
        call(() -> { identity.addRole(account.id(), role); return null; });
        return list();
    }

    @DeleteMapping("/{userId}/roles/{role}")
    List<Member> remove(@PathVariable UUID userId, @PathVariable String role, CurrentUser me) {
        checkRole(role);
        if (Roles.ADMIN.equals(role) && userId.equals(me.id())) {
            throw problem(HttpStatus.CONFLICT, "Vous ne pouvez pas retirer votre propre rôle d'administrateur.");
        }
        call(() -> { identity.removeRole(userId, role); return null; });
        return list();
    }

    private static String checkRole(String role) {
        if (role == null || !TEAM_ROLES.contains(role)) throw problem(HttpStatus.UNPROCESSABLE_CONTENT, "Rôle inconnu.");
        return role;
    }

    private static String name(IdentityAdmin.Account a) {
        String n = ((a.firstName() == null ? "" : a.firstName()) + " " + (a.lastName() == null ? "" : a.lastName())).strip();
        return n.isEmpty() ? null : n;
    }

    private static <T> T call(java.util.function.Supplier<T> s) {
        try {
            return s.get();
        } catch (IdentityAdmin.IdentityException e) {
            throw problem(HttpStatus.valueOf(e.status()), e.getMessage());
        } catch (ErrorResponseException e) {
            throw e;
        } catch (RuntimeException e) {
            throw problem(HttpStatus.SERVICE_UNAVAILABLE, "Gestion des comptes momentanément indisponible.");
        }
    }

    private static ErrorResponseException problem(HttpStatus status, String detail) {
        var p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setProperty("code", "team");
        return new ErrorResponseException(status, p, null);
    }
}
