package kr.bit.mapper;

import kr.bit.entity.Board;
import kr.bit.entity.Like;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BoardMapper {
    @Insert("insert into boards(writer_id,title,content) values(#{writer_id},#{title},#{content})")
    void insertBoard(Board board);

    @Select("select * from boards order by id desc")
    List<Board> getBoards();

    @Select("select * from boards where id = #{id}")
    Board getBoard(int id);

    @Select("select count(*) from likes where user_id =#{user_id} and board_id = #{board_id}")
    int checkLikeExists(Like like);

    @Insert("insert into likes values(#{user_id},#{board_id})")
    void insertLike(Like like);

    @Delete("delete from likes where user_id =#{user_id} and board_id = #{board_id}")
    void deleteLike(Like like);

    @Select("select heart_count from boards where id=#{board_id}")
    int getHeartCount(int board_id);
}
