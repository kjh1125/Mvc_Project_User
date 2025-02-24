package kr.bit.controller;

import kr.bit.entity.Point;
import kr.bit.entity.UserProfile;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class RestUserController {

    @Value("${upload.path}")
    private String uploadDir;


    private final UserService userService;

    public RestUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/changeProfile/{profileImageId}")
    public String changeProfile(@PathVariable("profileImageId") int profileImageId, HttpSession session) {
        int userId = (int)session.getAttribute("user");
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        userProfile.setProfileImageId(profileImageId);
        userService.updateProfileImage(userProfile);

        return "success";
    }

    @PostMapping("/charge")
    public String charge(@RequestParam("points") int points,
            HttpSession session) {
        int userId = (int)session.getAttribute("user");
        Point point = new Point();
        point.setPoints(points);
        point.setUserId(userId);
        userService.setPoints(point);
        return "success";
    }

    @PostMapping("/change")
    public String changeUserProfile(@RequestParam("type") String type,
                                    @RequestParam("value") String value,
                                    HttpSession session) {
        int userId = (int) session.getAttribute("user");
        System.out.println("ㅎㅇ");
        userService.changeUserProfile(type, value, userId);
        return "success";
    }

    @PostMapping("/changeHobby")
    public String changeHobby(@RequestParam("data") String data, HttpSession session) {
        int userId = (int)session.getAttribute("user");
        userService.updateUserHobby(data, userId);
        return "success";
    }

    @PostMapping("/changePhoto")
    private String changePhoto(@RequestParam("image") MultipartFile file, HttpSession session) throws IOException {
        int userId = (int) session.getAttribute("user");

        Path uploadPath = Paths.get(uploadDir, "user_photos");

        // 디렉토리가 존재하지 않으면 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = "";
        if(file.isEmpty()){
            fileName = "default.jpg";
        }
        else {
            fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        }
        Path path = Paths.get(uploadDir, "user_photos", fileName);

        // 파일을 지정된 디렉토리에 저장
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        userService.changeUserProfile("photo_image_url",fileName,userId);
        return "success";
    }

}
