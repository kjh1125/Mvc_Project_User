package kr.bit.service;

import kr.bit.entity.Board;
import kr.bit.dao.BoardDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public class BoardService {
    @Autowired
    private BoardDao boardDao;
    @Autowired
    private HttpSession session;

    public void insertBoard(Board board){
        int userId = (int)session.getAttribute("user");
        board.setWriter_id(userId); //로그인 된(session) 아이디를 writer_id로 세팅.
        boardDao.insertBoard(board);
    }

}
