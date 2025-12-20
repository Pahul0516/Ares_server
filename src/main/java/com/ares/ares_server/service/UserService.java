package com.ares.ares_server.service;

import com.ares.ares_server.domain.Run;
import com.ares.ares_server.domain.User;
import com.ares.ares_server.domain.Zone;
import com.ares.ares_server.dto.RunnerDTO;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.dto.UserStatsDTO;
import com.ares.ares_server.dto.mappers.UserMapper;
import com.ares.ares_server.exceptions.UserDoesNotExistsException;
import com.ares.ares_server.repository.RunRepository;
import com.ares.ares_server.repository.UserRepository;
import com.ares.ares_server.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RunRepository runRepository;
    private final ZoneRepository zoneRepository;

    @Transactional
    public void deleteUser(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new UserDoesNotExistsException("User with email " + email + " does not exist!");
        }
        userRepository.deleteByEmail(email);
    }

    public UserDTO updateUser(String email, UserDTO updatedUserDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserDoesNotExistsException(
                        "User with email " + email + " does not exist!"
                ));

        user.setEmail(updatedUserDto.getEmail());
        user.setUsername(updatedUserDto.getUsername());
        user.setEncryptedPassword(updatedUserDto.getEncryptedPassword());
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public UserDTO getUserByEmail(String email) {
        return userMapper.toDto(
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new UserDoesNotExistsException(
                                "User with email " + email + " does not exist"
                        ))
        );
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserStatsDTO getUserStats(String email) {
        UserStatsDTO userStatsDTO = new UserStatsDTO();
        UserDTO user =  getUserByEmail(email);
        List<Run> runs = runRepository.findByOwnerId(user.getId());
        double totalArea = 0;
        int totalDuration = 0;
        int totalDistance = 0;
        for (Run run : runs) {
            totalDistance+=run.getDistance();
            totalDuration+=run.getDuration();
        }
        List<Zone> zones = zoneRepository.findByOwnerId(user.getId());
        for (Zone zone : zones) {
            totalArea+=zone.getArea();
        }
        userStatsDTO.setTimeRunning(totalDuration);
        userStatsDTO.setTotalArea(totalArea);
        userStatsDTO.setTotalDistance(totalDistance);
        return userStatsDTO;
    }

    public List<RunnerDTO> getTopTenRunners() {
        return getAllUsers().stream()
                .map(user -> {
                    RunnerDTO runnerDTO = new RunnerDTO();
                    runnerDTO.setUsername(user.getUsername());
                    List<Zone> zones = zoneRepository.findByOwnerId(user.getId());
                    Double totalDistance = zones.stream().mapToDouble(Zone::getArea).sum();
                    runnerDTO.setScore(totalDistance);
                    return runnerDTO;
                })
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .limit(10)
                .toList();
    }
}
