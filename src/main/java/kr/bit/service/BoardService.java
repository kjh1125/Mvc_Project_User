package kr.bit.service;

import kr.bit.entity.Board;
import kr.bit.dao.BoardDao;
import kr.bit.entity.Like;
import kr.bit.entity.ReportContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;

@Service
public class BoardService {
    @Autowired
    private BoardDao boardDao;
    @Autowired
    private HttpSession session;
    @Autowired
    UserService userService;

    public void insertBoard(Board board){
        int userId = (int)session.getAttribute("user");
        board.setWriter_id(userId); //로그인 된(session) 아이디를 writer_id로 세팅.
        boardDao.insertBoard(board);
    }

    public List<Board> getBoards(){

        List<Board> boards = boardDao.getBoards();

        for(Board board : boards){
            board.setNickname(userService.getNickname(board.getWriter_id()));
        }

        return boards;
    }

    public Board getBoard(int id){
        Board board = boardDao.getBoard(id);
        board.setNickname(userService.getNickname(board.getWriter_id()));

        return board;
    }

    public boolean checkLikeExists(Like like){
        int count = boardDao.checkLikeExists(like);
        return count > 0; //0보다 클 경우 true 값 리턴
    }

    public void insertLike(Like like){
        boardDao.insertLike(like);
    }

    public void deleteLike(Like like){
        boardDao.deleteLike(like);
    }

    public int getHeartCount(int board_id){
        return boardDao.getHeartCount(board_id);
    }

    public boolean checkReportExists(ReportContent reportContent){
        int count = boardDao.checkReportExists(reportContent);
        return count > 0; //0보다 클 경우 true 값 리턴
    }

    public void insertReportContent(ReportContent reportContent){
        boardDao.insertReportContent(reportContent);
    }

    public void deleteContent(int board_id){
        boardDao.deleteContent(board_id);
    }

    public void updateContent(Board board){
        int userId = (int)session.getAttribute("user");
        board.setWriter_id(userId); //로그인 된(session) 아이디를 writer_id로 세팅.
        boardDao.updateContent(board);
    }

}
