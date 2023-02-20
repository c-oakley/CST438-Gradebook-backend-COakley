package com.cst438.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends CrudRepository <Course, Integer> {
//	@Query("select c from Couse a where c.needsGrading=1 and a.dueDate < current_date and a.course.instructor= :email order by a.id")
//	List<Assignment> findNeedGradingByEmail(@Param("email") String email);

}
