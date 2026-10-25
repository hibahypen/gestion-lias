package ma.lias.gestion_lias.controller;

import ma.lias.gestion_lias.service.AdhesionService;
import ma.lias.gestion_lias.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AdhesionService adhesionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("permanents", dashboardService.countPermanentMembers());
        model.addAttribute("doctorants", dashboardService.countDoctorants());
        model.addAttribute("events", dashboardService.countEvents());
        model.addAttribute("publications", dashboardService.countPublications());
        model.addAttribute("directeur", dashboardService.getDirecteurActuel());

        boolean isAdmin = hasRole(auth, "ROLE_ADMIN");
        boolean isDirecteur = hasRole(auth, "ROLE_DIRECTEUR");
        boolean isViceDirecteur = hasRole(auth, "ROLE_VICE_DIRECTEUR");

        if (isAdmin || isDirecteur || isViceDirecteur) {
            model.addAttribute("pendingAdhesions", adhesionService.countPending());
            model.addAttribute("membersByStatus", dashboardService.getMembersByStatus());
            model.addAttribute("eventsByType", dashboardService.getEventsByType());
            model.addAttribute("publicationsByYear", dashboardService.getPublicationsByYear());
        }

        if (isAdmin) {
            return "dashboard-admin";
        } else if (isDirecteur || isViceDirecteur) {
            return "dashboard-directeur";
        } else {
            return "dashboard-membre";
        }
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}