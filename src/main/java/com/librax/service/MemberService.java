package com.librax.service;

import com.librax.exception.ValidationException;
import com.librax.logging.AuditLogger;
import com.librax.model.Member;
import com.librax.model.MemberStatus;
import com.librax.repository.MemberRepository;
import com.librax.security.SessionManager;
import com.librax.util.ValidationUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MemberService {

    private final MemberRepository memberRepository;
    private final AuditLogger auditLogger;
    private final SessionManager sessionManager;

    public MemberService(MemberRepository memberRepository, AuditLogger auditLogger, SessionManager sessionManager) {
        this.memberRepository = memberRepository;
        this.auditLogger = auditLogger;
        this.sessionManager = sessionManager;
    }

    public Member createMember(Member member) {
        validate(member);
        if (memberRepository.findByMembershipNumber(member.getMembershipNumber()).isPresent()) {
            throw new ValidationException("A member with this membership number already exists.");
        }
        if (member.getRegistrationDate() == null) member.setRegistrationDate(LocalDate.now());
        if (member.getMembershipExpiry() == null) member.setMembershipExpiry(LocalDate.now().plusYears(1));
        if (member.getStatus() == null) member.setStatus(MemberStatus.ACTIVE);
        Member saved = memberRepository.save(member);
        audit("MEMBER_CREATED", saved.getId(), "Created member: " + saved.getFullName());
        return saved;
    }

    public void updateMember(Member member) {
        validate(member);
        memberRepository.update(member);
        audit("MEMBER_UPDATED", member.getId(), "Updated member: " + member.getFullName());
    }

    public void deactivateMember(int memberId) {
        memberRepository.findById(memberId).ifPresent(m -> {
            m.setStatus(MemberStatus.SUSPENDED);
            memberRepository.update(m);
            audit("MEMBER_DEACTIVATED", memberId, "Deactivated member #" + memberId);
        });
    }

    public Optional<Member> findById(int id) {
        return memberRepository.findById(id);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public List<Member> search(String query) {
        return ValidationUtils.isBlank(query) ? memberRepository.findAll() : memberRepository.search(query.trim());
    }

    /** Returns whether a member is currently eligible to borrow (active, not expired). */
    public boolean isEligibleToBorrow(Member member) {
        return member.getStatus() == MemberStatus.ACTIVE
                && (member.getMembershipExpiry() == null || !member.getMembershipExpiry().isBefore(LocalDate.now()));
    }

    private void validate(Member m) {
        if (ValidationUtils.isBlank(m.getFullName())) {
            throw new ValidationException("Full name is required.");
        }
        if (!ValidationUtils.isValidEmail(m.getEmail())) {
            throw new ValidationException("A valid email address is required.");
        }
        if (!ValidationUtils.isValidPhone(m.getPhone())) {
            throw new ValidationException("A valid phone number is required.");
        }
        if (ValidationUtils.isBlank(m.getMembershipNumber())) {
            throw new ValidationException("Membership number is required.");
        }
    }

    private void audit(String action, Integer entityId, String description) {
        Integer userId = sessionManager.getCurrentUser() != null ? sessionManager.getCurrentUser().getId() : null;
        auditLogger.log(userId, action, "MEMBER", entityId, description);
    }
}
