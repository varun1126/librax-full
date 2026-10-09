package com.librax.repository;

import com.librax.model.Member;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(int id);
    Optional<Member> findByMembershipNumber(String membershipNumber);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByUserId(int userId);
    List<Member> findAll();
    List<Member> search(String query);
    void update(Member member);
    void delete(int id);
}
