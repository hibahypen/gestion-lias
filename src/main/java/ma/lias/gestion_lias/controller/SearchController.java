package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.DocumentArchive;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.model.Publication;
import ma.lias.gestion_lias.model.ScientificEvent;
import ma.lias.gestion_lias.repository.DocumentArchiveRepository;
import ma.lias.gestion_lias.repository.EventRepository;
import ma.lias.gestion_lias.repository.MemberRepository;
import ma.lias.gestion_lias.repository.PublicationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final MemberRepository memberRepository;
    private final DocumentArchiveRepository documentRepository;
    private final EventRepository eventRepository;
    private final PublicationRepository publicationRepository;

    @GetMapping("/search")
    public String search(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query == null || query.trim().isBlank()) {
            model.addAttribute("query", "");
            model.addAttribute("totalResults", 0);
            return "search-results";
        }

        String q = query.trim().toLowerCase();

        List<LaboratoryMember> members = memberRepository.findAll().stream()
                .filter(m -> (m.getFirstName() != null && m.getFirstName().toLowerCase().contains(q))
                        || (m.getLastName() != null && m.getLastName().toLowerCase().contains(q))
                        || (m.getEmail() != null && m.getEmail().toLowerCase().contains(q))
                        || (m.getSpecialite() != null && m.getSpecialite().toLowerCase().contains(q))
                        || (m.getTeam() != null && m.getTeam().toLowerCase().contains(q)))
                .toList();

        List<ScientificEvent> events = eventRepository.findAll().stream()
                .filter(e -> (e.getTitle() != null && e.getTitle().toLowerCase().contains(q))
                        || (e.getDescription() != null && e.getDescription().toLowerCase().contains(q)))
                .toList();

        List<Publication> publications = publicationRepository.findAll().stream()
                .filter(p -> (p.getTitle() != null && p.getTitle().toLowerCase().contains(q))
                        || (p.getJournal() != null && p.getJournal().toLowerCase().contains(q))
                        || (p.getPubType() != null && p.getPubType().toLowerCase().contains(q)))
                .toList();

        List<DocumentArchive> documents = documentRepository.findAll().stream()
                .filter(d -> (d.getTitle() != null && d.getTitle().toLowerCase().contains(q))
                        || (d.getFileName() != null && d.getFileName().toLowerCase().contains(q)))
                .toList();

        int total = members.size() + events.size() + publications.size() + documents.size();

        model.addAttribute("query", query);
        model.addAttribute("members", members);
        model.addAttribute("events", events);
        model.addAttribute("publications", publications);
        model.addAttribute("documents", documents);
        model.addAttribute("totalResults", total);

        return "search-results";
    }
}