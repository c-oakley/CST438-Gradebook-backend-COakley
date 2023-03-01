package com.cst438;

import static com.cst438.test.utils.TestUtils.asJsonString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.Optional;
import java.sql.Date;

import com.cst438.controllers.GradeBookController;
import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.GradebookDTO;
import com.cst438.domain.AssignmentListDTO.AssignmentDTO;
import com.cst438.services.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ContextConfiguration;

/* 
 * Example of using Junit with Mockito for mock objects
 *  the database repositories are mocked with test data.
 *  
 * Mockmvc is used to test a simulated REST call to the RestController
 * 
 * the http response and repository is verified.
 * 
 *   Note: This tests uses Junit 5.
 *  ContextConfiguration identifies the controller class to be tested
 *  addFilters=false turns off security.  (I could not get security to work in test environment.)
 *  WebMvcTest is needed for test environment to create Repository classes.
 */
@ContextConfiguration(classes = { GradeBookController.class })
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest
public class JunitTestGradebook {

	static final String URL = "http://localhost:8080";
	public static final int TEST_COURSE_ID = 40442;
	public static final String TEST_COURSE_TITLE = "Test Course";
	public static final String TEST_STUDENT_EMAIL = "test@csumb.edu";
	public static final String TEST_STUDENT_NAME = "test";
	public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
	public static final int TEST_YEAR = 2021;
	public static final String TEST_SEMESTER = "Fall";
	public static final String TEST_NAME = "Assignment 2";
	//public static final Date DUE_DATE = "02/21/2023";

	@MockBean
	AssignmentRepository assignmentRepository;

	@MockBean
	AssignmentGradeRepository assignmentGradeRepository;

	@MockBean
	CourseRepository courseRepository; // must have this to keep Spring test happy

	@MockBean
	RegistrationService registrationService; // must have this to keep Spring test happy

	@Autowired
	private MockMvc mvc;

	@Test
	public void gradeAssignment() throws Exception {

		MockHttpServletResponse response;

		// mock database data

		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setEnrollments(new java.util.ArrayList<Enrollment>());
		course.setAssignments(new java.util.ArrayList<Assignment>());

		Enrollment enrollment = new Enrollment();
		enrollment.setCourse(course);
		course.getEnrollments().add(enrollment);
		enrollment.setId(TEST_COURSE_ID);
		enrollment.setStudentEmail(TEST_STUDENT_EMAIL);
		enrollment.setStudentName(TEST_STUDENT_NAME);

		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		course.getAssignments().add(assignment);
		// set dueDate to 1 week before now.
		assignment.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
		assignment.setId(1);
		assignment.setName("Assignment 1");
		assignment.setNeedsGrading(1);

		AssignmentGrade ag = new AssignmentGrade();
		ag.setAssignment(assignment);
		ag.setId(1);
		ag.setScore("");
		ag.setStudentEnrollment(enrollment);

		// given -- stubs for database repositories that return test data
		given(assignmentRepository.findById(1)).willReturn(Optional.of(assignment));
		given(assignmentGradeRepository.findByAssignmentIdAndStudentEmail(1, TEST_STUDENT_EMAIL)).willReturn(null);
		given(assignmentGradeRepository.save(any())).willReturn(ag);

		// end of mock data

		// then do an http get request for assignment 1
		response = mvc.perform(MockMvcRequestBuilders.get("/gradebook/1").accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify return data with entry for one student without no score
		assertEquals(200, response.getStatus());

		// verify that a save was called on repository
		verify(assignmentGradeRepository, times(1)).save(any()); // ???

		// verify that returned data has non zero primary key
		GradebookDTO result = fromJsonString(response.getContentAsString(), GradebookDTO.class);
		// assignment id is 1
		assertEquals(1, result.assignmentId);
		// there is one student list
		assertEquals(1, result.grades.size());
		assertEquals(TEST_STUDENT_NAME, result.grades.get(0).name);
		assertEquals("", result.grades.get(0).grade);

		// change grade to score = 80
		result.grades.get(0).grade = "80";

		given(assignmentGradeRepository.findById(1)).willReturn(Optional.of(ag));

		// send updates to server
		response = mvc
				.perform(MockMvcRequestBuilders.put("/gradebook/1").accept(MediaType.APPLICATION_JSON)
						.content(asJsonString(result)).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify that return status = OK (value 200)
		assertEquals(200, response.getStatus());

		AssignmentGrade updatedag = new AssignmentGrade();
		updatedag.setId(1);
		updatedag.setScore("80");

		// verify that repository saveAll method was called
		verify(assignmentGradeRepository, times(1)).save(updatedag);
	}

	@Test
	public void updateAssignmentGrade() throws Exception {

		MockHttpServletResponse response;

		// mock database data

		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setEnrollments(new java.util.ArrayList<Enrollment>());
		course.setAssignments(new java.util.ArrayList<Assignment>());

		Enrollment enrollment = new Enrollment();
		enrollment.setCourse(course);
		course.getEnrollments().add(enrollment);
		enrollment.setId(TEST_COURSE_ID);
		enrollment.setStudentEmail(TEST_STUDENT_EMAIL);
		enrollment.setStudentName(TEST_STUDENT_NAME);

		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		course.getAssignments().add(assignment);
		// set dueDate to 1 week before now.
		assignment.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
		assignment.setId(1);
		assignment.setName("Assignment 1");
		assignment.setNeedsGrading(1);

		AssignmentGrade ag = new AssignmentGrade();
		ag.setAssignment(assignment);
		ag.setId(1);
		ag.setScore("80");
		ag.setStudentEnrollment(enrollment);

		// given -- stubs for database repositories that return test data
		given(assignmentRepository.findById(1)).willReturn(Optional.of(assignment));
		given(assignmentGradeRepository.findByAssignmentIdAndStudentEmail(1, TEST_STUDENT_EMAIL)).willReturn(ag);
		given(assignmentGradeRepository.findById(1)).willReturn(Optional.of(ag));

		// end of mock data

		// then do an http get request for assignment 1
		response = mvc.perform(MockMvcRequestBuilders.get("/gradebook/1").accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify return data with entry for one student without no score
		assertEquals(200, response.getStatus());

		// verify that a save was NOT called on repository because student already has a
		// grade
		verify(assignmentGradeRepository, times(0)).save(any());

		// verify that returned data has non zero primary key
		GradebookDTO result = fromJsonString(response.getContentAsString(), GradebookDTO.class);
		// assignment id is 1
		assertEquals(1, result.assignmentId);
		// there is one student list
		assertEquals(1, result.grades.size());
		assertEquals(TEST_STUDENT_NAME, result.grades.get(0).name);
		assertEquals("80", result.grades.get(0).grade);

		// change grade to score = 88
		result.grades.get(0).grade = "88";

		// send updates to server
		response = mvc
				.perform(MockMvcRequestBuilders.put("/gradebook/1").accept(MediaType.APPLICATION_JSON)
						.content(asJsonString(result)).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify that return status = OK (value 200)
		assertEquals(200, response.getStatus());

		// verify that repository save method was called
		// AssignmentGrade must override equals method for this test for work !!!
		AssignmentGrade updatedag = new AssignmentGrade();
		updatedag.setId(1);
		updatedag.setScore("88");
		verify(assignmentGradeRepository, times(1)).save(updatedag);
	}

	private static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T fromJsonString(String str, Class<T> valueType) {
		try {
			return new ObjectMapper().readValue(str, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	// As an instructor for a course , I can add a new assignment for my course.  The assignment has a name and a due date.
	@Test
	public void testCreateNewAssignment() throws Exception {
		// create course for mock		
		Course course = new Course();
		course.setCourse_id(99999);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setSemester("Fall");
		course.setYear(2021);
		course.setTitle(TEST_COURSE_TITLE);
		assertEquals(TEST_COURSE_TITLE, course.getTitle());
		
		assertEquals(null, courseRepository.findById(course.getCourse_id()).orElse(null));
		given(courseRepository.findById(99999)).willReturn(Optional.of(course));
		// this line fails since object is now in mock DB
		// assertEquals(null, courseRepository.findById(course.getCourse_id()).orElse(null));
		
		// Use AssignmentDTO to transfer objects to the endpoint
		//	Create new assignment object that is sent to the server
		AssignmentDTO newAssignment = new AssignmentDTO(10, course.getCourse_id(), 
				"Assignment 123", "2023-02-28", course.getTitle());
		
		// send updates to server
		MockHttpServletResponse result = mvc
				.perform(MockMvcRequestBuilders.post("/course/99999/createAssignment")
				.content(asJsonString(newAssignment)).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		
		// verify we successfully sent the object to the server
		assertEquals(200, result.getStatus());
	}
	
	// As an instructor, I can change the name of the assignment for my course.
	@Test
	public void testUpdateAssignmentName() throws Exception {
		// create course for mock		
		Course course = new Course();
		course.setCourse_id(99999);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setSemester("Fall");
		course.setYear(2021);
		course.setTitle(TEST_COURSE_TITLE);
		assertEquals(TEST_COURSE_TITLE, course.getTitle());
		
		// create assignment for mock
		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		// set dueDate to 1 week before now.
		assignment.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
		assignment.setId(123);
		assignment.setName("Assignment 1");
		assignment.setNeedsGrading(1);
		
		assertEquals(null, courseRepository.findById(course.getCourse_id()).orElse(null));
		assertEquals(null, assignmentRepository.findById(assignment.getId()).orElse(null));
		given(courseRepository.findById(99999)).willReturn(Optional.of(course));
		given(assignmentRepository.findById(123)).willReturn(Optional.of(assignment));
		// this line fails since object is now in mock DB
		// assertEquals(null, courseRepository.findById(course.getCourse_id()).orElse(null));
		// assertEquals(null, assignmentRepository.findById(assignment.getId()).orElse(null));
		
		// Use AssignmentDTO to transfer objects to the endpoint
		//	Create new assignment object that is sent to the server
		AssignmentDTO newAssignment = new AssignmentDTO(assignment.getId(), course.getCourse_id(), 
				"Assignment 123", "2023-02-28", course.getTitle());
		
		// send updates to server
		MockHttpServletResponse result = mvc
				.perform(MockMvcRequestBuilders.put("/course/99999/updateAssignmentName")
				.content(asJsonString(newAssignment)).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		
		// verify we successfully sent the object to the server
		assertEquals(200, result.getStatus());
	}
		
	// As an instructor, I can delete an assignment  for my course (only if there are no grades for the assignment).
	@Test
	public void testDeleteAssignment() throws Exception {
		//
		// TEST BEST CASE
		//
		
		// create course entry in mock repo
		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setEnrollments(new java.util.ArrayList<Enrollment>());
		course.setAssignments(new java.util.ArrayList<Assignment>());

		// create assignment for mock
		Assignment assignment1 = new Assignment();
		assignment1.setCourse(course);
		// set dueDate to 1 week before now.
		assignment1.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
		assignment1.setId(123);
		assignment1.setName("Assignment 1");
		assignment1.setNeedsGrading(1);
		
		assertEquals(null, courseRepository.findById(course.getCourse_id()).orElse(null));
		assertEquals(null, assignmentRepository.findById(assignment1.getId()).orElse(null));
		// update mock repo with entries
		given(courseRepository.findById(99999)).willReturn(Optional.of(course));
		given(assignmentRepository.findById(123)).willReturn(Optional.of(assignment1));
		
		// Use AssignmentDTO to transfer objects to the endpoint
		//	Create new assignment object that is sent to the server
		AssignmentDTO assignmentDTO1 = new AssignmentDTO(assignment1.getId(), course.getCourse_id(), 
				"Assignment 123", "2023-02-28", course.getTitle());
		
		// send updates to server
		MockHttpServletResponse result = mvc
				.perform(MockMvcRequestBuilders.delete("/course/99999/deleteAssignment")
				.content(asJsonString(assignmentDTO1)).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		
		// verify we successfully deleted the object
		assertEquals(200, result.getStatus());
		
		//
		// TEST FAIL CASE
		//
		
		// create enrollemnt entry in mock repo
		Enrollment enrollment = new Enrollment();
		enrollment.setCourse(course);
		course.getEnrollments().add(enrollment);
		enrollment.setId(TEST_COURSE_ID);
		enrollment.setStudentEmail(TEST_STUDENT_EMAIL);
		enrollment.setStudentName(TEST_STUDENT_NAME);

		// create assignment entry in mock repo
		Assignment assignment2 = new Assignment();
		assignment2.setCourse(course);
		course.getAssignments().add(assignment2);
		// set dueDate to 1 week before now.
		assignment2.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
		assignment2.setId(321);
		assignment2.setName("Assignment 321");
		assignment2.setNeedsGrading(1);

		// create assignment grade entry in mock repo
		AssignmentGrade ag = new AssignmentGrade();
		ag.setAssignment(assignment2);
		ag.setId(321);
		ag.setScore("");
		ag.setStudentEnrollment(enrollment);
		
		//given(enrollmentRepository.findById(TEST_COURSE_ID)).willReturn(Optional.of(enrollment));
		given(assignmentRepository.findById(321)).willReturn(Optional.of(assignment2));
		given(assignmentGradeRepository.findById(321)).willReturn(Optional.of(ag));
		
		AssignmentDTO assignmentDTO2 = new AssignmentDTO(assignment2.getId(), course.getCourse_id(), 
				"Assignment 321", "2023-02-28", course.getTitle());
		
		// send updates to server
		MockHttpServletResponse result2 = mvc
				.perform(MockMvcRequestBuilders.delete("/course/99999/deleteAssignment")
				.content(asJsonString(assignmentDTO2)).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		
		// verify we failed to delete the object
		assertEquals(409, result2.getStatus());
	}
}
