package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.model.Publication;
import ma.lias.gestion_lias.model.ScientificEvent;
import ma.lias.gestion_lias.service.MandateService;
import ma.lias.gestion_lias.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final MandateService mandateService;

    @GetMapping
    public String report(@RequestParam(required = false) Integer year, Model model) {
        int selected = (year != null) ? year : LocalDate.now().getYear();

        List<ScientificEvent> events = reportService.getEvents(selected);
        List<Publication> publications = reportService.getPublications(selected);
        List<LaboratoryMember> newMembers = reportService.getNewMembers(selected);

        model.addAttribute("year", selected);
        model.addAttribute("years", reportService.getAvailableYears());
        model.addAttribute("events", events);
        model.addAttribute("publications", publications);
        model.addAttribute("newMembers", newMembers);
        model.addAttribute("eventsByType", reportService.countEventsByType(events));
        model.addAttribute("pubsByType", reportService.countPublicationsByType(publications));
        model.addAttribute("membersByStatus", reportService.countMembersByStatus());
        model.addAttribute("totalMembers", reportService.countMembers());
        model.addAttribute("directeur", mandateService.getDirecteurActuelNom());
        model.addAttribute("generatedOn", LocalDate.now());
        return "rapport-annuel";
    }
}
