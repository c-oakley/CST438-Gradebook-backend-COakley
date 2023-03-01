package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.sql.Date;

import static com.cst438.test.utils.TestUtils.fromJsonString;
import static com.cst438.test.utils.TestUtils.asJsonString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;

import static org.mockito.BDDMockito.given;

@ContextConfiguration(classes = {Assignment.class, Course.class})
@WebMvcTest
public class TestChangeAssignmentName {
	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private CourseRepository courseRepository;
	
	@MockBean
	private AssignmentRepository assignmentRepository;

	@Test
	public void testGetProblem() throws Exception {
		// create course for mock		
		Course course = new Course();
		course.setCourse_id(99999);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setSemester("Fall");
		course.setYear(2021);
		course.setTitle(TEST_COURSE_TITLE);
		assertEquals(TEST_COURSE_TITLE, course.getTitle());
		Assignment newAssignment = new Assignment();
		newAssignment.setName("Assignment 2");
		newAssignment.setDueDate(new Date(2282023));
		MockHttpServletResponse createAssignment = mvc.perform(
				post("/course/123456/createAssignment").content(asJsonString(newAssignment)).contentType(
						MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		assertEquals(200, createAssignment.getStatus());
		
		// UPDATE ASSIGNMENT NAME
		int id = newAssignment.getId();
		MockHttpServletResponse renameAssignment = mvc.perform(
				put("/course/123456/updateAssignmentName?assignmentID=" + id + "&name=Assignment 2").accept(
						MediaType.APPLICATION_JSON)).andReturn().getResponse();
		assertEquals(200, renameAssignment.getStatus());
	}
}
