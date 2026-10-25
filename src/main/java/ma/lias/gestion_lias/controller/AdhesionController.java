package ma.lias.gestion_lias.controller;

import ma.lias.gestion_lias.model.AdhesionRequest;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.repository.MemberRepository;
import ma.lias.gestion_lias.service.AdhesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AdhesionController {

    private final AdhesionService adhesionService;
    private final MemberRepository memberRepository;

    @GetMapping("/adhesion")
    public String form(Model model) {
        model.addAttribute("adhesionRequest", new AdhesionRequest());
        return "adhesion-form";
    }

    @PostMapping("/adhesion/save")
    public String save(@ModelAttribute AdhesionRequest adhesionRequest) {
        adhesionService.submit(adhesionRequest);
        return "redirect:/adhesion-merci";
    }

    @GetMapping("/adhesion-merci")
    public String merci() {
        return "adhesion-merci";
    }

    @GetMapping("/adhesion-requests")
    public String list(Model model) {
        model.addAttribute("requests", adhesionService.getAll());
        model.addAttribute("pendingCount", adhesionService.countPending());
        return "adhesion-requests";
    }

    @GetMapping("/adhesion-requests/accept/{id}")
    public String acceptForm(@PathVariable Long id, Model model) {
        model.addAttribute("request", adhesionService.getById(id));
        return "adhesion-accept-form";
    }

    @PostMapping("/adhesion-requests/accept/{id}")
    public String acceptSubmit(@PathVariable Long id,
                               @RequestParam LaboratoryMember.MemberStatus statut,
                               Authentication auth) {
        LaboratoryMember directeur = memberRepository.findByEmail(auth.getName()).orElseThrow();
        adhesionService.accept(id, directeur, statut);
        return "redirect:/adhesion-requests";
    }

    @PostMapping("/adhesion-requests/refuse/{id}")
    public String refuse(@PathVariable Long id, Authentication auth) {
        LaboratoryMember directeur = memberRepository.findByEmail(auth.getName()).orElseThrow();
        adhesionService.refuse(id, directeur);
        return "redirect:/adhesion-requests";
    }
}