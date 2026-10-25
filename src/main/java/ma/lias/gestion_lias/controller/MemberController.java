package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.service.HistoryService;
import ma.lias.gestion_lias.service.MemberService;
import ma.lias.gestion_lias.service.PublicationService;
import ma.lias.gestion_lias.service.TeamService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final HistoryService historyService;
    private final PublicationService publicationService;
    private final TeamService teamService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", memberService.getAllMembers());
        return "members";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        LaboratoryMember m = new LaboratoryMember();
        m.setStatus(LaboratoryMember.MemberStatus.PERMANENT);
        model.addAttribute("member", m);
        model.addAttribute("teams", teamService.getAllTeams());
        return "member-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute LaboratoryMember formMember,
                       @RequestParam(value = "password", required = false) String rawPassword) {
        if (formMember.getId() != null) {
            memberService.updateByAdmin(formMember, rawPassword);
        } else {
            memberService.save(formMember, rawPassword);
        }
        return "redirect:/members";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.getById(id));
        model.addAttribute("teams", teamService.getAllTeams());
        return "member-form";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model, Authentication auth) {
        LaboratoryMember m = memberService.getById(id);
        boolean self = m.getEmail().equals(auth.getName());
        boolean governance = auth.getAuthorities().stream().anyMatch(a ->
                Set.of("ROLE_ADMIN", "ROLE_DIRECTEUR", "ROLE_VICE_DIRECTEUR").contains(a.getAuthority()));
        boolean canSeeHistory = self || governance;

        model.addAttribute("member", m);
        model.addAttribute("affiliations", historyService.getAffiliations(id));
        model.addAttribute("publications", publicationService.getByAuthor(id));
        model.addAttribute("canSeeHistory", canSeeHistory);
        if (canSeeHistory) {
            model.addAttribute("history", historyService.getHistory(id));
        }
        return "member-view";
    }

    @PostMapping("/delete/{id}")
    public String archive(@PathVariable Long id) {
        memberService.archive(id);
        return "redirect:/members";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id) {
        memberService.toggleActive(id);
        return "redirect:/members";
    }
}