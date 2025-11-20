//package com.ares.ares_server.Controllers;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(HelloController.class)
//public class HelloControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // Helper method to add a user
//    private void addUser(String name) throws Exception {
//        HelloController.User user = new HelloController.User(name);
//        mockMvc.perform(post("/api/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(user)))
//                .andExpect(status().isCreated());
//    }
//
//    @Test
//    public void testHelloEndpoint() throws Exception {
//        mockMvc.perform(get("/api/users/hello"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Hello World"));
//    }
//
//    @Test
//    public void testGreetEndpoint() throws Exception {
//        mockMvc.perform(get("/api/users/greet"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Greetings!"));
//    }
//
//    @Test
//    public void testAddUser() throws Exception {
//        HelloController.User user = new HelloController.User("Alice");
//        mockMvc.perform(post("/api/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(user)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.name", is("Alice")));
//    }
//
////    @Test
////    public void testGetAllUsers() throws Exception {
////        addUser("Alice");
////        addUser("Bob");
////
////        mockMvc.perform(get("/api/users"))
////                .andExpect(status().isOk())
////                .andExpect(jsonPath("$[0].name", is("Alice")))
////                .andExpect(jsonPath("$[1].name", is("Bob")));
////    }
//
////    @Test
////    public void testGetUserById() throws Exception {
////        addUser("Alice");
////
////        mockMvc.perform(get("/api/users/0"))
////                .andExpect(status().isOk())
////                .andExpect(jsonPath("$.name", is("Alice")));
////    }
//
//    @Test
//    public void testUpdateUser() throws Exception {
//        addUser("Alice");
//
//        HelloController.User updatedUser = new HelloController.User("Bob");
//        mockMvc.perform(put("/api/users/0")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updatedUser)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name", is("Bob")));
//    }
//
//    @Test
//    public void testDeleteUser() throws Exception {
//        addUser("Alice");
//
//        mockMvc.perform(delete("/api/users/0"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("User deleted: Alice"));
//    }
//
//    @Test
//    public void testGetUserNotFound() throws Exception {
//        mockMvc.perform(get("/api/users/999"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    public void testUpdateUserNotFound() throws Exception {
//        HelloController.User updatedUser = new HelloController.User("Bob");
//        mockMvc.perform(put("/api/users/999")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updatedUser)))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    public void testDeleteUserNotFound() throws Exception {
//        mockMvc.perform(delete("/api/users/999"))
//                .andExpect(status().isNotFound())
//                .andExpect(content().string("User not found"));
//    }
//}
