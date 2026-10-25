package ma.lias.gestion_lias.service;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Storage names never contain path components supplied by the client. */
final class StoredFileNames {
    private StoredFileNames() { }
    static String original(String name) {
        if (name == null || name.isBlank()) return "document";
        String clean = name.replace('\\', '/');
        clean = clean.substring(clean.lastIndexOf('/') + 1);
        clean = clean.replaceAll("[\\r\\n\\x00\"]", "_");
        if (clean.isBlank() || clean.equals(".") || clean.equals(".."))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
        return clean;
    }
    static String stored(String original) { return UUID.randomUUID() + "_" + original(original); }
}
