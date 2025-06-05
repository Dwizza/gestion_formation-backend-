package org.example.config;

import org.example.Model.Admin;
import org.example.Model.Formateur;
import org.example.Repository.FormateurRepository;
import org.example.config.AdminService;
import org.example.config.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")  // signup & login both live here
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private FormateurRepository formateurRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Admin admin = adminService.findByEmail(request.getEmail());

            if (admin != null && adminService.seConnecter(request.getEmail(), request.getPassword())) {
                String token = jwtTokenProvider.generateToken(
                        admin.getId(), admin.getEmail(), admin.getRole()
                );

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);

                Map<String, Object> userResponse = new HashMap<>();
                userResponse.put("name", admin.getNom());
                userResponse.put("email", admin.getEmail());
                userResponse.put("role", admin.getRole());

                // Check if the user is a trainer (either format: "trainer" or "ROLE_TRAINER")
                if ("trainer".equalsIgnoreCase(admin.getRole()) ||
                        (admin.getRole() != null && admin.getRole().toUpperCase().contains("TRAINER"))) {

                    Formateur formateur = formateurRepository.findByEmail(admin.getEmail());
                    if (formateur != null) {
                        // Use existing formateur ID (don't update it in login flow to avoid DB issues)
                        userResponse.put("id", formateur.getId());
                    } else {
                        // If we can't find a formateur, fall back to admin ID
                        userResponse.put("id", admin.getId());
                    }
                } else {
                    userResponse.put("id", admin.getId()); // fallback for other roles
                }

                response.put("user", userResponse);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (Exception e) {
            // Log the exception
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            // 1) Email must be unique
            if (adminService.findByEmail(request.getEmail()) != null) {
                return ResponseEntity.badRequest().body("Email already in use");
            }

            // 2) Role validation
            String role = request.getRole().toUpperCase();
            if (!role.equals("ADMIN") && !role.equals("TRAINER")) {
                return ResponseEntity.badRequest().body("Invalid role selected");
            }

            // 3) Create and save Admin
            Admin newAdmin = new Admin();
            newAdmin.setNom(request.getNom());
            newAdmin.setEmail(request.getEmail());
            newAdmin.setMotDePasse(request.getMotDePasse());
            newAdmin.setRole("ROLE_" + role);
            Admin savedAdmin = adminService.enregistrerAdmin(newAdmin);

            // 4) If TRAINER, create Formateur row
            if (role.equals("TRAINER")) {
                // Check if a formateur with this email already exists
                Formateur existingFormateur = formateurRepository.findByEmail(savedAdmin.getEmail());

                if (existingFormateur != null) {
                    // Update existing formateur to match admin ID
                    existingFormateur.setId(savedAdmin.getId());
                    existingFormateur.setName(savedAdmin.getNom());
                    formateurRepository.save(existingFormateur);
                } else {
                    // Create new formateur
                    Formateur newFormateur = new Formateur();
                    newFormateur.setId(savedAdmin.getId());     // Set same ID
                    newFormateur.setName(savedAdmin.getNom());
                    newFormateur.setEmail(savedAdmin.getEmail());
                    newFormateur.setPhone(null);                // optional
                    if (newFormateur.getGroupes() != null) {
                        newFormateur.getGroupes().clear();      // optional
                    }
                    formateurRepository.save(newFormateur);
                }
            }

            // 5) Generate JWT
            String token = jwtTokenProvider.generateToken(
                    savedAdmin.getId(), savedAdmin.getEmail(), savedAdmin.getRole()
            );

            // 6) Build response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", savedAdmin.getId());
            userResponse.put("name", savedAdmin.getNom());
            userResponse.put("email", savedAdmin.getEmail());
            userResponse.put("role", role);
            response.put("user", userResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the exception
            System.err.println("Signup error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}