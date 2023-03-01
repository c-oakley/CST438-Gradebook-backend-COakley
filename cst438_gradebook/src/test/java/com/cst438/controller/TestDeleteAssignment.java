package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
public class TestDeleteAssignment {
	@Autowired
	private MockMvc mvc;
	
	/*@MockBean
	private CourseRepository courseRepository;
	
	@MockBean
	private AssignmentRepository assignmentRepository;*/

	@Test
	public void testCreateNewAssignment() throws Exception {
		// CREATE ASSIGNMENT OBJ
		Assignment newAssignment = new Assignment();
		newAssignment.setName("Assignment 2");
		newAssignment.setDueDate(new Date(2282023));
		
		MockHttpServletResponse createAssignment = mvc.perform(
				post("/course/123456/createAssignment").content(asJsonString(newAssignment)).contentType(
						MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		assertEquals(200, createAssignment.getStatus());
		
		// DELETE ASSIGNMENT OBJ
		int id = newAssignment.getId();
		MockHttpServletResponse deleteAssignment = mvc.perform(
				delete("course/123456/deleteAssignment?assignmentID=" + id).accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		assertEquals(200, deleteAssignment.getStatus());
	}
}
