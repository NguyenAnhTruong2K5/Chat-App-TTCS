package com.TTCS.Chat_App.Controller;

import com.TTCS.Chat_App.DTO.LoginUserDTO;
import com.TTCS.Chat_App.DTO.UserRegisterDTO;
import com.TTCS.Chat_App.Model.User;
import com.TTCS.Chat_App.Repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {
    private final UserRepo userRepo;

    @GetMapping("/login")
    public String viewLogin(Model model) {
        model.addAttribute("loginUserDTO", new LoginUserDTO());
        return "login";
    }

    @GetMapping("/register")
    public String viewRegister(Model model) {
        model.addAttribute("userRegisterDTO", new UserRegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(Model model, @ModelAttribute("userRegisterDTO") UserRegisterDTO request) {
        String email = request.getEmail();
        String username = request.getUsername();
        String password = request.getPassword();
        String bio = request.getBio();

        if (email == null || email.trim().isEmpty() || username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập đầy đủ các thông tin!");
            return "register";
        }

        if (userRepo.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Tài khoản email này đã tồn tại, vui lòng nhập email khác!");
            return "register";
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setBio(bio);
        newUser.setPassword(password);
        newUser.setUsername(username);

        userRepo.save(newUser);
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String userLogin(@ModelAttribute("loginUserDTO") LoginUserDTO request, Model model, HttpSession session) {
        String email = request.getEmail().trim();
        String password = request.getPassword().trim();

        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Tài khoản người dùng không tồn tại!");
            return "login";
        }

        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Mật khẩu không chính xác, vui lòng nhập lại");
            return "login";
        }

        if (!user.getStatus().equals(User.Status.ALLOWED)) {
            model.addAttribute("error", "Tài khoản người dùng đã bị khoá");
            return "login";
        }

        session.setAttribute("loggedInUser", user);
        if (user.getRole() == User.Role.ADMIN) {
            return "redirect:/admin/homepage";
        }

        return "redirect:/user/homepage";
    }

    @GetMapping("/logout")
    public String logOut(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
