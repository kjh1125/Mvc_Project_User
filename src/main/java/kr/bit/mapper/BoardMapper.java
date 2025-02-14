package kr.bit.mapper;

import kr.bit.entity.Board;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BoardMapper {
    @Insert("insert into boards(writer_id,title,content) values(#{writer_id},#{title},#{content})")
    void insertBoard(Board board);

    @Select("select * from boards")
    List<Board> getBoards();

    @Select("select * from boards where id = #{id}")
    Board getBoard(int id);
}
