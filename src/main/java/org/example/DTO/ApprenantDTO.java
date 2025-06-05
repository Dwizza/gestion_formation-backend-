package org.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprenantDTO {
    private Long id;
    private String nom;
    private String email;
    private String phone;
    private String status;
    private LocalDate joinDate;

    // Lists for handling multiple groups
    private List<Long> groupeIds = new ArrayList<>();
    private List<String> groupeNames = new ArrayList<>();


}