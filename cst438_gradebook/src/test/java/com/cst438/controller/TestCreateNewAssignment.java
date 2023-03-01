package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static com.cst438.test.utils.TestUtils.fromJsonString;
import static com.cst438.test.utils.TestUtils.asJsonString;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Date;
import java.util.Optional;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentListDTO.AssignmentDTO;
import com.cst438.services.RegistrationService;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;

import static org.mockito.BDDMockito.given;

@ContextConfiguration(classes = {Assignment.class, Course.class})
@WebMvcTest
public class TestCreateNewAssignment {
	
	static final String URL = "http://localhost:8080";
	public static final int TEST_COURSE_ID = 40442;
	public static final String TEST_COURSE_TITLE = "Test Course";
	public static final String TEST_STUDENT_EMAIL = "test@csumb.edu";
	public static final String TEST_STUDENT_NAME = "test";
	public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
	public static final int TEST_YEAR = 2021;
	public static final String TEST_SEMESTER = "Fall";
	public static final String TEST_NAME = "Assignment 2";
	
	@MockBean
	CourseRepository courseRepository;
	
	@MockBean
	AssignmentRepository assignmentRepository;
	
	@Autowired
	private MockMvc mvc;

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
		
		Enrollment enrollment = new Enrollment();
		enrollment.setCourse(course);
		course.getEnrollments().add(enrollment);
		enrollment.setId(TEST_COURSE_ID);
		enrollment.setStudentEmail(TEST_STUDENT_EMAIL);
		enrollment.setStudentName(TEST_STUDENT_NAME);
		
		assertEquals(null, courseRepository.findById(course.getCourse_id()).orElse(null));
		given(courseRepository.findById(99999)).willReturn(Optional.of(course));
		// this line fails since object is now in mock DB
		// assertEquals(null, courseRepository.findById(testCourse.getCourse_id()).orElse(null)); 
		
		// Use AssignmentDTO to transfer objects to the endpoint
		//	Create new assignment object that is sent to the server
		AssignmentDTO newAssignment = new AssignmentDTO(10, course.getCourse_id(), "Assignment", "2023-02-28", "CST 438");
		
		// send updates to server
		MockHttpServletResponse result = mvc
				.perform(MockMvcRequestBuilders.post("/course/99999/createAssignment")
				.content(asJsonString(newAssignment)).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		
		// veify we successfully sent the object to the server
		assertEquals(200, result.getStatus());
	}
}


//.perform(MockMvcRequestBuilders
//.get("/course/99999/createAssignment").accept(MediaType.APPLICATION_JSON)
//.content(asJsonString(newAssignment)).contentType(MediaType.APPLICATION_JSON))
//.andReturn().getResponse();