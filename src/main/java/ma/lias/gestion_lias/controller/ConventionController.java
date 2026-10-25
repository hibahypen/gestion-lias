package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.Convention;
import ma.lias.gestion_lias.model.Partner;
import ma.lias.gestion_lias.service.ConventionService;
import ma.lias.gestion_lias.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/conventions")
@RequiredArgsConstructor
public class ConventionController {

    private final ConventionService conventionService;
    private final EventService eventService;

    // 1. Liste des conventions
    @GetMapping
    public String list(Model model) {
        model.addAttribute("conventions", conventionService.getAllConventions());
        model.addAttribute("activeCount", conventionService.countActiveConventions());
        model.addAttribute("partnersCount", conventionService.getAllPartners().size());
        return "conventions";
    }

    // 2. Création convention
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("convention", new Convention());
        model.addAttribute("partners", conventionService.getAllPartners());
        model.addAttribute("events", eventService.getAllEvents());
        return "convention-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Convention convention,
                       @RequestParam Long partnerId,
                       @RequestParam(required = false) List<Long> eventIds,
                       @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        conventionService.saveConvention(convention, partnerId, eventIds, file);
        return "redirect:/conventions";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Convention conv = conventionService.getConventionById(id);
        model.addAttribute("convention", conv);
        model.addAttribute("partners", conventionService.getAllPartners());
        model.addAttribute("events", eventService.getAllEvents());
        return "convention-form";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("convention", conventionService.getConventionById(id));
        return "convention-view";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        conventionService.deleteConvention(id);
        return "redirect:/conventions";
    }

    // 3. Gestion des partenaires
    @GetMapping("/partners")
    public String listPartners(Model model) {
        model.addAttribute("partners", conventionService.getAllPartners());
        return "partners";
    }

    @GetMapping("/partners/new")
    public String newPartnerForm(Model model) {
        model.addAttribute("partner", new Partner());
        return "partner-form";
    }

    @PostMapping("/partners/save")
    public String savePartner(@ModelAttribute Partner partner) {
        conventionService.savePartner(partner);
        return "redirect:/conventions/partners";
    }

    @PostMapping("/partners/delete/{id}")
    public String deletePartner(@PathVariable Long id) {
        conventionService.deletePartner(id);
        return "redirect:/conventions/partners";
    }
}