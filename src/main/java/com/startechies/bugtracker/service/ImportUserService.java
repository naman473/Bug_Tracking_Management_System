package com.startechies.bugtracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.startechies.bugtracker.model.ImportedUser;
import com.startechies.bugtracker.model.Role;
import com.startechies.bugtracker.repository.ImportUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ImportUserService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ImportUserRepository repository;

    public ImportUserService(ImportUserRepository repository) {
        this.repository = repository;
    }

    /**
     * Parses the uploaded JSON array and inserts only genuinely new users.
     * Uses a single batch SELECT to check existing IDs instead of one query per user.
     *
     * @return number of rows inserted
     * @throws IOException if the file is not valid JSON or not a JSON array
     */
    @Transactional
    public int importUsers(MultipartFile file) throws IOException {
        JsonNode root = MAPPER.readTree(file.getBytes());
        if (!root.isArray()) throw new IOException("Expected a JSON array of users.");

        // 1. Parse all valid candidate users from the file
        List<ImportedUser> candidates = new ArrayList<>();
        for (JsonNode node : root) {
            String userEmail = text(node, "userEmail");
            if (userEmail.isBlank()) continue;

            Role role = Role.fromDb(text(node, "userRole"));
            if (role == null) continue;

            String userId   = userEmail.split("@", 2)[0].toLowerCase(Locale.ROOT);
            String userName = text(node, "userName");
            candidates.add(new ImportedUser(userId, userName, role, userEmail));
        }

        if (candidates.isEmpty()) return 0;

        // 2. Single batch query: which of these IDs already exist?
        Set<String> existingIds = repository.findExistingUserIds(
                candidates.stream().map(ImportedUser::userId).toList());

        // 3. Insert only new ones
        int inserted = 0;
        for (ImportedUser user : candidates) {
            if (!existingIds.contains(user.userId())) {
                inserted += repository.insert(user);
            }
        }
        return inserted;
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null ? "" : v.asText("").trim();
    }
}
