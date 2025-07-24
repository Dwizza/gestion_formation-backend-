package org.example.Service;

import org.example.DTO.SessionDTO;
import org.example.Model.Formateur;
import org.example.Model.Groupe;
import org.example.Model.Session;
import org.example.Repository.FormateurRepository;
import org.example.Repository.GroupeRepository;
import org.example.Repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private GroupeRepository groupeRepository;

    @Autowired
    private FormateurRepository formateurRepository;

    public List<SessionDTO> getAllSessions() {
        return sessionRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public SessionDTO getSessionById(Long id) {
        return sessionRepository.findById(id).map(this::convertToDTO).orElse(null);
    }

    public SessionDTO createSession(SessionDTO sessionDTO) {
        Session session = convertToEntity(sessionDTO);
        Session savedSession = sessionRepository.save(session);
        return convertToDTO(savedSession);
    }

    public SessionDTO updateSession(Long id, SessionDTO sessionDTO) {
        if (!sessionRepository.existsById(id)) {
            return null;
        }
        Session session = convertToEntity(sessionDTO);
        session.setId(id);
        Session updatedSession = sessionRepository.save(session);
        return convertToDTO(updatedSession);
    }

    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }

    private SessionDTO convertToDTO(Session session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setDate(session.getDate());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setStatus(session.getStatus());
        if (session.getGroupe() != null) {
            dto.setGroupeId(session.getGroupe().getId());
        }
        if (session.getFormateur() != null) {
            dto.setFormateurId(session.getFormateur().getId());
        }
        return dto;
    }

    private Session convertToEntity(SessionDTO dto) {
        Session session = new Session();
        session.setId(dto.getId());
        session.setDate(dto.getDate());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setStatus(dto.getStatus());

        if (dto.getGroupeId() != null) {
            Groupe groupe = groupeRepository.findById(dto.getGroupeId())
                    .orElseThrow(() -> new RuntimeException("Groupe not found"));
            session.setGroupe(groupe);
        }

        if (dto.getFormateurId() != null) {
            Formateur formateur = formateurRepository.findById(dto.getFormateurId())
                    .orElseThrow(() -> new RuntimeException("Formateur not found"));
            session.setFormateur(formateur);
        }

        return session;
    }
}

