package org.example.config;

import org.example.Model.Admin;
import org.example.Repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Admin enregistrerAdmin(Admin admin) {
        // Hachage du mot de passe
        if (admin.getMotDePasse() != null && !admin.getMotDePasse().startsWith("$2a$")) {
            // Only encode if not already encoded
            String motDePasseCrypte = passwordEncoder.encode(admin.getMotDePasse());
            admin.setMotDePasse(motDePasseCrypte);
        }
        return adminRepository.save(admin);
    }

    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email).orElse(null);
    }

    // New method to find admin by ID
    public Admin findById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    // New method to find all admins
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    // New method to find admins by role
    public List<Admin> findByRole(String role) {
        return adminRepository.findAll().stream()
                .filter(admin -> admin.getRole() != null && admin.getRole().equals(role))
                .collect(Collectors.toList());
    }

    // Add the missing existsById method
    public boolean existsById(Long id) {
        return adminRepository.existsById(id);
    }

    // Add the missing deleteById method
    public void deleteById(Long id) {
        adminRepository.deleteById(id);
    }

    // Update seConnecter to use email
    public boolean seConnecter(String email, String password) {
        Admin admin = findByEmail(email);
        if (admin != null) {
            return passwordEncoder.matches(password, admin.getMotDePasse());
        }
        return false;
    }
}