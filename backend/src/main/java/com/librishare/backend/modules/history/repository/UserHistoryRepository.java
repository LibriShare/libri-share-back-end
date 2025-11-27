package com.librishare.backend.modules.history.repository;

import com.librishare.backend.modules.history.entity.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    List<UserHistory> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);
}