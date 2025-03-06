package kr.bit.dao;

import kr.bit.entity.Board;
import kr.bit.entity.Comment;
import kr.bit.entity.Like;
import kr.bit.entity.ReportContent;
import kr.bit.mapper.BoardMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BoardDao {
    @Autowired
    private BoardMapper boardMapper;

    public void insertBoard(Board board){
        //System.out.println(board);
        boardMapper.insertBoard(board);
    }

    public List<Board> getBoards(int limit,int offset){
        return boardMapper.getBoards(limit, offset);
    }

    public Board getBoard(int id){
        return boardMapper.getBoard(id);
    }

    public int checkLikeExists(Like like){return boardMapper.checkLikeExists(like);}

    public void insertLike(Like like){
        boardMapper.insertLike(like);
    }

    public void deleteLike(Like like){
        boardMapper.deleteLike(like);
    }

    public int getHeartCount(int board_id){
        return boardMapper.getHeartCount(board_id);
    }

    public int checkReportExists(ReportContent reportContent){
        return boardMapper.checkReportExists(reportContent);
    }

    public void insertReportContent(ReportContent reportContent){
        boardMapper.insertReportContent(reportContent);
    }

    public void deleteContent(int board_id){
        boardMapper.deleteContent(board_id);
    }

    public void updateContent(Board board){
        boardMapper.updateContent(board);
    }

    public void insertComment(Comment comment){
        boardMapper.insertComment(comment);
    }
    public void deleteComment(int id){
        boardMapper.deleteComment(id);
    }
    public List<Comment> getComments(int board_id){
        return boardMapper.getComments(board_id);
    }

    public int getImage(int writerId) {
        return boardMapper.getImage(writerId);
    }
}
