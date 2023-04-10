package com.cst438.services;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Course;
import com.cst438.domain.CourseDTOG;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.MultiplyProblem;
import com.cst438.dto.MultiplyResult;
import com.cst438.dto.v2.MultiplyResultV2;
import com.cst438.dto.v2.UserDTO;


public class RegistrationServiceMQ extends RegistrationService {

	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public RegistrationServiceMQ() {
		System.out.println("MQ registration service ");
	}

	// ----- configuration of message queues

	@Autowired
	Queue registrationQueue;


	// ----- end of configuration of message queue

	// receiver of messages from Registration service
	@RabbitListener(queues = "gradebook-queue") // listens for messages from the Gradebook service
	@Transactional
	public void receive(EnrollmentDTO enrollmentDTO) {
		// check if the course is valid before continuing
		Course courseToEnrollIn = courseRepository.findById(enrollmentDTO.course_id).orElse(null);
		if (courseToEnrollIn == null) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course ID not found in the database."); }
		
		// create new Enrollment object and fill in the values
		Enrollment newEnrollmentObj = new Enrollment();
		// fill Enrollment record fields
		newEnrollmentObj.setStudentName(enrollmentDTO.studentName);
		newEnrollmentObj.setStudentEmail(enrollmentDTO.studentEmail);
		// set the course relationship
		newEnrollmentObj.setCourse(courseToEnrollIn);
		
		// save the new object in the database
		enrollmentRepository.save(newEnrollmentObj);
	}

	// sender of messages to Registration Service
	@Override
	public void sendFinalGrades(int course_id, CourseDTOG courseDTO) { // sends messages to the Gradebook service
		System.out.println("Sending final grades " + course_id + " " + courseDTO);
		
		// use the rabbitTemplate
		// send the attempt as message to LEVEL service 
		System.out.println("Sending rabbitmq message to the Gradebook service: " + courseDTO);
		// use template to send
		rabbitTemplate.convertAndSend(registrationQueue.getName(), courseDTO);
		System.out.println("Final grades have been sent");
	}
}

/*
@PostMapping("/v2/result")
	public MultiplyResultV2 checkV2(@RequestBody MultiplyProblem mp) {
		System.out.println("check called");
		MultiplyResult mr = checker.check(mp);
		if (mp.alias != null && mp.alias.trim().length() != 0) {
			mr = history.save(mr);
		}

		// send the attempt as message to LEVEL service 
		System.out.println("Sending rabbitmq message: "+ mr);
		rabbitTemplate.convertAndSend(queue.getName(), mr);
		System.out.println("Message sent.");

		// get user level from LEVEL service.   Level service returns a String value.
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<UserDTO> response = restTemplate.getForEntity("http://localhost:8081/v1/user/" + mp.alias,
				UserDTO.class);
		UserDTO userDTO = response.getBody();
		String level = userDTO.level;
		
		// create response to caller.
		MultiplyResultV2 mrv2 = new MultiplyResultV2(mr, level);
		return mrv2;
	}


*/
