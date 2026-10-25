package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.model.ScientificEvent;
import ma.lias.gestion_lias.service.EventService;
import ma.lias.gestion_lias.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final MemberService memberService;

    @GetMapping
    public String list(Model model) {
        LocalDate today = LocalDate.now();
        List<ScientificEvent> all = eventService.getAllEvents();

        // 1. الأحداث القادمة (Prévus / À venir)
        List<ScientificEvent> upcoming = all.stream()
                .filter(e -> e.getEventDate() != null && !e.getEventDate().isBefore(today))
                .sorted(Comparator.comparing(ScientificEvent::getEventDate))
                .toList();

        // 2. الأحداث السابقة (Déjà passés / Archives)
        List<ScientificEvent> past = all.stream()
                .filter(e -> e.getEventDate() != null && e.getEventDate().isBefore(today))
                .sorted(Comparator.comparing(ScientificEvent::getEventDate).reversed())
                .toList();

        model.addAttribute("upcomingEvents", upcoming);
        model.addAttribute("pastEvents", past);
        model.addAttribute("totalEvents", all.size());

        return "events";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("event", new ScientificEvent());
        model.addAttribute("members", memberService.getActiveMembers());
        model.addAttribute("selectedIds", new HashSet<Long>());
        return "event-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ScientificEvent event,
                       @RequestParam(required = false) List<Long> organizerIds) {
        eventService.save(event, organizerIds);
        return "redirect:/events";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        ScientificEvent event = eventService.getById(id);
        Set<Long> selected = event.getOrganizers().stream()
                .map(LaboratoryMember::getId)
                .collect(Collectors.toSet());
        model.addAttribute("event", event);
        model.addAttribute("members", memberService.getActiveMembers());
        model.addAttribute("selectedIds", selected);
        return "event-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        eventService.delete(id);
        return "redirect:/events";
    }
}