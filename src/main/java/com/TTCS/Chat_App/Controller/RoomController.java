package com.TTCS.Chat_App.Controller;

import com.TTCS.Chat_App.Model.Message;
import com.TTCS.Chat_App.Model.Room;
import com.TTCS.Chat_App.Model.RoomMember;
import com.TTCS.Chat_App.Model.User;
import com.TTCS.Chat_App.Repository.MessageRepo;
import com.TTCS.Chat_App.Repository.RoomMemberRepo;
import com.TTCS.Chat_App.Repository.RoomRepo;
import com.TTCS.Chat_App.Repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {
    private final UserRepo userRepo;
    private final RoomRepo roomRepo;
    private final RoomMemberRepo roomMemberRepo;
    private final MessageRepo messageRepo;
    @GetMapping("/direct/create")
    public String createDirectRoom(HttpSession session, Model model, @RequestParam("receiver_id") String receiverId) {
        User sender = (User) session.getAttribute("loggedInUser");
        if (sender == null) {
            return "redirect:/login";
        }

        User receiver = userRepo.findById(receiverId).orElse(null);
        if (receiver == null) {
            return "redirect:/user/homepage";
        }

        Room existingRoom = roomMemberRepo.findExistingRoom(
                sender,
                receiver,
                Room.Type.DIRECT
        ).orElse(null);

        if (existingRoom != null) {
            return "redirect:/room/direct/" + existingRoom.getRoomId() + "/enter";
        }

        Room directRoom = new Room();
        directRoom.setType(Room.Type.DIRECT);
        roomRepo.save(directRoom);

        RoomMember sendingMember = new RoomMember();
        sendingMember.setUser(sender);
        sendingMember.setRoom(directRoom);
        sendingMember.setRoomName(receiver.getEmail());
        roomMemberRepo.save(sendingMember);

        RoomMember receivingMember = new RoomMember();
        receivingMember.setUser(receiver);
        receivingMember.setRoom(directRoom);
        receivingMember.setRoomName(sender.getEmail());
        roomMemberRepo.save(receivingMember);

        return "redirect:/room/direct/" + directRoom.getRoomId() + "/enter";
    }

    @GetMapping("/direct/{room_id}/enter")
    public String enterChatRoom(HttpSession session, Model model, @PathVariable("room_id") String roomId) {
        User sender = (User) session.getAttribute("loggedInUser");
        if (sender == null) {
            return "redirect:/login";
        }

        Room room = roomRepo.findById(roomId).orElse(null);
        if (room == null) {
            return "redirect:/user/homepage/";
        }

        RoomMember sender_member = roomMemberRepo.findByUserAndRoom(sender, room).orElse(null);
        if (sender_member == null) {
            return "redirect:/user/homepage";
        }

        String roomName = sender_member.getRoomName();
        if (roomRepo.findById(roomId).isEmpty()) {
            return "redirect:/user/homepage";
        }

        TreeMap<LocalDate, List<Message>> preMessages = new TreeMap<>();
        for (Message msg : messageRepo.findByRoom_RoomIdOrderByCreatedAtAsc(room.getRoomId())) {
            preMessages.computeIfAbsent(msg.getCreatedAt().toLocalDate(), k -> new ArrayList<>()).add(msg);
        }

        model.addAttribute("preMessages", preMessages);
        model.addAttribute("sender", sender);
        model.addAttribute("room_id", roomId);
        model.addAttribute("room_name", roomName);
        return "direct-chat-room";
    }

    @PostMapping("/group/create")
    public String createGroup(HttpSession session, Model model, @RequestParam("group_name") String roomName) {
        User host = (User) session.getAttribute("loggedInUser");
        if (host == null ) {
            return "redirect:/login";
        }

        Room room = new Room();
        room.setType(Room.Type.GROUP);
        room.setRoomName(roomName);
        roomRepo.save(room);

        RoomMember hostMember = new RoomMember();
        hostMember.setRoomName(roomName);
        hostMember.setUser(host);
        hostMember.setRoom(room);
        roomMemberRepo.save(hostMember);

        return "redirect:/room/group/" + room.getRoomId() + "/enter";
    }

    @GetMapping("/group/{group_id}/enter")
    public String enterGroup(HttpSession session, Model model, @PathVariable("group_id") String roomId) {
        User sender = (User) session.getAttribute("loggedInUser");
        if (sender == null) {
            return "redirect:/login";
        }

        Room group = roomRepo.findById(roomId).orElse(null);
        if (group == null) {
            return "redirect:/user/homepage/groups";
        }

        int memberCount = roomMemberRepo.findByRoom(group).size();
        TreeMap<LocalDate, List<Message>> preMessages = new TreeMap<>();
        for (Message msg : messageRepo.findByRoom_RoomIdOrderByCreatedAtAsc(group.getRoomId())) {
            preMessages.computeIfAbsent(msg.getCreatedAt().toLocalDate(), k -> new ArrayList<>()).add(msg);
        }

        model.addAttribute("member_count", memberCount);
        model.addAttribute("pre_messages", preMessages);
        model.addAttribute("sender", sender);
        model.addAttribute("room_name", group.getRoomName());
        model.addAttribute("room_id", roomId);
        return "group-chat-room";
    }

    @GetMapping("/group/create")
    public String openGroupCreatingPage(HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        return "create-group";
    }

    @PostMapping("/group/{group_id}/leave")
    public String leaveGroup(HttpSession session, @PathVariable("group_id") String roomId) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Room group = roomRepo.findById(roomId).orElse(null);
        if (group != null) {
            roomMemberRepo.findByUserAndRoom(currentUser, group).ifPresent(roomMemberRepo::delete);
        }


        return "redirect:/user/homepage/groups";
    }

    @PostMapping("/group/{group_id}/add_member")
    public String showMemberSearchBox(RedirectAttributes redirectAttributes, HttpSession session, @RequestParam("newMemberEmail") String newMemberEmail, @PathVariable("group_id") String roomId) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Room room = roomRepo.findById(roomId).orElse(null);
        if (room == null) {
            return "redirect:/user/homepage/groups";
        }

        User newMember = userRepo.findByEmail(newMemberEmail).orElse(null);
        if (newMember == null || newMember.getRole() == User.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("error_msg", "Không tìm thấy người dùng, vui lòng nhập lại!");
            return "redirect:/room/group/" + room.getRoomId() + "/enter";
        }

        if (roomMemberRepo.findByUserAndRoom(newMember, room).isPresent()) {
            redirectAttributes.addFlashAttribute("error_msg", "Người dùng đã tồn tại trong nhóm");
            return "redirect:/room/group/" + room.getRoomId() + "/enter";
        }

        RoomMember newRoomMember = new RoomMember();
        newRoomMember.setRoom(room);
        newRoomMember.setUser(newMember);
        newRoomMember.setRoomName(room.getRoomName());
        roomMemberRepo.save(newRoomMember);

        redirectAttributes.addFlashAttribute("success_msg", "Thêm thành viên thành công!");
        return "redirect:/room/group/" + room.getRoomId() + "/enter";
    }

    @GetMapping("/group/{group_id}/view_member")
    public String openMemberList(HttpSession session, @PathVariable("group_id") String roomId, Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Room room = roomRepo.findById(roomId).orElse(null);
        if (room == null) {
            return "redirect:/user/homepage/groups";
        }

        List<RoomMember> roomMemberList = roomMemberRepo.findByRoom(room);
        model.addAttribute("room_id", roomId);
        model.addAttribute("current_user", currentUser);
        model.addAttribute("member_list", roomMemberList);
        return "view-group-member";
    }
}
