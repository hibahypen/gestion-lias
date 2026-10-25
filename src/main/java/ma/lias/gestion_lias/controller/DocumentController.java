package ma.lias.gestion_lias.controller;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.DocumentArchive;
import ma.lias.gestion_lias.service.DocumentService;
import ma.lias.gestion_lias.service.EventService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final EventService eventService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("documents", documentService.getAll());
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("types", DocumentArchive.DocumentType.values());
        return "documents";
    }

    @GetMapping("/pv")
    public String listPV(Model model) {
        model.addAttribute("documents", documentService.getPVs());
        model.addAttribute("events", eventService.getAllEvents());
        return "documents-pv";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam String title,
                         @RequestParam DocumentArchive.DocumentType docType,
                         @RequestParam(required = false) String docDate,
                         @RequestParam(required = false) Long eventId,
                         @RequestParam(required = false) String ordreDuJour,
                         Authentication auth) throws IOException {

        LocalDate date = (docDate != null && !docDate.isBlank()) ? LocalDate.parse(docDate) : LocalDate.now();
        documentService.saveFile(file, title, docType, date, eventId, ordreDuJour, auth.getName());

        if (docType == DocumentArchive.DocumentType.PV_REUNION) {
            return "redirect:/documents/pv";
        }
        return "redirect:/documents";
    }

    @GetMapping("/download/id/{id}")
    public ResponseEntity<Resource> downloadById(@PathVariable Long id) {
        DocumentArchive doc = documentService.getById(id);
        String storedFileName = doc.getFileDownloadUri().substring(doc.getFileDownloadUri().lastIndexOf('/') + 1);
        Resource resource = documentService.loadFileAsResource(storedFileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        Resource resource = documentService.loadFileAsResource(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        documentService.delete(id);
        return "redirect:/documents";
    }
}