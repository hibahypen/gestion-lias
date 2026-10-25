package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.ScientificEvent;
import ma.lias.gestion_lias.repository.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final EventRepository eventRepository;

    @GetMapping("/calendrier")
    public String calendar(@RequestParam(required = false) Integer year, Model model) {
        int selectedYear = (year != null) ? year : LocalDate.now().getYear();

        List<ScientificEvent> events = eventRepository.findAll().stream()
                .filter(e -> e.getEventDate() != null && e.getEventDate().getYear() == selectedYear)
                .sorted(Comparator.comparing(ScientificEvent::getEventDate))
                .toList();

        Map<Integer, List<ScientificEvent>> eventsByMonth = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            final int month = m;
            List<ScientificEvent> monthEvents = events.stream()
                    .filter(e -> e.getEventDate().getMonthValue() == month)
                    .toList();
            eventsByMonth.put(month, monthEvents);
        }

        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("eventsByMonth", eventsByMonth);
        model.addAttribute("months", Month.values());
        model.addAttribute("totalYearEvents", events.size());

        return "calendrier";
    }
}