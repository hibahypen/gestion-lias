package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.Material;
import ma.lias.gestion_lias.model.MaterialRequest;
import ma.lias.gestion_lias.service.MaterialService;
import ma.lias.gestion_lias.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final MemberService memberService;

    // 1. Liste du parc matériel
    @GetMapping
    public String list(Model model) {
        model.addAttribute("materials", materialService.getAllMaterials());
        model.addAttribute("pendingRequests", materialService.countPendingRequests());
        return "materials";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("material", new Material());
        return "material-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Material material) {
        materialService.saveMaterial(material);
        return "redirect:/materials";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("material", materialService.getMaterialById(id));
        return "material-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return "redirect:/materials";
    }

    // 2. Vue Distribution équitable (§14) : Qui a reçu quoi / Qui n'a rien reçu
    @GetMapping("/distribution")
    public String distribution(Model model) {
        model.addAttribute("stats", materialService.getDistributionStats());
        model.addAttribute("materials", materialService.getAllMaterials());
        model.addAttribute("members", memberService.getActiveMembers());
        return "material-distribution";
    }

    @PostMapping("/assign")
    public String assign(@RequestParam Long materialId,
                         @RequestParam Long memberId,
                         @RequestParam(required = false) String returnDate,
                         @RequestParam(required = false) String notes) {
        LocalDate date = (returnDate != null && !returnDate.isBlank()) ? LocalDate.parse(returnDate) : null;
        materialService.assignMaterial(materialId, memberId, date, notes);
        return "redirect:/materials/distribution";
    }

    @PostMapping("/return/{id}")
    public String returnMaterial(@PathVariable Long id) {
        materialService.returnMaterial(id);
        return "redirect:/materials/distribution";
    }

    // 3. Gestion des besoins (§15)
    @GetMapping("/requests")
    public String requests(Model model, Authentication auth) {
        boolean governance = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DIRECTEUR"));
        model.addAttribute("requests", governance ? materialService.getAllRequests()
                : materialService.getRequestsByMember(memberService.getByEmail(auth.getName()).getId()));
        return "material-requests";
    }

    @GetMapping("/requests/new")
    public String newRequestForm(Model model) {
        model.addAttribute("materialRequest", new MaterialRequest());
        return "material-request-form";
    }

    @PostMapping("/requests/save")
    public String saveRequest(@ModelAttribute MaterialRequest request, Authentication auth) {
        materialService.submitRequest(request, auth.getName());
        return "redirect:/materials/requests";
    }

    @PostMapping("/requests/accept/{id}")
    public String acceptRequest(@PathVariable Long id, Authentication auth) {
        materialService.decideRequest(id, MaterialRequest.RequestStatus.VALIDEE, auth.getName());
        return "redirect:/materials/requests";
    }

    @PostMapping("/requests/refuse/{id}")
    public String refuseRequest(@PathVariable Long id, Authentication auth) {
        materialService.decideRequest(id, MaterialRequest.RequestStatus.REFUSEE, auth.getName());
        return "redirect:/materials/requests";
    }
}