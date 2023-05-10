package com.example.worthreadingservice.service;

import com.example.worthreadingservice.dto.UserDto;
import com.example.worthreadingservice.entity.MessageEntity;
import com.example.worthreadingservice.entity.UserEntity;
import com.example.worthreadingservice.repository.MessageRepository;
import com.example.worthreadingservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class LikeService {

    MessageRepository messageRepo;
    UserRepository userRepo;

    public LikeService(MessageRepository messageRepo, UserRepository userRepo) {
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
    }

    public void addLike(Long messageId, Long userId) {
        MessageEntity message = messageRepo.findById(messageId).orElse(null);
        if (message == null) {
            message = new MessageEntity();
            message.setId(messageId);
        }

        UserEntity user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            user = new UserEntity();
            user.setId(userId);
            userRepo.save(user);
        }

        message.getUserIds().add(user);
        messageRepo.save(message);
    }

    @Transactional
    public void removeLike(Long messageId, Long userId) {
        messageRepo.findById(messageId).ifPresent(m -> m.getUserIds().removeIf(user -> user.getId().equals(userId)));
    }

    public boolean isLiked(Long messageId, Long userId) {
        return messageRepo.findById(messageId).map(m -> m.getUserIds().stream().anyMatch(u -> u.getId().equals(userId))).orElse(false);
    }

    public int getAmountOfLikes(Long messageId) {
        return messageRepo.findById(messageId).map(m -> m.getUserIds().size()).orElse(0);
    }

    public List<UserDto> getUsersWhoLikeMessage(Long messageId) {
        return callUserApi(messageRepo.findById(messageId)
                .orElseThrow()
                .getUserIds()
                .stream()
                .map(UserEntity::getId)
                .map(Object::toString)
                .collect(Collectors.joining(",")));
    }

    private List<UserDto> callUserApi(String csv) {

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8006/users/" + csv;
        String jsonResponse = restTemplate.getForObject(url, String.class);
        return parseUserJson(jsonResponse);
    }

    private List<UserDto> parseUserJson(String jsonResponse) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<UserDto>>(){}.getType();
        return gson.fromJson(jsonResponse, listType);
    }

    public List<UserDto> getMessagesLikedByUser(Long userId) {
        //Todo: implement
        return new ArrayList<>();
    }
}
