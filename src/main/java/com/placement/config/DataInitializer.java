//package com.placement.config;
//
//import com.placement.entity.Student;
//import com.placement.entity.User;
//import com.placement.repository.StudentRepository;
//import com.placement.repository.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@RequiredArgsConstructor
//public class DataInitializer implements CommandLineRunner {
//    private final UserRepository userRepository;
//    private final StudentRepository studentRepository;
//    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) {
//        userRepository.findByUsername("admin").orElseGet(() -> userRepository.save(User.builder()
//            .username("admin")
//            .password(passwordEncoder.encode("admin123"))
//            .role(User.Role.ADMIN).build()));
//
//        User studentUser = userRepository.findByUsername("student").orElseGet(() -> userRepository.save(User.builder()
//            .username("student")
//            .password(passwordEncoder.encode("student123"))
//            .role(User.Role.STUDENT).build()));
//
//        if (studentUser.getStudent() == null) {
//            Student student = Student.builder()
//                .name("Demo Student")
//                .email("student@college.com")
//                .phone("9999999999")
//                .department("CSE")
//                .batch("2026")
//                .cgpa(8.2)
//                .backlogs(0)
//                .skills("Java, SQL, JavaScript")
//                .user(studentUser)
//                .build();
//            studentRepository.save(student);
//            studentUser.setStudent(student);
//            userRepository.save(studentUser);
//        }
//    }
//}
