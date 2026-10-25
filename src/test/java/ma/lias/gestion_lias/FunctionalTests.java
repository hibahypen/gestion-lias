package ma.lias.gestion_lias;

import ma.lias.gestion_lias.model.*;
import ma.lias.gestion_lias.repository.*;
import ma.lias.gestion_lias.service.MaterialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:lias_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
    "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
@Transactional
class FunctionalTests {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @Autowired PublicationRepository publications;
    @Autowired MaterialRepository materials;
    @Autowired MaterialAssignmentRepository assignments;
    @Autowired MaterialRequestRepository requests;
    @Autowired MaterialService materialService;
    @Autowired ma.lias.gestion_lias.service.MemberService memberService;
    @Autowired ma.lias.gestion_lias.service.HistoryService history;
    @Autowired ma.lias.gestion_lias.service.ConventionService conventionService;
    @Autowired PartnerRepository partners;
    @Autowired ma.lias.gestion_lias.service.AdhesionService adhesions;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder passwords;
    @Autowired ma.lias.gestion_lias.service.DocumentService documents;

    @Test void adminPagesRender() throws Exception {
        for (String path : new String[]{"/dashboard", "/members", "/members/new", "/teams", "/teams/new",
            "/events", "/events/new", "/calendrier", "/publications", "/publications/new", "/profile",
            "/adhesion-requests", "/mandates", "/reports", "/documents", "/documents/pv", "/settings",
            "/materials", "/materials/new", "/materials/distribution", "/materials/requests", "/materials/requests/new",
            "/conventions", "/conventions/new", "/conventions/partners", "/communication", "/search", "/search?query=Data"})
            mvc.perform(get(path).with(user("admin@lias.ma").roles("ADMIN"))).andExpect(status().isOk());
    }
    @Test void memberPagesRender() throws Exception {
        for (String path : new String[]{"/dashboard", "/profile", "/publications", "/publications/new", "/events",
            "/members", "/teams", "/materials/requests", "/materials/requests/new", "/communication"})
            mvc.perform(get(path).with(user("s.bennani@lias.ma").roles("MEMBRE"))).andExpect(status().isOk());
        mvc.perform(get("/publications").with(user("s.bennani@lias.ma").roles("MEMBRE")))
            .andExpect(content().string(containsString("Publications scientifiques")))
            .andExpect(content().string(containsString("/style.css")));
    }
    @Test void publicPagesRender() throws Exception {
        for (String path : new String[]{"/", "/login", "/equipes", "/activites", "/publications-publiques", "/adhesion"})
            mvc.perform(get(path)).andExpect(status().isOk());
        mvc.perform(get("/members")).andExpect(status().is3xxRedirection());
    }
    @Test void doctorantAccess() throws Exception {
        var student = user("o.mansouri@lias.ma").authorities(new SimpleGrantedAuthority("ROLE_MEMBRE"), new SimpleGrantedAuthority("STATUT_DOCTORANT"));
        for (String path : new String[]{"/dashboard", "/profile", "/publications", "/publications/new"})
            mvc.perform(get(path).with(student)).andExpect(status().isOk());
        for (String path : new String[]{"/members", "/teams", "/events", "/documents", "/communication", "/materials/requests/new", "/conventions", "/search"})
            mvc.perform(get(path).with(student)).andExpect(status().isForbidden());
        mvc.perform(get("/dashboard").with(student)).andExpect(content().string(not(containsString("href=\"/events\""))));
    }
    @Test void memberCannotManage() throws Exception {
        for (String path : new String[]{"/events/new", "/events/edit/1", "/materials/edit/1", "/members/new", "/settings"})
            mvc.perform(get(path).with(user("s.bennani@lias.ma").roles("MEMBRE"))).andExpect(status().isForbidden());
    }
    @Test void publicationOwnershipOnSave() throws Exception {
        Publication p = new Publication(); p.setTitle("Original"); p.setAddedBy(members.findByEmail("s.bennani@lias.ma").orElseThrow()); p = publications.saveAndFlush(p);
        mvc.perform(post("/publications/save").with(csrf()).with(user("o.mansouri@lias.ma").roles("MEMBRE"))
            .param("id", p.getId().toString()).param("title", "Intrusion"))
            .andExpect(redirectedUrl("/publications?unauthorized"));
        assertThat(publications.findById(p.getId()).orElseThrow().getTitle()).isEqualTo("Original");
    }
    @Test void publicationCreateEditAndFilters() throws Exception {
        mvc.perform(post("/publications/save").with(csrf()).with(user("s.bennani@lias.ma").roles("MEMBRE"))
            .param("title", "Travaux test").param("pubYear", "2026").param("pubType", "Article").param("journal", "Journal test"))
            .andExpect(redirectedUrl("/publications"));
        Publication p = publications.findAll().stream().filter(x -> "Travaux test".equals(x.getTitle())).findFirst().orElseThrow();
        mvc.perform(get("/publications/edit/" + p.getId()).with(user("s.bennani@lias.ma").roles("MEMBRE"))).andExpect(status().isOk());
        mvc.perform(post("/publications/save").with(csrf()).with(user("s.bennani@lias.ma").roles("MEMBRE"))
            .param("id", p.getId().toString()).param("title", "Travaux corrigés").param("pubYear", "2026"))
            .andExpect(redirectedUrl("/publications"));
        mvc.perform(get("/publications").param("authorId", p.getAddedBy().getId().toString()).param("year", "2026")
            .with(user("admin@lias.ma").roles("ADMIN"))).andExpect(content().string(containsString("Travaux corrigés")));
        mvc.perform(get("/publications").param("year", "2000").with(user("admin@lias.ma").roles("ADMIN")))
            .andExpect(content().string(not(containsString("Travaux corrigés"))));
    }
    @Test void profileCannotChangeRoleAndHistoryIsRecorded() throws Exception {
        LaboratoryMember original = members.findByEmail("s.bennani@lias.ma").orElseThrow();
        mvc.perform(post("/profile/save").with(csrf()).with(user(original.getEmail()).roles("MEMBRE"))
            .param("firstName", "Sara modifiée").param("lastName", "Bennani").param("role", "ADMIN").param("email", "intrusion@lias.ma"))
            .andExpect(redirectedUrl("/profile?success"));
        assertThat(original.getRole()).isEqualTo(Role.MEMBRE);
        assertThat(original.getEmail()).isEqualTo("s.bennani@lias.ma");
        assertThat(history.getHistory(original.getId())).anyMatch(h -> "Prénom".equals(h.getFieldName()));
        mvc.perform(get("/profile").with(user(original.getEmail()).roles("MEMBRE"))).andExpect(status().isOk());
    }
    @Test void archivePreservesMemberAndAffiliation() {
        LaboratoryMember m = members.findByEmail("s.bennani@lias.ma").orElseThrow();
        memberService.archive(m.getId());
        assertThat(members.findById(m.getId())).isPresent();
        assertThat(m.isActive()).isFalse();
        assertThat(m.getStatus()).isEqualTo(LaboratoryMember.MemberStatus.ANCIEN);
        assertThat(history.getAffiliations(m.getId())).allMatch(a -> a.getEndDate() != null);
    }
    @Test void conventionKeepsExistingAttachmentOnEdit() throws Exception {
        Partner partner = new Partner(); partner.setName("Partenaire test"); partner = partners.save(partner);
        Convention c = new Convention(); c.setTitle("Convention test"); c.setDocumentUrl("/documents/download/existant.pdf");
        c = conventionService.saveConvention(c, partner.getId(), null, null);
        Convention form = new Convention(); form.setId(c.getId()); form.setTitle("Convention modifiée");
        Convention saved = conventionService.saveConvention(form, partner.getId(), null, null);
        assertThat(saved.getDocumentUrl()).isEqualTo("/documents/download/existant.pdf");
        mvc.perform(get("/conventions/view/" + saved.getId()).with(user("admin@lias.ma").roles("ADMIN"))).andExpect(status().isOk());
    }
    @Test void requestPrivacy() throws Exception {
        MaterialRequest r = new MaterialRequest(); r.setMaterialName("Confidentiel autre membre"); r.setMember(members.findByEmail("o.mansouri@lias.ma").orElseThrow()); requests.saveAndFlush(r);
        mvc.perform(get("/materials/requests").with(user("s.bennani@lias.ma").roles("MEMBRE")))
            .andExpect(content().string(not(containsString(r.getMaterialName()))));
    }
    @Test void stockAndRepeatedReturn() {
        Material m = new Material(); m.setName("Test"); m.setQuantity(1); m = materials.save(m);
        Long memberId = members.findByEmail("s.bennani@lias.ma").orElseThrow().getId();
        materialService.assignMaterial(m.getId(), memberId, null, null);
        Long id = assignments.findByMemberIdAndActiveTrue(memberId).get(0).getId();
        materialService.returnMaterial(id); materialService.returnMaterial(id);
        assertThat(materials.findById(m.getId()).orElseThrow().getQuantity()).isEqualTo(1);
        Material empty = new Material(); empty.setName("Vide"); empty.setQuantity(0); empty = materials.save(empty);
        Long emptyId = empty.getId();
        assertThatThrownBy(() -> materialService.assignMaterial(emptyId, memberId, null, null)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }
    @Test void destructiveActionsRequirePostAndCsrf() throws Exception {
        Publication p = new Publication(); p.setTitle("Test suppression"); p.setAddedBy(members.findByEmail("s.bennani@lias.ma").orElseThrow()); p = publications.saveAndFlush(p);
        String url = "/publications/delete/" + p.getId();
        mvc.perform(get(url).with(user("s.bennani@lias.ma").roles("MEMBRE"))).andExpect(status().isMethodNotAllowed());
        mvc.perform(post(url).with(user("s.bennani@lias.ma").roles("MEMBRE"))).andExpect(status().isForbidden());
        mvc.perform(post(url).with(csrf()).with(user("s.bennani@lias.ma").roles("MEMBRE"))).andExpect(redirectedUrl("/publications"));
        assertThat(publications.findById(p.getId())).isEmpty();
    }
    @Test void adhesionCreatesUsableAccountAndCannotBeDecidedTwice() {
        AdhesionRequest r = new AdhesionRequest(); r.setFirstName("Test"); r.setLastName("Adhésion"); r.setEmail("test.adhesion@lias.ma"); r.setPassword("123456");
        adhesions.submit(r);
        assertThat(r.getPassword()).isNotEqualTo("123456");
        LaboratoryMember director = members.findByEmail("directeur@lias.ma").orElseThrow();
        adhesions.accept(r.getId(), director, LaboratoryMember.MemberStatus.PERMANENT);
        assertThat(passwords.matches("123456", members.findByEmail(r.getEmail()).orElseThrow().getPassword())).isTrue();
        assertThatThrownBy(() -> adhesions.refuse(r.getId(), director)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }
    @Test void documentUploadDownloadAndConfinement() throws Exception {
        var file = new org.springframework.mock.web.MockMultipartFile("file", "../verification.txt", "text/plain", "contenu test".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        DocumentArchive doc = documents.saveFile(file, "Test document", DocumentArchive.DocumentType.PV_REUNION, java.time.LocalDate.now(), null, "Ordre du jour", "admin@lias.ma");
        String name = doc.getFileDownloadUri().substring(doc.getFileDownloadUri().lastIndexOf('/') + 1);
        try {
            mvc.perform(get("/documents/download/id/" + doc.getId()).with(user("admin@lias.ma").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().bytes("contenu test".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            assertThat(documents.getPVs()).anyMatch(d -> d.getId().equals(doc.getId()));
            assertThatThrownBy(() -> documents.loadFileAsResource("../pom.xml")).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        } finally {
            java.nio.file.Files.deleteIfExists(java.nio.file.Path.of("uploads", name));
        }
    }
}
