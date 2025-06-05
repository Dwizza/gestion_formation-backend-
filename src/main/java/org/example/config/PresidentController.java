package org.example.config;

import org.example.Model.Admin;
import org.example.Model.Formateur;
import org.example.Model.President;
import org.example.Repository.FormateurRepository;
import org.example.Repository.PresidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/president")
@CrossOrigin(origins = "http://localhost:5173")
public class PresidentController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private PresidentRepository presidentRepository;

    @Autowired
    private FormateurRepository formateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // FIXED LOGIN ENDPOINT
    @PostMapping("/login")
    public ResponseEntity<?> presidentLogin(@RequestBody LoginRequest request) {
        try {
            // Add debug logging
            System.out.println("President login attempt for email: " + request.getEmail());

            // Find president by email directly from president repository
            President president = presidentRepository.findByEmail(request.getEmail())
                    .orElse(null);

            // Debug output
            if (president == null) {
                System.out.println("No president found with email: " + request.getEmail());
                return ResponseEntity.status(401).body("Invalid credentials or not authorized as president");
            }

            System.out.println("President found, validating password");

            // Check if president exists and password is correct
            if (
                // If using password encoder (make sure this matches your frontend):
                    passwordEncoder.matches(request.getPassword(), president.getMotDePasse())
                // If using plain text comparison (for testing):
                // president.getMotDePasse().equals(request.getPassword())
            ) {
                System.out.println("Password validation successful, generating token");

                String token = jwtTokenProvider.generateToken(
                        president.getId(), president.getEmail(), "ROLE_PRESIDENT"
                );

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);

                Map<String, Object> userResponse = new HashMap<>();
                userResponse.put("id", president.getId());
                userResponse.put("name", president.getNom());
                userResponse.put("email", president.getEmail());
                userResponse.put("role", "PRESIDENT");
                response.put("user", userResponse);

                System.out.println("Login successful for president: " + president.getEmail());
                return ResponseEntity.ok(response);
            }

            System.out.println("Password validation failed for president: " + president.getEmail());
            return ResponseEntity.status(401).body("Invalid credentials or not authorized as president");
        } catch (Exception e) {
            System.err.println("Error in president login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Only the president can create admin and trainer accounts
    @PostMapping("/create-account")
    public ResponseEntity<?> createAccount(@RequestBody SignupRequest request, @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(401).body("Invalid or expired token");
            }

            // Extract claims and verify president role
            var claims = jwtTokenProvider.getClaimsFromToken(token);
            String role = claims.get("role", String.class);
            if (!"ROLE_PRESIDENT".equals(role)) {
                return ResponseEntity.status(403).body("Only the president can create accounts");
            }

            // Check if email already exists
            if (adminService.findByEmail(request.getEmail()) != null) {
                return ResponseEntity.badRequest().body("Email already in use");
            }

            // Validate requested role (only ADMIN or TRAINER allowed)
            String requestedRole = request.getRole().toUpperCase();
            if (!requestedRole.equals("ADMIN") && !requestedRole.equals("TRAINER")) {
                return ResponseEntity.badRequest().body("Invalid role selected");
            }

            // Create and save Admin with proper role
            Admin newAdmin = new Admin();
            newAdmin.setNom(request.getNom());
            newAdmin.setEmail(request.getEmail());
            newAdmin.setMotDePasse(request.getMotDePasse());
            newAdmin.setRole("ROLE_" + requestedRole);
            newAdmin.setActive(true); // Set active by default
            Admin savedAdmin = adminService.enregistrerAdmin(newAdmin);

            // If creating a trainer, also create a Formateur entry
            if (requestedRole.equals("TRAINER")) {
                try {
                    // Create a new Formateur entity with minimal required fields
                    Formateur formateur = new Formateur();
                    formateur.setName(request.getNom());
                    formateur.setEmail(request.getEmail());

                    // Save the formateur - we'll let JPA handle any required fields
                    Formateur savedFormateur = formateurRepository.save(formateur);

                    System.out.println("Created trainer with ID: " + savedFormateur.getId() +
                            " linked to admin with ID: " + savedAdmin.getId());
                } catch (Exception e) {
                    System.err.println("Error while creating formateur: " + e.getMessage());
                    e.printStackTrace();
                    // Continue with the response - we've already created the admin account
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account created successfully",
                    "user", Map.of(
                            "id", savedAdmin.getId(),
                            "name", savedAdmin.getNom(),
                            "email", savedAdmin.getEmail(),
                            "role", requestedRole
                    )
            ));
        } catch (Exception e) {
            System.err.println("Error creating account: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestParam(required = false) String role,
                                      @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("Get users endpoint called with role: " + role);

            // Validate token
            String token = authHeader.substring(7);
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(401).body("Invalid or expired token");
            }

            // Get all admins (or filtered by role if provided)
            List<Admin> admins;
            if (role != null && !role.isEmpty()) {
                admins = adminService.findByRole("ROLE_" + role);
            } else {
                admins = adminService.findAll();
            }

            System.out.println("Found " + admins.size() + " users");

            // Map to response format
            List<Map<String, Object>> response = admins.stream()
                    .map(admin -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", admin.getId());
                        userMap.put("name", admin.getNom());
                        userMap.put("email", admin.getEmail());
                        userMap.put("role", admin.getRole().replace("ROLE_", ""));
                        userMap.put("active", admin.isActive());
                        return userMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error getting users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token
            String token = authHeader.substring(7);
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(401).build();
            }

            // Extract claims and verify president role
            var claims = jwtTokenProvider.getClaimsFromToken(token);
            String role = claims.get("role", String.class);
            if (!"ROLE_PRESIDENT".equals(role)) {
                return ResponseEntity.status(403).build();
            }

            // Check if admin exists
            if (!adminService.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            // Delete the admin
            adminService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}