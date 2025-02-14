package kr.bit.controller;

import kr.bit.entity.Board;
import kr.bit.entity.Comment;
import kr.bit.service.BoardService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {
    @Autowired
    BoardService boardService;

    // 현재 Timestamp 생성
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    // Timestamp → LocalDateTime → String 변환 (소수점 제거)
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedTime = timestamp.toLocalDateTime().format(formatter);

    @GetMapping(value = "/list")
    public String boardList(Model model){
        List<Board> boards = boardService.getBoards();

        model.addAttribute("boards",boards);

        return "board/boardList";
    }

    @GetMapping(value = "/detail")
    public String boardDetail(Model model, @RequestParam("id") int id){
        //샘플 게시글 생성
        Board board = boardService.getBoard(id);

        List<Comment> commentList = new ArrayList<>();
        Comment comment1 = new Comment(1, 2, 3, "댓글 내용", formattedTime,"치킨1");
        Comment comment2 = new Comment(1, 2, 3, "댓글 내용", formattedTime,"치킨2");

        commentList.add(comment1);
        commentList.add(comment2);
        commentList.add(comment2);
        commentList.add(comment2);
        commentList.add(comment2);

        model.addAttribute("board",board);
        model.addAttribute("commentList",commentList);

        return "board/boardDetail";
    }

    @GetMapping(value = "/add")
    public String boardAdd(Model model){
        model.addAttribute("board", new Board());
        return "board/boardAdd";
    }

    @PostMapping("/add")
    public String addBoard(@ModelAttribute Board board) {

        boardService.insertBoard(board);
        return "redirect:/board/list";
    }

}
