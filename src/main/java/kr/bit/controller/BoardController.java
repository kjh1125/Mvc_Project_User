package kr.bit.controller;

import com.mysql.cj.Session;
import kr.bit.entity.Board;
import kr.bit.entity.Comment;
import kr.bit.entity.Like;
import kr.bit.entity.ReportContent;
import kr.bit.service.BoardService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/board")
public class BoardController {
    @Autowired
    BoardService boardService;
    @Autowired
    private HttpSession session;

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
        //게시물 불러오기
        Board board = boardService.getBoard(id);
        //좋아요 표시를 위한 확인
        Like like = new Like();
        like.setUser_id((int)session.getAttribute("user")); //로그인 사람의 id값
        like.setBoard_id(id); //게시물 id
        boolean checkLikeExists = boardService.checkLikeExists(like);

        //샘플 코멘트
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
        model.addAttribute("checkLikeExists",checkLikeExists);

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

    @PostMapping("/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> likePost(@RequestBody Like like) {

        // 좋아요 상태 확인
        boolean liked = boardService.checkLikeExists(like);
        
        if(!liked){ //중복 값(false)이 없을 경우
            //좋아요 테이블에 값 추가
            boardService.insertLike(like);
        }
        else{
            //좋아요 테이블에 값 제거
            boardService.deleteLike(like);
        }
        //좋아요 개수 확인
        int heartCount = boardService.getHeartCount(like.getBoard_id());
        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("heartCount", heartCount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/report")
    @ResponseBody
    public ResponseEntity<String> reportContent(@RequestBody ReportContent reportContent) {

        //신고 중복 확인 중복일 경우 true 값이 나옴
        boolean duplicated = boardService.checkReportExists(reportContent);

        if(duplicated){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("duplicate"); // 409 Conflict 응답
        }
        else {
            //중복이 아닐 경우 db에 값 추가
            boardService.insertReportContent(reportContent);
            return ResponseEntity.ok("success");
        }

    }

    @DeleteMapping("/delete")
    @ResponseBody
    public ResponseEntity<String> deleteContent(@RequestBody @RequestParam("board_id") int board_id) {

        //게시물 삭제, 본인만 삭제할 수 있도록 Front에서 제어
        boardService.deleteContent(board_id);

        return ResponseEntity.ok("success");
    }



}
