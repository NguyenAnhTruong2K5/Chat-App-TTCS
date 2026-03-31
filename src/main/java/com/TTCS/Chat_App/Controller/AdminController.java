package com.TTCS.Chat_App.Controller;

import com.TTCS.Chat_App.Model.Report;
import com.TTCS.Chat_App.Model.User;
import com.TTCS.Chat_App.Repository.ReportRepo;
import com.TTCS.Chat_App.Repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserRepo userRepo;
    private final ReportRepo reportRepo;
    @GetMapping("/homepage")
    public String openAdminHomepage(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return "redirect:/login";
        }

        long userCount = userRepo.count();
        long reportCount = reportRepo.count();

        model.addAttribute("user_count", userCount);
        model.addAttribute("report_count", reportCount);
        return "admin-homepage";
    }

    @GetMapping("/manage_user")
    public String openManageUserPage(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return "redirect:/login";
        }

        List<User> userList = userRepo.findAll();
        model.addAttribute("user_list", userList);
        return "manage-user";
    }

    @GetMapping("/view_report")
    public String openViewReportPage(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return "redirect:/login";
        }

        List<Report> reportList = reportRepo.findAll();
        model.addAttribute("report_list", reportList);
        return "view-report-list";
    }

    @GetMapping("/manage_user/set_status")
    public String setUserStatus(RedirectAttributes model, HttpSession session, @RequestParam("user_id") String userId) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return "redirect:/login";
        }

        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            model.addFlashAttribute("error_msg", "Người dùng không tồn tại!");
            return "redirect:/admin/manage_user";
        }

        if (user.getStatus() == User.Status.ALLOWED) {
            user.setStatus(User.Status.BANNED);
        } else {
            user.setStatus(User.Status.ALLOWED);
        }

        userRepo.save(user);
        return "redirect:/admin/manage_user";
    }

    @GetMapping("/view_report/view")
    public String viewReport(HttpSession session, @RequestParam("report_id") String reportId, Model model) {
        Report report = reportRepo.findById(reportId).orElse(null);
        model.addAttribute("report", report);
        return "view-report";
    }
}
