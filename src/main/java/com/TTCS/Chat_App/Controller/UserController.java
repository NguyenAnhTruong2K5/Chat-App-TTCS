package com.TTCS.Chat_App.Controller;

import com.TTCS.Chat_App.DTO.ChatHistoryDTO;
import com.TTCS.Chat_App.DTO.InboxMessageDTO;
import com.TTCS.Chat_App.DTO.ReportDTO;
import com.TTCS.Chat_App.Model.*;
import com.TTCS.Chat_App.Repository.MessageRepo;
import com.TTCS.Chat_App.Repository.ReportRepo;
import com.TTCS.Chat_App.Repository.RoomMemberRepo;
import com.TTCS.Chat_App.Repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepo userRepo;
    private final MessageRepo messageRepo;
    private final RoomMemberRepo roomMemberRepo;
    private final ReportRepo reportRepo;

    @GetMapping("/homepage")
    public String viewUserHomepage(HttpSession httpSession, Model model) {
        User currentUser = (User) httpSession.getAttribute("loggedInUser");
        if (currentUser == null || currentUser.getStatus() == User.Status.BANNED) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "user-homepage";
    }

    @GetMapping("/homepage/search")
    public String userSearching(@RequestParam("search_query") String searchQuery, Model model, HttpSession session) {
        User currUser = (User) session.getAttribute("loggedInUser");
        if (currUser == null || currUser.getStatus() == User.Status.BANNED) {
            return "redirect:/login";
        }

        if (searchQuery == null) {
            model.addAttribute("error_msg", "Vui lòng nhập thông tin tìm kiếm");
            return "show-search-result";
        }

        String keyWord = searchQuery.trim().toLowerCase();

        if (currUser.getEmail().equals(keyWord)) {
            model.addAttribute("error_msg", "Email không được trùng với email của người dùng hiện tại!");
            return "show-search-result";
        }

        User user = userRepo.findByEmail(keyWord).orElse(null);

        if (user == null) {
            model.addAttribute("error_msg", "Không tìm thấy người dùng!");
            return "show-search-result";
        }

        model.addAttribute("found_user", user);
        return "show-search-result";
    }

    @GetMapping("/homepage/inbox")
    public String viewInbox(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getStatus() == User.Status.BANNED) {
            return "redirect:/login";
        }

        List<RoomMember> rooms = roomMemberRepo.findByUser(user);
        List<InboxMessageDTO> newMessages = new ArrayList<>();
        for (RoomMember roomMember : rooms) {
            Message message = messageRepo.findTopByRoom_RoomIdOrderByCreatedAtDesc(roomMember.getRoom().getRoomId()).orElse(null);
            if (message == null || message.getUser() == user) {
                continue;
            }

            InboxMessageDTO inboxMessage = new InboxMessageDTO();
            inboxMessage.setContent(message.getContent());
            inboxMessage.setSenderEmail(message.getUser().getEmail());
            inboxMessage.setRoomId(message.getRoom().getRoomId());

            RoomMember currMember = roomMemberRepo.findByUserAndRoom(user, message.getRoom()).orElse(null);
            if (currMember != null) {
                inboxMessage.setRoomName(currMember.getRoomName());
            }
            newMessages.add(inboxMessage);
        }

        model.addAttribute("new_messages", newMessages);
        return "inbox";
    }

    @GetMapping("/homepage/profile")
    public String viewProfile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getStatus() == User.Status.BANNED) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/homepage/chat_history")
    public String viewChatHistory(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getStatus() == User.Status.BANNED) {
            return "redirect:/login";
        }
        List<ChatHistoryDTO> chatHistories = new ArrayList<>();
        List<RoomMember> rooms = roomMemberRepo.findByUser(user);

        for (RoomMember roomMember : rooms) {
            Message latestMessage = messageRepo.findTopByRoom_RoomIdOrderByCreatedAtDesc(roomMember.getRoom().getRoomId()).orElse(null);
            if (latestMessage != null) {
                ChatHistoryDTO chatHistoryDTO = new ChatHistoryDTO();
                chatHistoryDTO.setType(roomMember.getRoom().getType());
                chatHistoryDTO.setRoomId(roomMember.getRoom().getRoomId());
                chatHistoryDTO.setLatestMessage(latestMessage.getContent());
                chatHistoryDTO.setSenderEmail(latestMessage.getUser().getEmail());
                chatHistoryDTO.setRoomName(roomMember.getRoomName());
                chatHistories.add(chatHistoryDTO);
            }
        }

        model.addAttribute("chat_history", chatHistories);
        return "chat-history";
    }

    @GetMapping("/homepage/groups")
    public String openGroupPage(HttpSession session, Model model, @RequestParam(value="search_query", required=false) String roomName) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getStatus() == User.Status.BANNED) {
            return "redirect:/login";
        }

        List<RoomMember> roomMembers = roomName == null ? roomMemberRepo.findByUser(user) : roomMemberRepo.findByUserAndRoomNameContainingIgnoreCase(user, roomName);
        List<Room> userGroups = new ArrayList<>();

        for (RoomMember rm : roomMembers) {
            if (rm.getRoom().getType() == Room.Type.GROUP) {
                userGroups.add(rm.getRoom());
            }
        }

        model.addAttribute("search_query", roomName);
        model.addAttribute("groups", userGroups);
        return "user-groups";
    }

    @GetMapping("/report")
    public String openReportPage(Model model, HttpSession session, @RequestParam("reported_user_id") String reportedUserId) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getStatus() == User.Status.BANNED) {
            return "redirect:/login";
        }

        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setReporterId(user.getUserId());
        reportDTO.setReportedUserId(reportedUserId);

        model.addAttribute("reportDTO", reportDTO);
        return "report";
    }

    @PostMapping("/report")
    public String sendReport(RedirectAttributes model, HttpSession session, @ModelAttribute("reportDTO") ReportDTO request) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getStatus() == User.Status.BANNED) {
            return "redirect:/login";
        }

        String reportedUserId = request.getReportedUserId();
        String content = request.getContent();

        User reportedUser = userRepo.findById(reportedUserId).orElse(null);
        if (reportedUser == null) {
            model.addFlashAttribute("error_msg", "Không tìm thấy người dùng!");
            return "redirect:/user/homepage";
        }

        Report report = new Report();
        report.setReporter(user);
        report.setReportedUser(reportedUser);
        report.setContent(content);
        reportRepo.save(report);

        model.addFlashAttribute("success_msg", "Đã gửi báo cáo!");
        return "redirect:/user/homepage";
    }
}
