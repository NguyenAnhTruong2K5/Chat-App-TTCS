package com.TTCS.Chat_App.Controller;

import com.TTCS.Chat_App.DTO.ChatHistoryDTO;
import com.TTCS.Chat_App.Model.Message;
import com.TTCS.Chat_App.Model.Room;
import com.TTCS.Chat_App.Model.RoomMember;
import com.TTCS.Chat_App.Model.User;
import com.TTCS.Chat_App.Repository.MessageRepo;
import com.TTCS.Chat_App.Repository.RoomMemberRepo;
import com.TTCS.Chat_App.Repository.RoomRepo;
import com.TTCS.Chat_App.Repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {
    private final UserRepo userRepo;
    private final RoomRepo roomRepo;
    private final RoomMemberRepo roomMemberRepo;
    private final MessageRepo messageRepo;
    @GetMapping("/direct/create")
    public String openChatPage(HttpSession session, Model model, @RequestParam("receiver_id") String receiverId) {
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
            model.addAttribute("room_id", existingRoom.getRoomId());
            model.addAttribute("sender", sender);
            model.addAttribute("receiver", receiver);
            List<Message> preMessages = messageRepo.findByRoom_RoomIdOrderByCreatedAtAsc(existingRoom.getRoomId());
            model.addAttribute("chat_history", preMessages);
            return "direct-chat-room";
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

        model.addAttribute("room_id", directRoom.getRoomId());
        model.addAttribute("sender", sender);
        model.addAttribute("receiver", receiver);

        return "direct-chat-room";
    }

    @GetMapping("/direct/enter")
    public String enterChatRoom(HttpSession session, @RequestParam("room_id") String roomId, Model model) {
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
            return "redirect:/user/homepage/";
        }

        String roomName = sender_member.getRoomName();
        if (roomRepo.findById(roomId).isEmpty()) {
            model.addAttribute("error_msg", "Không tìm thấy phòng chat!");
            return "/user/homepage";
        }

        List<Message> preMessages = messageRepo.findByRoom_RoomIdOrderByCreatedAtAsc(roomId);

        model.addAttribute("chat_history", preMessages);
        model.addAttribute("sender", sender);
        model.addAttribute("room_id", roomId);
        model.addAttribute("room_name", roomName);
        return "direct-chat-room";
    }

    @PostMapping("/group/create")
    public String createGroup(HttpSession session, Model model, @RequestParam("group_name") String roomName) {
        User host = (User) session.getAttribute("loggedInUser");
        if (host == null) {
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

        return "redirect:/room/group/enter?room_id=" + room.getRoomId();
    }

    @GetMapping("/group/enter")
    public String enterGroup(HttpSession session, Model model, @RequestParam("room_id") String roomId) {
        User sender = (User) session.getAttribute("loggedInUser");
        if (sender == null) {
            return "redirect:/login";
        }

        Room group = roomRepo.findById(roomId).orElse(null);
        if (group == null) {
            return "redirect:/user/homepage/groups";
        }

        List<Message> preMessages = messageRepo.findByRoom_RoomIdOrderByCreatedAtAsc(roomId);

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

    @GetMapping("/group/search")
    public String searchGroup(HttpSession session, @RequestParam("search_query") String groupName, Model model) {
        return "user-groups";
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
    public String showMemberSearchBox(Model model, HttpSession session, @RequestParam("newMemberEmail") String newMemberEmail, @PathVariable("group_id") String roomId) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Room room = roomRepo.findById(roomId).orElse(null);
        if (room == null) {
            return "redirect:/user/homepage/groups";
        }

        User newMember = userRepo.findByEmail(newMemberEmail).orElse(null);
        if (newMember == null) {
            model.addAttribute("error_msg", "Không tìm thấy người dùng, vui lòng nhập lại!");
            model.addAttribute("room_id", roomId);
            model.addAttribute("room_name", room.getRoomName());
            model.addAttribute("sender", currentUser);
            return "group-chat-room";
        }

        if (roomMemberRepo.findByUserAndRoom(newMember, room).isPresent()) {
            model.addAttribute("error_msg", "Người dùng đã tồn tại trong nhóm");
            model.addAttribute("room_id", roomId);
            model.addAttribute("room_name", room.getRoomName());
            model.addAttribute("sender", currentUser);
            return "group-chat-room";
        }

        RoomMember newRoomMember = new RoomMember();
        newRoomMember.setRoom(room);
        newRoomMember.setUser(newMember);
        newRoomMember.setRoomName(room.getRoomName());
        roomMemberRepo.save(newRoomMember);

        model.addAttribute("success_msg", "Thêm thành viên thành công!");
        model.addAttribute("room_id", roomId);
        model.addAttribute("room_name", room.getRoomName());
        model.addAttribute("sender", currentUser);
        return "group-chat-room";
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
        model.addAttribute("curr_user", currentUser);
        model.addAttribute("member_list", roomMemberList);
        return "view-group-member";
    }
}
