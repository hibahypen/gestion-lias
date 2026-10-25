package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.ScientificEvent;
import ma.lias.gestion_lias.repository.EventRepository;
import ma.lias.gestion_lias.service.EventService;
import ma.lias.gestion_lias.service.MemberService;
import ma.lias.gestion_lias.service.PublicationService;
import ma.lias.gestion_lias.service.TeamService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final MemberService memberService;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final PublicationService publicationService;
    private final TeamService teamService;

    @GetMapping("/")
    public String home(Model model) {
        LocalDate today = LocalDate.now();
        List<ScientificEvent> allEvents = eventRepository.findByArchivedFalse();

        List<ScientificEvent> upcomingEvents = allEvents.stream()
                .filter(e -> e.getEventDate() != null && !e.getEventDate().isBefore(today))
                .sorted(Comparator.comparing(ScientificEvent::getEventDate))
                .limit(4)
                .toList();

        List<ScientificEvent> pastEvents = allEvents.stream()
                .filter(e -> e.getEventDate() != null && e.getEventDate().isBefore(today))
                .sorted(Comparator.comparing(ScientificEvent::getEventDate).reversed())
                .limit(4)
                .toList();

        model.addAttribute("nbMembres", memberService.getAllMembers().size());
        model.addAttribute("nbEvents", allEvents.size());
        model.addAttribute("nbPublications", publicationService.getAllPublications().size());
        model.addAttribute("nbTeams", teamService.getAllTeams().size());

        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("teamService", teamService);

        model.addAttribute("coordNom", "Pr. Belaangour Abderrahim");
        model.addAttribute("coordRole", "Coordinateur du Master Data Science & Big Data");
        model.addAttribute("coordDept", "Faculté des Sciences Ben M'Sik — Département d'Informatique");
        model.addAttribute("coordEmail", "a.belaangour@univh2c.ma");
        model.addAttribute("coordTel", "+212 (0) 5 22 70 46 71");
        model.addAttribute("coordBureau", "Bloc D - Laboratoire LIAS, Bureau 12");

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/equipes")
    public String equipes(Model model) {
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("teamService", teamService);
        return "equipes";
    }

    @GetMapping("/activites")
    public String activites(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "activites";
    }

    @GetMapping("/publications-publiques")
    public String publicationsPubliques(Model model) {
        model.addAttribute("publications", publicationService.getAllPublications());
        return "publications-publiques";
    }
}