package kr.bit.mapper;

import kr.bit.entity.Board;
import kr.bit.entity.Like;
import kr.bit.entity.ReportContent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface BoardMapper {
    //게시물 추가
    @Insert("insert into boards(writer_id,title,content) values(#{writer_id},#{title},#{content})")
    void insertBoard(Board board);
    //모든 게시물 확인
    @Select("select * from boards order by id desc")
    List<Board> getBoards();
    //단일 게시물 확인
    @Select("select * from boards where id = #{id}")
    Board getBoard(int id);
    //좋아요 중복 확인
    @Select("select count(*) from likes where user_id =#{user_id} and board_id = #{board_id}")
    int checkLikeExists(Like like);
    //좋아요
    @Insert("insert into likes values(#{user_id},#{board_id})")
    void insertLike(Like like);
    //좋아요 취소
    @Delete("delete from likes where user_id =#{user_id} and board_id = #{board_id}")
    void deleteLike(Like like);
    //좋아요 개수 확인
    @Select("select heart_count from boards where id=#{board_id}")
    int getHeartCount(int board_id);
    //신고 중복 확인
    @Select("select count(*) from reportContent where user_id =#{user_id} and board_id = #{board_id}")
    int checkReportExists(ReportContent reportContent);
    //게시물 신고
    @Insert("insert into reportContent values(#{user_id},#{board_id},#{reason_id})")
    void insertReportContent(ReportContent reportContent);
    //게시물 삭제
    @Delete("delete from boards where id = #{board_id}")
    void deleteContent(int board_id);
    //게시물 수정
    @Update("update boards set title=#{title},content=#{content} where id=#{id} and writer_id=#{writer_id}")
    void updateContent(Board board);

}
