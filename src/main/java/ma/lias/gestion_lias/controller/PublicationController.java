package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.Publication;
import ma.lias.gestion_lias.service.MemberService;
import ma.lias.gestion_lias.service.PublicationService;
import ma.lias.gestion_lias.service.TeamService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;
    private final MemberService memberService;
    private final TeamService teamService;

    @GetMapping
    public String list(@RequestParam(required = false) Long authorId,
                       @RequestParam(required = false) Integer year,
                       @RequestParam(required = false) String team,
                       Model model) {

        List<Publication> pubs = publicationService.getAllPublications().stream()
                .filter(p -> authorId == null || (p.getAddedBy() != null && p.getAddedBy().getId().equals(authorId)))
                .filter(p -> year == null || (p.getPubYear() != null && p.getPubYear().equals(year)))
                .filter(p -> team == null || team.isBlank() || (p.getAddedBy() != null && team.equals(p.getAddedBy().getTeam())))
                .toList();

        model.addAttribute("publications", pubs);
        model.addAttribute("authors", memberService.getAllMembers());
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("selectedAuthor", authorId);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedTeam", team);

        return "publications";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("publication", new Publication());
        return "publication-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Publication publication, Authentication auth) {
        if (publication.getId() != null) {
            Publication existing = publicationService.getById(publication.getId());
            boolean owner = existing.getAddedBy() != null && existing.getAddedBy().getEmail().equalsIgnoreCase(auth.getName());
            boolean governance = auth.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DIRECTEUR"));
            if (!owner && !governance) return "redirect:/publications?unauthorized";
        }
        publicationService.save(publication, auth.getName());
        return "redirect:/publications";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, Authentication auth) {
        Publication pub = publicationService.getById(id);
        boolean isOwner = pub.getAddedBy() != null && pub.getAddedBy().getEmail().equalsIgnoreCase(auth.getName());
        boolean isGov = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DIRECTEUR"));

        // حماية: إذا ما كانش هو مول المنشور أو مدير/أدمين ما عندوش الحق
        if (!isOwner && !isGov) {
            return "redirect:/publications?unauthorized";
        }

        model.addAttribute("publication", pub);
        return "publication-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Authentication auth) {
        Publication pub = publicationService.getById(id);
        boolean isOwner = pub.getAddedBy() != null && pub.getAddedBy().getEmail().equalsIgnoreCase(auth.getName());
        boolean isGov = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DIRECTEUR"));

        // حماية: لا يمكن مسح منشورات الآخرين
        if (!isOwner && !isGov) {
            return "redirect:/publications?unauthorized";
        }

        publicationService.delete(id);
        return "redirect:/publications";
    }
}