package ma.lias.gestion_lias.controller;

import ma.lias.gestion_lias.model.Role;
import ma.lias.gestion_lias.repository.MemberRepository;
import ma.lias.gestion_lias.service.MandateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mandates")
@RequiredArgsConstructor
public class MandateController {

    private final MandateService mandateService;
    private final MemberRepository memberRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("mandates", mandateService.getActiveMandates());
        model.addAttribute("members", memberRepository.findAll());
        model.addAttribute("roles", Role.values());
        return "mandates";
    }

    @PostMapping("/assign")
    public String assign(@RequestParam Long memberId,
                         @RequestParam Role role,
                         @RequestParam(required = false) String team) {
        mandateService.assignRole(memberId, role, team);
        return "redirect:/mandates";
    }

    @PostMapping("/end/{id}")
    public String end(@PathVariable Long id) {
        mandateService.endMandate(id);
        return "redirect:/mandates";
    }
}