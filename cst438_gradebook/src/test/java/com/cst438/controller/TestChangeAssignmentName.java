package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static com.cst438.test.utils.TestUtils.fromJsonString;
import static com.cst438.test.utils.TestUtils.asJsonString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import com.cst438.dto.MultiplyProblem;
import com.cst438.dto.MultiplyResult;
import com.cst438.service.Assignment;
import com.cst438.service.MultiplyChecker;
import com.cst438.service.MultiplyHistoryTestImpl;
import com.cst438.service.RandomProblem;

import static org.mockito.BDDMockito.given;

@ContextConfiguration(classes = {Assignment.class, Course.class})
@WebMvcTest
public class TestChangeAssignmentName {
	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private MultiplyChecker checker;
	private Assignment assignment
	

	@Test
	public void testGetProblem() throws Exception {
		// verify that factors are between 11 and 99
		for (int i=0; i<1000; i++) {
			MockHttpServletResponse response = mvc.perform(
					get("/multiplication/new").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
			assertEquals(200, response.getStatus());
			MultiplyProblem mp = fromJsonString(response.getContentAsString(), MultiplyProblem.class);
			assertTrue(mp.factorA>=11 && mp.factorA<=99);
			assertTrue(mp.factorB>=11 && mp.factorB<=99);
		}
	}
}
