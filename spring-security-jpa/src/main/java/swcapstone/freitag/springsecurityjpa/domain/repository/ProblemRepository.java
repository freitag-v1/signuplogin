package swcapstone.freitag.springsecurityjpa.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {

    long count();
    long countByReferenceIdAndValidationStatus(int referenceId, String validationStatus);
    long countByValidationStatusAndLevel(String validationStatus, String level);
    long countByUserId(String userId);
    long countByUserIdAndValidationStatus(String userId, String validationStatus);
    long countByUserIdAndValidationStatusAndRightAnswer(String userId, String validationStatus, Boolean rightAnswer);
    Optional<ProblemEntity> findByProblemId(int problemId);
    Optional<ProblemEntity> findFirstByProjectIdAndValidationStatus(int projectId, String validationStatus);
    Optional<ProblemEntity> findTopByOrderByProblemIdDesc();
    Optional<ProblemEntity> findOneByValidationStatus(String validationStatus);
    Optional<ProblemEntity> findByReferenceIdAndValidationStatusAndLevel(int referenceId, String validationStatus, String level);
    List<ProblemEntity> findAllByProjectId(int projectId);
    List<ProblemEntity> findAllByReferenceId(int referenceId);
    List<ProblemEntity> findAllByReferenceIdAndValidationStatus(int referenceId, String validationStatus);
    List<ProblemEntity> findAllByProjectIdAndReferenceIdAndValidationStatus(int projectId, int referenceId, String validationStatus);
}
