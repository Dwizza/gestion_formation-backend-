package org.example.config;

public  class LoginRequest {
    private String email;    // CHANGED FROM 'nom'
    private String password; // CHANGED FROM 'motDePasse'

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
